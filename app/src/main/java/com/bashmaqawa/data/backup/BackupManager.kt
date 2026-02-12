package com.bashmaqawa.data.backup

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.sqlite.db.SimpleSQLiteQuery
import com.bashmaqawa.data.database.AppDatabase
import com.bashmaqawa.data.model.BackupMetadata
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase
) {

    companion object {
        private const val METADATA_FILE = "metadata.json"
        private const val DATABASE_NAME = AppDatabase.DATABASE_NAME
        private const val WAL_FILE = "$DATABASE_NAME-wal"
        private const val SHM_FILE = "$DATABASE_NAME-shm"
        private const val TAG = "BackupManager"
    }

    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    suspend fun createBackup(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1. Checkpoint WAL to ensure data is in the main DB file
            // Using a raw query to force a checkpoint. 
            // Note: In some Room versions, you might need to use the support database directly.
            database.openHelper.writableDatabase.query(SimpleSQLiteQuery("pragma wal_checkpoint(full)"))

            // 2. Prepare files
            val dbFile = context.getDatabasePath(DATABASE_NAME)
            val walFile = context.getDatabasePath(WAL_FILE)
            val shmFile = context.getDatabasePath(SHM_FILE)

            if (!dbFile.exists()) {
                return@withContext Result.failure(Exception("Database file not found"))
            }

            // 3. Create Metadata
            val checksum = calculateFileChecksum(dbFile)
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            val metadata = BackupMetadata(
                versionCode = info.versionCode, // Deprecated in newer APIs but still widely used. 
                // specialized handling for API 28+ uses longVersionCode, but for simplicity int is often enough or use PackageInfoCompat
                versionName = info.versionName ?: "1.0",
                timestamp = System.currentTimeMillis(),
                checksum = checksum,
                manufacturer = Build.MANUFACTURER,
                model = Build.MODEL,
                sdkInt = Build.VERSION.SDK_INT
            )

            // 4. Create Zip Stream to the target URI
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                ZipOutputStream(BufferedOutputStream(outputStream)).use { zipOut ->
                    // Add Metadata
                    val metadataJson = json.encodeToString(metadata)
                    val metadataEntry = ZipEntry(METADATA_FILE)
                    zipOut.putNextEntry(metadataEntry)
                    zipOut.write(metadataJson.toByteArray())
                    zipOut.closeEntry()

                    // Add DB File
                    addFileToZip(dbFile, zipOut)

                    // Add WAL/SHM if they exist (Good practice for complete state restoration, 
                    // though Checkpoint should make them mostly empty)
                    if (walFile.exists()) addFileToZip(walFile, zipOut)
                    if (shmFile.exists()) addFileToZip(shmFile, zipOut)
                }
            } ?: return@withContext Result.failure(Exception("Could not open output stream"))

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Backup failed", e)
            Result.failure(e)
        }
    }

    suspend fun restoreBackup(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        // Prepare temp directory
        val tempDir = File(context.cacheDir, "restore_temp")
        if (tempDir.exists()) tempDir.deleteRecursively()
        tempDir.mkdirs()

        try {
            // 1. Unzip to temp
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                ZipInputStream(BufferedInputStream(inputStream)).use { zipIn ->
                    var entry = zipIn.nextEntry
                    while (entry != null) {
                        val file = File(tempDir, entry.name)
                        // Security: prevent zip path traversal
                        if (!file.canonicalPath.startsWith(tempDir.canonicalPath)) {
                            throw SecurityException("Zip path traversal attempted")
                        }
                        
                        FileOutputStream(file).use { fos ->
                            zipIn.copyTo(fos)
                        }
                        zipIn.closeEntry()
                        entry = zipIn.nextEntry
                    }
                }
            } ?: return@withContext Result.failure(Exception("Could not open input stream"))

            // 2. Load and Verify Metadata
            val metadataFile = File(tempDir, METADATA_FILE)
            if (!metadataFile.exists()) {
                return@withContext Result.failure(Exception("Invalid backup: Missing metadata"))
            }
            val metadataJson = metadataFile.readText()
            val metadata = try {
                json.decodeFromString<BackupMetadata>(metadataJson)
            } catch (e: Exception) {
                return@withContext Result.failure(Exception("Invalid backup: Corrupted metadata"))
            }

            // 3. Verify Checksum of the extracted DB file
            val restoredDbFile = File(tempDir, DATABASE_NAME)
            if (!restoredDbFile.exists()) {
                return@withContext Result.failure(Exception("Invalid backup: Missing database file"))
            }
            val calculatedChecksum = calculateFileChecksum(restoredDbFile)
            if (calculatedChecksum != metadata.checksum) {
                return@withContext Result.failure(Exception("Data corruption detected: Checksum mismatch"))
            }

            // 4. Overwrite Database
            // Ideally close the DB first. Since we are in the app, this is tricky. 
            // We'll rely on the ViewModel to trigger a restart or re-initialization after this returns success.
            // But to safely overwrite, we should try to close it if possible, or just overwrite (Room handles open files relatively well on restart if we kill it).
            // Better approach: Close the Room database connection.
            database.close()

            val targetDb = context.getDatabasePath(DATABASE_NAME)
            val targetWal = context.getDatabasePath(WAL_FILE)
            val targetShm = context.getDatabasePath(SHM_FILE)

            // Copy files
            restoredDbFile.copyTo(targetDb, overwrite = true)
            
            // Handle WAL/SHM
            val restoredWal = File(tempDir, WAL_FILE)
            val restoredShm = File(tempDir, SHM_FILE)
            
            if (restoredWal.exists()) {
                restoredWal.copyTo(targetWal, overwrite = true)
            } else {
                targetWal.delete() // Delete existing WAL if not in backup
            }
            
            if (restoredShm.exists()) {
                restoredShm.copyTo(targetShm, overwrite = true)
            } else {
                targetShm.delete() // Delete existing SHM if not in backup
            }

            // Cleanup
            tempDir.deleteRecursively()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Restore failed", e)
            tempDir.deleteRecursively()
            Result.failure(e)
        }
    }

    private fun addFileToZip(file: File, zipOut: ZipOutputStream) {
        FileInputStream(file).use { fis ->
            val entry = ZipEntry(file.name)
            zipOut.putNextEntry(entry)
            fis.copyTo(zipOut)
            zipOut.closeEntry()
        }
    }

    private fun calculateFileChecksum(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(8192) // 8KB buffer
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
