package com.bashmaqawa.di

import android.content.Context
import com.bashmaqawa.data.backup.BackupManager
import com.bashmaqawa.data.database.AppDatabase
import com.bashmaqawa.data.repository.BackupRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BackupModule {

    @Provides
    @Singleton
    fun provideBackupManager(
        @ApplicationContext context: Context,
        database: AppDatabase
    ): BackupManager {
        return BackupManager(context, database)
    }

    @Provides
    @Singleton
    fun provideBackupRepository(
        backupManager: BackupManager
    ): BackupRepository {
        return BackupRepository(backupManager)
    }
}
