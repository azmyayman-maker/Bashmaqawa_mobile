package com.bashmaqawa.data.repository

import android.net.Uri
import com.bashmaqawa.data.backup.BackupManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepository @Inject constructor(
    private val backupManager: BackupManager
) {

    suspend fun createBackup(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        backupManager.createBackup(uri)
    }

    suspend fun restoreBackup(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        backupManager.restoreBackup(uri)
    }
}
