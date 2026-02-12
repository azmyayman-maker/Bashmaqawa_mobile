package com.bashmaqawa.di

import android.content.Context
import com.bashmaqawa.data.database.AppDatabase
import com.bashmaqawa.data.database.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing database dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }
    
    @Provides
    fun provideWorkerCategoryDao(database: AppDatabase): WorkerCategoryDao {
        return database.workerCategoryDao()
    }
    
    @Provides
    fun provideProjectDao(database: AppDatabase): ProjectDao {
        return database.projectDao()
    }
    
    @Provides
    fun provideWorkerDao(database: AppDatabase): WorkerDao {
        return database.workerDao()
    }
    
    @Provides
    fun provideWorkerAdvanceDao(database: AppDatabase): WorkerAdvanceDao {
        return database.workerAdvanceDao()
    }
    
    @Provides
    fun provideWorkerDeductionDao(database: AppDatabase): WorkerDeductionDao {
        return database.workerDeductionDao()
    }
    
    @Provides
    fun provideAttendanceDao(database: AppDatabase): AttendanceDao {
        return database.attendanceDao()
    }
    
    @Provides
    fun provideCostCenterDao(database: AppDatabase): CostCenterDao {
        return database.costCenterDao()
    }
    
    @Provides
    fun provideAccountDao(database: AppDatabase): AccountDao {
        return database.accountDao()
    }
    
    @Provides
    fun provideTransactionDao(database: AppDatabase): TransactionDao {
        return database.transactionDao()
    }
    
    @Provides
    fun provideAIMemoryDao(database: AppDatabase): AIMemoryDao {
        return database.aiMemoryDao()
    }
    
    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }
    
    @Provides
    fun provideJournalEntryDao(database: AppDatabase): JournalEntryDao {
        return database.journalEntryDao()
    }
    
    @Provides
    fun providePayrollDao(database: AppDatabase): PayrollDao {
        return database.payrollDao()
    }
}
