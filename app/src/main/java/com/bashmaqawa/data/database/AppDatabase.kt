package com.bashmaqawa.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bashmaqawa.data.database.dao.*
import com.bashmaqawa.data.database.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Main Room Database for Bashmaqawa ERP
 * قاعدة بيانات الغرفة الرئيسية لنظام بشمقاول
 * 
 * Version 4: Added double-entry accounting support
 * - JournalEntry entity for audit trail
 * - PayrollEntry entity for wage tracking
 * - New fields in Transaction, Account, WorkerAdvance
 */
@Database(
    entities = [
        WorkerCategory::class,
        Project::class,
        Worker::class,
        WorkerAdvance::class,
        WorkerDeduction::class,
        Attendance::class,
        CostCenter::class,
        Account::class,
        Transaction::class,
        AIMemory::class,
        User::class,
        JournalEntry::class,
        PayrollEntry::class
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    // DAOs
    abstract fun workerCategoryDao(): WorkerCategoryDao
    abstract fun projectDao(): ProjectDao
    abstract fun workerDao(): WorkerDao
    abstract fun workerAdvanceDao(): WorkerAdvanceDao
    abstract fun workerDeductionDao(): WorkerDeductionDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun costCenterDao(): CostCenterDao
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun aiMemoryDao(): AIMemoryDao
    abstract fun userDao(): UserDao
    abstract fun journalEntryDao(): JournalEntryDao
    abstract fun payrollDao(): PayrollDao
    
    companion object {
        const val DATABASE_NAME = "bashmaqawa.db"
        
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        /**
         * Migration from version 3 to 4
         * Adds double-entry accounting tables and fields
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add new columns to transactions table
                db.execSQL("ALTER TABLE transactions ADD COLUMN source_account_id INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE transactions ADD COLUMN destination_account_id INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE transactions ADD COLUMN transaction_state TEXT DEFAULT 'CLEARED'")
                db.execSQL("ALTER TABLE transactions ADD COLUMN journal_entry_id INTEGER DEFAULT NULL")
                
                // Create indices for new columns
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_source_account_id ON transactions(source_account_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_destination_account_id ON transactions(destination_account_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_transaction_state ON transactions(transaction_state)")
                
                // Add new columns to accounts table
                db.execSQL("ALTER TABLE accounts ADD COLUMN account_code TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE accounts ADD COLUMN is_system_account INTEGER DEFAULT 0")
                
                // Create index for account_code
                db.execSQL("CREATE INDEX IF NOT EXISTS index_accounts_account_code ON accounts(account_code)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_accounts_category ON accounts(category)")
                
                // Add new columns to worker_advances table
                db.execSQL("ALTER TABLE worker_advances ADD COLUMN account_id INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE worker_advances ADD COLUMN transaction_id INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE worker_advances ADD COLUMN is_settled INTEGER DEFAULT 0")
                db.execSQL("ALTER TABLE worker_advances ADD COLUMN settled_date TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE worker_advances ADD COLUMN settled_amount REAL DEFAULT NULL")
                db.execSQL("ALTER TABLE worker_advances ADD COLUMN settlement_transaction_id INTEGER DEFAULT NULL")
                
                // Create indices for worker_advances
                db.execSQL("CREATE INDEX IF NOT EXISTS index_worker_advances_account_id ON worker_advances(account_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_worker_advances_transaction_id ON worker_advances(transaction_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_worker_advances_settlement_transaction_id ON worker_advances(settlement_transaction_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_worker_advances_is_settled ON worker_advances(is_settled)")
                
                // Create journal_entries table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS journal_entries (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        entry_date TEXT NOT NULL,
                        description TEXT NOT NULL,
                        debit_account_id INTEGER NOT NULL,
                        credit_account_id INTEGER NOT NULL,
                        amount REAL NOT NULL,
                        reference_type TEXT,
                        reference_id INTEGER,
                        is_reversing INTEGER NOT NULL DEFAULT 0,
                        reversed_entry_id INTEGER,
                        created_at TEXT,
                        FOREIGN KEY(debit_account_id) REFERENCES accounts(id) ON DELETE RESTRICT,
                        FOREIGN KEY(credit_account_id) REFERENCES accounts(id) ON DELETE RESTRICT
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS index_journal_entries_debit_account_id ON journal_entries(debit_account_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_journal_entries_credit_account_id ON journal_entries(credit_account_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_journal_entries_entry_date ON journal_entries(entry_date)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_journal_entries_reference_type_reference_id ON journal_entries(reference_type, reference_id)")
                
                // Create payroll_entries table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS payroll_entries (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        worker_id INTEGER NOT NULL,
                        period_start TEXT NOT NULL,
                        period_end TEXT NOT NULL,
                        days_present REAL NOT NULL DEFAULT 0,
                        half_days REAL NOT NULL DEFAULT 0,
                        overtime_hours REAL NOT NULL DEFAULT 0,
                        daily_rate REAL NOT NULL DEFAULT 0,
                        overtime_rate REAL NOT NULL DEFAULT 0,
                        gross_wage REAL NOT NULL DEFAULT 0,
                        deductions REAL NOT NULL DEFAULT 0,
                        advances_deducted REAL NOT NULL DEFAULT 0,
                        net_wage REAL NOT NULL DEFAULT 0,
                        status TEXT NOT NULL DEFAULT 'DRAFT',
                        project_id INTEGER,
                        transaction_id INTEGER,
                        notes TEXT,
                        created_at TEXT,
                        approved_at TEXT,
                        paid_at TEXT,
                        FOREIGN KEY(worker_id) REFERENCES workers(id) ON DELETE CASCADE,
                        FOREIGN KEY(project_id) REFERENCES projects(id) ON DELETE SET NULL,
                        FOREIGN KEY(transaction_id) REFERENCES transactions(id) ON DELETE SET NULL
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS index_payroll_entries_worker_id ON payroll_entries(worker_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_payroll_entries_project_id ON payroll_entries(project_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_payroll_entries_transaction_id ON payroll_entries(transaction_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_payroll_entries_period_start_period_end ON payroll_entries(period_start, period_end)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_payroll_entries_status ON payroll_entries(status)")
            }
        }
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addCallback(DatabaseCallback())
                    .addMigrations(MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
    
    /**
     * Database callback to seed initial data
     */
    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    // Seed worker categories
                    populateWorkerCategories(database.workerCategoryDao())
                    
                    // Seed default user
                    populateDefaultUser(database.userDao())
                    
                    // Seed default accounts including system accounts
                    populateDefaultAccounts(database.accountDao())
                }
            }
        }
        
        private suspend fun populateWorkerCategories(dao: WorkerCategoryDao) {
            dao.insertCategories(WorkerCategorySeedData.categories)
        }
        
        private suspend fun populateDefaultUser(dao: UserDao) {
            dao.insertUser(DefaultUser.admin)
        }
        
        private suspend fun populateDefaultAccounts(dao: AccountDao) {
            val accounts = listOf(
                // Asset accounts
                Account(
                    id = 1,
                    name = "الخزنة الرئيسية",
                    type = AccountType.CASH_BOX,
                    balance = 0.0,
                    category = AccountCategory.ASSET,
                    accountCode = AccountCodes.CASH,
                    isActive = true
                ),
                Account(
                    id = 2,
                    name = "البنك الأهلي",
                    type = AccountType.BANK,
                    balance = 0.0,
                    category = AccountCategory.ASSET,
                    accountCode = AccountCodes.BANK,
                    bankName = "البنك الأهلي المصري",
                    isActive = true
                ),
                Account(
                    id = 3,
                    name = "محفظة فودافون كاش",
                    type = AccountType.WALLET,
                    balance = 0.0,
                    category = AccountCategory.ASSET,
                    accountCode = AccountCodes.WALLET,
                    isActive = true
                ),
                // System accounts for double-entry
                Account(
                    id = 4,
                    name = "سلف العمال",
                    type = AccountType.RECEIVABLE,
                    balance = 0.0,
                    category = AccountCategory.ASSET,
                    accountCode = AccountCodes.ADVANCES_RECEIVABLE,
                    isSystemAccount = true,
                    isActive = true
                ),
                Account(
                    id = 5,
                    name = "الأجور المستحقة",
                    type = AccountType.PAYABLE,
                    balance = 0.0,
                    category = AccountCategory.LIABILITY,
                    accountCode = AccountCodes.WAGES_PAYABLE,
                    isSystemAccount = true,
                    isActive = true
                )
            )
            accounts.forEach { dao.insertAccount(it) }
        }
    }
}

