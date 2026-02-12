package com.bashmaqawa.data.repository

import androidx.room.withTransaction
import com.bashmaqawa.core.DateRange
import com.bashmaqawa.core.ErrorCode
import com.bashmaqawa.core.Resource
import com.bashmaqawa.core.safeCall
import com.bashmaqawa.data.database.AppDatabase
import com.bashmaqawa.data.database.dao.*
import com.bashmaqawa.data.database.entities.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Profit/Loss Report Data Class
 */
data class ProfitLossReport(
    val projectId: Int?,
    val projectName: String?,
    val totalIncome: Double,
    val totalExpense: Double,
    val wagesCost: Double,
    val materialsCost: Double,
    val otherCosts: Double,
    val netProfit: Double,
    val profitMargin: Double // As percentage
)

/**
 * Payroll Calculation Result
 */
data class PayrollCalculation(
    val workerId: Int,
    val workerName: String,
    val daysPresent: Double,
    val halfDays: Double,
    val overtimeHours: Double,
    val dailyRate: Double,
    val overtimeRate: Double,
    val grossWage: Double,
    val deductions: Double,
    val unsettledAdvances: Double,
    val netWage: Double
)

/**
 * Repository for Financial operations (Accounts & Transactions)
 * مستودع العمليات المالية - Double-Entry Accounting Engine
 * 
 * Implements ACID-compliant financial transactions with strict account linkage.
 * Every financial movement creates corresponding journal entries for audit trail.
 */
@Singleton
class FinancialRepository @Inject constructor(
    private val database: AppDatabase,
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao,
    private val journalEntryDao: JournalEntryDao,
    private val payrollDao: PayrollDao,
    private val attendanceDao: AttendanceDao,
    private val workerAdvanceDao: WorkerAdvanceDao,
    private val workerDao: WorkerDao
) {
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    
    // =====================================================
    // ACCOUNT OPERATIONS
    // =====================================================
    
    fun getAllActiveAccounts(): Flow<List<Account>> =
        accountDao.getAllActiveAccounts()
    
    fun getAccountsByType(type: AccountType): Flow<List<Account>> =
        accountDao.getAccountsByType(type)
    
    fun getAccountsByCategory(category: AccountCategory): Flow<List<Account>> =
        accountDao.getAccountsByCategory(category)
    
    fun getRootAccounts(): Flow<List<Account>> =
        accountDao.getRootAccounts()
    
    fun getChildAccounts(parentId: Int): Flow<List<Account>> =
        accountDao.getChildAccounts(parentId)
    
    suspend fun getAccountById(id: Int): Account? =
        accountDao.getAccountById(id)
    
    fun getAccountByIdFlow(id: Int): Flow<Account?> =
        accountDao.getAccountByIdFlow(id)
    
    suspend fun getAccountByCode(code: String): Account? =
        accountDao.getAccountByCode(code)
    
    fun getSystemAccounts(): Flow<List<Account>> =
        accountDao.getSystemAccounts()
    
    suspend fun getTotalCashBalance(): Double =
        accountDao.getTotalBalanceByType(AccountType.CASH_BOX) ?: 0.0
    
    suspend fun getTotalBankBalance(): Double =
        accountDao.getTotalBalanceByType(AccountType.BANK) ?: 0.0
    
    suspend fun getTotalWalletBalance(): Double =
        accountDao.getTotalBalanceByType(AccountType.WALLET) ?: 0.0
    
    suspend fun getTotalAssets(): Double =
        accountDao.getTotalAssets() ?: 0.0
    
    suspend fun getTotalLiabilities(): Double =
        accountDao.getTotalLiabilities() ?: 0.0
    
    suspend fun insertAccount(account: Account): Long {
        val now = LocalDateTime.now().format(dateTimeFormatter)
        return accountDao.insertAccount(account.copy(createdAt = now, updatedAt = now))
    }
    
    suspend fun updateAccount(account: Account) {
        val now = LocalDateTime.now().format(dateTimeFormatter)
        accountDao.updateAccount(account.copy(updatedAt = now))
    }
    
    suspend fun updateAccountBalance(id: Int, amount: Double) {
        val now = LocalDateTime.now().format(dateTimeFormatter)
        accountDao.updateAccountBalance(id, amount, now)
    }
    
    suspend fun deactivateAccount(id: Int) {
        val now = LocalDateTime.now().format(dateTimeFormatter)
        accountDao.updateAccountActiveStatus(id, false, now)
    }
    
    suspend fun deleteAccount(id: Int) =
        accountDao.deleteAccountById(id)
    
    // =====================================================
    // TRANSACTION OPERATIONS - DOUBLE ENTRY ENGINE
    // =====================================================
    
    fun getAllTransactions(): Flow<List<Transaction>> =
        transactionDao.getAllTransactions()
    
    fun getAllActiveTransactions(): Flow<List<Transaction>> =
        transactionDao.getAllActiveTransactions()
    
    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>> =
        transactionDao.getTransactionsByType(type)
    
    fun getTransactionsByProjectId(projectId: Int): Flow<List<Transaction>> =
        transactionDao.getTransactionsByProjectId(projectId)
    
    fun getTransactionsByAccountId(accountId: Int): Flow<List<Transaction>> =
        transactionDao.getTransactionsByAccountId(accountId)
    
    fun getTransactionsByWorkerId(workerId: Int): Flow<List<Transaction>> =
        transactionDao.getTransactionsByWorkerId(workerId)
    
    fun getTransactionsInDateRange(startDate: String, endDate: String): Flow<List<Transaction>> =
        transactionDao.getTransactionsInDateRange(startDate, endDate)
    
    fun getTransactionsWithDetailsInRange(startDate: String, endDate: String): Flow<List<TransactionWithDetails>> =
        transactionDao.getTransactionsWithDetailsInRange(startDate, endDate)
    
    suspend fun getTransactionById(id: Int): Transaction? =
        transactionDao.getTransactionById(id)
    
    suspend fun getTotalIncomeInRange(startDate: String, endDate: String): Double =
        transactionDao.getTotalByTypeInRange(TransactionType.INCOME, startDate, endDate) ?: 0.0
    
    suspend fun getTotalExpenseInRange(startDate: String, endDate: String): Double =
        transactionDao.getTotalByTypeInRange(TransactionType.EXPENSE, startDate, endDate) ?: 0.0
    
    suspend fun getTotalIncomeByProject(projectId: Int): Double =
        transactionDao.getTotalByTypeAndProject(TransactionType.INCOME, projectId) ?: 0.0
    
    suspend fun getTotalExpenseByProject(projectId: Int): Double =
        transactionDao.getTotalByTypeAndProject(TransactionType.EXPENSE, projectId) ?: 0.0
    
    suspend fun getTodayExpenses(date: String): Double =
        transactionDao.getTotalByTypeAndDate(TransactionType.EXPENSE, date) ?: 0.0
    
    suspend fun getTodayIncome(date: String): Double =
        transactionDao.getTotalByTypeAndDate(TransactionType.INCOME, date) ?: 0.0
    
    suspend fun getExpenseCategorySummary(startDate: String, endDate: String): List<CategorySummary> =
        transactionDao.getCategorySummary(TransactionType.EXPENSE, startDate, endDate)
    
    suspend fun getIncomeCategorySummary(startDate: String, endDate: String): List<CategorySummary> =
        transactionDao.getCategorySummary(TransactionType.INCOME, startDate, endDate)
    
    /**
     * Validate a transaction before processing
     * يتحقق من صحة المعاملة قبل تنفيذها
     */
    suspend fun validateTransaction(transaction: Transaction): Resource<Unit> {
        // Check amount is positive
        val amount = transaction.amount ?: return Resource.Error(
            message = "المبلغ مطلوب",
            errorCode = ErrorCode.INVALID_AMOUNT
        )
        
        if (amount <= 0) {
            return Resource.Error(
                message = "المبلغ يجب أن يكون أكبر من صفر",
                errorCode = ErrorCode.INVALID_AMOUNT
            )
        }
        
        // Check source account exists
        val sourceAccountId = transaction.effectiveSourceAccountId
        if (sourceAccountId != null) {
            val sourceAccount = accountDao.getAccountById(sourceAccountId)
                ?: return Resource.Error(
                    message = "الحساب المصدر غير موجود",
                    errorCode = ErrorCode.ACCOUNT_NOT_FOUND
                )
            
            // For expenses, check sufficient balance
            if (transaction.type == TransactionType.EXPENSE) {
                if (sourceAccount.balance < amount) {
                    return Resource.Error(
                        message = "الرصيد غير كافي. الرصيد المتاح: ${sourceAccount.balance}",
                        errorCode = ErrorCode.INSUFFICIENT_BALANCE
                    )
                }
            }
        }
        
        // Check destination account exists for transfers
        if (transaction.type == TransactionType.TRANSFER) {
            val destAccountId = transaction.destinationAccountId
                ?: return Resource.Error(
                    message = "حساب الوجهة مطلوب للتحويلات",
                    errorCode = ErrorCode.VALIDATION_FAILED
                )
            
            accountDao.getAccountById(destAccountId)
                ?: return Resource.Error(
                    message = "حساب الوجهة غير موجود",
                    errorCode = ErrorCode.ACCOUNT_NOT_FOUND
                )
        }
        
        return Resource.Success(Unit)
    }
    
    /**
     * Process a transaction with ACID compliance
     * يعالج المعاملة مع ضمان ACID
     * 
     * This is the master function for all financial transactions.
     * It executes within a database transaction, ensuring atomicity.
     */
    suspend fun processTransaction(transaction: Transaction): Resource<Long> {
        // Validate first
        val validation = validateTransaction(transaction)
        if (validation is Resource.Error) return Resource.Error(validation.message, validation.cause, validation.errorCode)
        
        return safeCall {
            database.withTransaction {
                val now = LocalDateTime.now().format(dateTimeFormatter)
                val date = LocalDateTime.now().format(dateFormatter)
                
                val sourceAccountId = transaction.effectiveSourceAccountId
                val amount = transaction.amount!!
                
                // Create journal entry first
                var journalEntryId: Int? = null
                
                when (transaction.type) {
                    TransactionType.EXPENSE -> {
                        if (sourceAccountId != null) {
                            // Debit the expense category (conceptually)
                            // Credit the source account (decrease cash/bank)
                            val journalEntry = JournalEntry(
                                entryDate = date,
                                description = transaction.description ?: "مصروف: ${transaction.category}",
                                debitAccountId = sourceAccountId, // For expenses, we debit the source (conceptually the expense account)
                                creditAccountId = sourceAccountId, // Self-referencing for single-account expenses
                                amount = amount,
                                referenceType = JournalReferenceType.TRANSACTION,
                                createdAt = now
                            )
                            journalEntryId = journalEntryDao.insertJournalEntry(journalEntry).toInt()
                            
                            // Decrease source account balance
                            accountDao.updateAccountBalance(sourceAccountId, -amount, now)
                        }
                    }
                    
                    TransactionType.INCOME -> {
                        if (sourceAccountId != null) {
                            // Debit the source account (increase cash/bank)
                            // Credit the revenue (conceptually)
                            val journalEntry = JournalEntry(
                                entryDate = date,
                                description = transaction.description ?: "إيراد: ${transaction.category}",
                                debitAccountId = sourceAccountId,
                                creditAccountId = sourceAccountId,
                                amount = amount,
                                referenceType = JournalReferenceType.TRANSACTION,
                                createdAt = now
                            )
                            journalEntryId = journalEntryDao.insertJournalEntry(journalEntry).toInt()
                            
                            // Increase source account balance
                            accountDao.updateAccountBalance(sourceAccountId, amount, now)
                        }
                    }
                    
                    TransactionType.TRANSFER -> {
                        val destAccountId = transaction.destinationAccountId!!
                        
                        // Debit destination, Credit source
                        val journalEntry = JournalEntry(
                            entryDate = date,
                            description = transaction.description ?: "تحويل بين حسابات",
                            debitAccountId = destAccountId,
                            creditAccountId = sourceAccountId!!,
                            amount = amount,
                            referenceType = JournalReferenceType.TRANSACTION,
                            createdAt = now
                        )
                        journalEntryId = journalEntryDao.insertJournalEntry(journalEntry).toInt()
                        
                        // Decrease source, increase destination
                        accountDao.updateAccountBalance(sourceAccountId, -amount, now)
                        accountDao.updateAccountBalance(destAccountId, amount, now)
                    }
                    
                    null -> { /* No-op */ }
                }
                
                // Insert the transaction
                val newTransaction = transaction.copy(
                    sourceAccountId = sourceAccountId,
                    transactionState = TransactionState.CLEARED,
                    journalEntryId = journalEntryId,
                    date = transaction.date ?: date,
                    createdAt = now,
                    modifiedAt = now
                )
                
                transactionDao.insertTransaction(newTransaction)
            }
        }
    }
    
    /**
     * Simple transaction insert (legacy support, does not enforce double-entry)
     * إدخال معاملة بسيط (دعم الإصدارات القديمة)
     */
    suspend fun insertTransaction(transaction: Transaction): Long {
        val now = LocalDateTime.now().format(dateTimeFormatter)
        val newTransaction = transaction.copy(createdAt = now, modifiedAt = now)
        val id = transactionDao.insertTransaction(newTransaction)
        
        // Update account balance if account is specified
        transaction.accountId?.let { accountId ->
            transaction.amount?.let { amount ->
                val balanceChange = when (transaction.type) {
                    TransactionType.INCOME -> amount
                    TransactionType.EXPENSE -> -amount
                    TransactionType.TRANSFER -> 0.0 // Transfers need separate handling
                    null -> 0.0
                }
                updateAccountBalance(accountId, balanceChange)
            }
        }
        
        return id
    }
    
    suspend fun updateTransaction(transaction: Transaction) {
        val now = LocalDateTime.now().format(dateTimeFormatter)
        transactionDao.updateTransaction(transaction.copy(modifiedAt = now))
    }
    
    suspend fun deleteTransaction(id: Int) =
        transactionDao.deleteTransactionById(id)
    
    /**
     * Reverse/Void a transaction
     * يلغي المعاملة
     */
    suspend fun reverseTransaction(transactionId: Int): Resource<Unit> {
        val transaction = transactionDao.getTransactionById(transactionId)
            ?: return Resource.Error("المعاملة غير موجودة", errorCode = ErrorCode.TRANSACTION_NOT_FOUND)
        
        if (transaction.transactionState == TransactionState.VOID) {
            return Resource.Error("المعاملة ملغاة بالفعل", errorCode = ErrorCode.TRANSACTION_ALREADY_VOID)
        }
        
        return safeCall {
            database.withTransaction {
                val now = LocalDateTime.now().format(dateTimeFormatter)
                val date = LocalDateTime.now().format(dateFormatter)
                val amount = transaction.amount ?: 0.0
                val sourceAccountId = transaction.effectiveSourceAccountId
                
                // Reverse the balance changes
                when (transaction.type) {
                    TransactionType.EXPENSE -> {
                        if (sourceAccountId != null) {
                            // Restore the balance
                            accountDao.updateAccountBalance(sourceAccountId, amount, now)
                        }
                    }
                    TransactionType.INCOME -> {
                        if (sourceAccountId != null) {
                            // Remove the income
                            accountDao.updateAccountBalance(sourceAccountId, -amount, now)
                        }
                    }
                    TransactionType.TRANSFER -> {
                        val destAccountId = transaction.destinationAccountId
                        if (sourceAccountId != null && destAccountId != null) {
                            // Reverse: add back to source, subtract from dest
                            accountDao.updateAccountBalance(sourceAccountId, amount, now)
                            accountDao.updateAccountBalance(destAccountId, -amount, now)
                        }
                    }
                    null -> { /* No-op */ }
                }
                
                // Create reversing journal entry
                if (transaction.journalEntryId != null) {
                    val originalEntry = journalEntryDao.getJournalEntryById(transaction.journalEntryId)
                    if (originalEntry != null) {
                        val reversingEntry = JournalEntry(
                            entryDate = date,
                            description = "إلغاء: ${originalEntry.description}",
                            debitAccountId = originalEntry.creditAccountId,
                            creditAccountId = originalEntry.debitAccountId,
                            amount = originalEntry.amount,
                            referenceType = JournalReferenceType.TRANSACTION,
                            referenceId = transactionId,
                            isReversing = true,
                            reversedEntryId = originalEntry.id,
                            createdAt = now
                        )
                        journalEntryDao.insertJournalEntry(reversingEntry)
                    }
                }
                
                // Mark transaction as void
                transactionDao.updateTransactionState(transactionId, TransactionState.VOID, now)
            }
        }
    }
    
    // =====================================================
    // PAYROLL OPERATIONS
    // =====================================================
    
    fun getAllPayrollEntries(): Flow<List<PayrollEntry>> =
        payrollDao.getAllPayrollEntries()
    
    fun getPayrollEntriesWithWorker(): Flow<List<PayrollWithWorker>> =
        payrollDao.getAllPayrollEntriesWithWorker()
    
    fun getPayrollEntriesByWorkerId(workerId: Int): Flow<List<PayrollEntry>> =
        payrollDao.getPayrollEntriesByWorkerId(workerId)
    
    fun getPayrollEntriesByStatus(status: PayrollStatus): Flow<List<PayrollEntry>> =
        payrollDao.getPayrollEntriesByStatus(status)
    
    /**
     * Calculate payroll for a worker based on attendance
     * يحسب كشف الأجور للعامل بناءً على الحضور
     * 
     * Formula: GrossWage = (DailyRate × DaysPresent) + (OvertimeHours × OvertimeRate)
     * NetWage = GrossWage - Deductions - UnsettledAdvances
     */
    suspend fun calculatePayroll(workerId: Int, dateRange: DateRange): Resource<PayrollCalculation> {
        val worker = workerDao.getWorkerById(workerId)
            ?: return Resource.Error("العامل غير موجود", errorCode = ErrorCode.WORKER_NOT_FOUND)
        
        return safeCall {
            val dailyRate = worker.dailyRate ?: 0.0
            val overtimeRate = (dailyRate / 8.0) * 1.5 // 1.5x hourly rate
            
            // Count attendance
            val attendanceList = attendanceDao.getWorkerAttendanceInRange(
                workerId, 
                dateRange.startDateString, 
                dateRange.endDateString
            ).first()
            
            var daysPresent = 0.0
            var halfDays = 0.0
            var overtimeHours = 0.0
            
            for (attendance in attendanceList) {
                when (attendance.status) {
                    AttendanceStatus.PRESENT -> daysPresent += 1.0
                    AttendanceStatus.HALF_DAY -> halfDays += 1.0
                    AttendanceStatus.OVERTIME -> {
                        daysPresent += 1.0
                        overtimeHours += attendance.overtimeHours ?: 0.0
                    }
                    AttendanceStatus.ABSENT -> { /* No-op */ }
                }
            }
            
            // Calculate gross wage
            val regularWage = dailyRate * daysPresent
            val halfDayWage = (dailyRate / 2.0) * halfDays
            val overtimeWage = overtimeRate * overtimeHours
            val grossWage = regularWage + halfDayWage + overtimeWage
            
            // Get deductions
            val deductions = workerDao.getTotalDeductionsByWorkerInRange(
                workerId, 
                dateRange.startDateString, 
                dateRange.endDateString
            ) ?: 0.0
            
            // Get unsettled advances
            val unsettledAdvances = workerAdvanceDao.getUnsettledAdvancesByWorkerId(workerId) ?: 0.0
            
            val netWage = grossWage - deductions - unsettledAdvances
            
            PayrollCalculation(
                workerId = workerId,
                workerName = worker.name,
                daysPresent = daysPresent,
                halfDays = halfDays,
                overtimeHours = overtimeHours,
                dailyRate = dailyRate,
                overtimeRate = overtimeRate,
                grossWage = grossWage,
                deductions = deductions,
                unsettledAdvances = unsettledAdvances,
                netWage = netWage.coerceAtLeast(0.0)
            )
        }
    }
    
    /**
     * Generate and save a payroll entry
     * ينشئ ويحفظ كشف أجور
     */
    suspend fun generatePayroll(
        workerId: Int, 
        dateRange: DateRange,
        projectId: Int? = null,
        includeAdvances: Boolean = true
    ): Resource<PayrollEntry> {
        val calculation = calculatePayroll(workerId, dateRange)
        if (calculation is Resource.Error) {
            return Resource.Error(calculation.message, calculation.cause, calculation.errorCode)
        }
        
        val calc = (calculation as Resource.Success).data
        
        return safeCall {
            database.withTransaction {
                val now = LocalDateTime.now().format(dateTimeFormatter)
                
                val payrollEntry = PayrollEntry(
                    workerId = workerId,
                    periodStart = dateRange.startDateString,
                    periodEnd = dateRange.endDateString,
                    daysPresent = calc.daysPresent,
                    halfDays = calc.halfDays,
                    overtimeHours = calc.overtimeHours,
                    dailyRate = calc.dailyRate,
                    overtimeRate = calc.overtimeRate,
                    grossWage = calc.grossWage,
                    deductions = calc.deductions,
                    advancesDeducted = if (includeAdvances) calc.unsettledAdvances else 0.0,
                    netWage = if (includeAdvances) calc.netWage else calc.grossWage - calc.deductions,
                    status = PayrollStatus.DRAFT,
                    projectId = projectId,
                    createdAt = now
                )
                
                val id = payrollDao.insertPayrollEntry(payrollEntry)
                
                // Create liability in Wages Payable account
                val wagesPayableAccount = accountDao.getAccountByCode(AccountCodes.WAGES_PAYABLE)
                if (wagesPayableAccount != null) {
                    accountDao.updateAccountBalance(wagesPayableAccount.id, payrollEntry.grossWage, now)
                    
                    // Create journal entry
                    journalEntryDao.insertJournalEntry(
                        JournalEntry(
                            entryDate = dateRange.endDateString,
                            description = "استحقاق أجور: ${calc.workerName}",
                            debitAccountId = wagesPayableAccount.id, // We're recording the liability
                            creditAccountId = wagesPayableAccount.id,
                            amount = payrollEntry.grossWage,
                            referenceType = JournalReferenceType.PAYROLL,
                            referenceId = id.toInt(),
                            createdAt = now
                        )
                    )
                }
                
                payrollEntry.copy(id = id.toInt())
            }
        }
    }
    
    /**
     * Process wage payment
     * يعالج دفع الأجور
     */
    suspend fun processWagePayment(
        payrollId: Int,
        sourceAccountId: Int,
        settleAdvances: List<Int> = emptyList()
    ): Resource<Long> {
        val payroll = payrollDao.getPayrollEntryById(payrollId)
            ?: return Resource.Error("كشف الأجور غير موجود", errorCode = ErrorCode.TRANSACTION_NOT_FOUND)
        
        if (payroll.status == PayrollStatus.PAID) {
            return Resource.Error("كشف الأجور مدفوع بالفعل", errorCode = ErrorCode.PAYROLL_ALREADY_PAID)
        }
        
        // Check source account balance
        val sourceAccount = accountDao.getAccountById(sourceAccountId)
            ?: return Resource.Error("الحساب غير موجود", errorCode = ErrorCode.ACCOUNT_NOT_FOUND)
        
        if (sourceAccount.balance < payroll.netWage) {
            return Resource.Error(
                "الرصيد غير كافي. المطلوب: ${payroll.netWage}, المتاح: ${sourceAccount.balance}",
                errorCode = ErrorCode.INSUFFICIENT_BALANCE
            )
        }
        
        return safeCall {
            database.withTransaction {
                val now = LocalDateTime.now().format(dateTimeFormatter)
                val date = LocalDateTime.now().format(dateFormatter)
                
                // Create payment transaction
                val transaction = Transaction(
                    type = TransactionType.EXPENSE,
                    amount = payroll.netWage,
                    category = "Wages",
                    description = "دفع أجور للفترة ${payroll.periodStart} - ${payroll.periodEnd}",
                    sourceAccountId = sourceAccountId,
                    workerId = payroll.workerId,
                    projectId = payroll.projectId,
                    date = date,
                    transactionState = TransactionState.CLEARED,
                    createdAt = now,
                    modifiedAt = now
                )
                
                val transactionId = transactionDao.insertTransaction(transaction)
                
                // Decrease source account (cash outflow)
                accountDao.updateAccountBalance(sourceAccountId, -payroll.netWage, now)
                
                // Decrease Wages Payable liability
                val wagesPayableAccount = accountDao.getAccountByCode(AccountCodes.WAGES_PAYABLE)
                if (wagesPayableAccount != null) {
                    accountDao.updateAccountBalance(wagesPayableAccount.id, -payroll.grossWage, now)
                }
                
                // Create journal entry
                journalEntryDao.insertJournalEntry(
                    JournalEntry(
                        entryDate = date,
                        description = "دفع أجور",
                        debitAccountId = wagesPayableAccount?.id ?: sourceAccountId,
                        creditAccountId = sourceAccountId,
                        amount = payroll.netWage,
                        referenceType = JournalReferenceType.PAYROLL,
                        referenceId = payrollId,
                        createdAt = now
                    )
                )
                
                // Settle selected advances
                for (advanceId in settleAdvances) {
                    val advance = workerAdvanceDao.getAdvanceById(advanceId)
                    if (advance != null && !advance.isSettled && advance.workerId == payroll.workerId) {
                        workerAdvanceDao.updateAdvance(
                            advance.copy(
                                isSettled = true,
                                settledDate = date,
                                settledAmount = advance.amount,
                                settlementTransactionId = transactionId.toInt()
                            )
                        )
                        
                        // Decrease Advances Receivable
                        val advancesAccount = accountDao.getAccountByCode(AccountCodes.ADVANCES_RECEIVABLE)
                        if (advancesAccount != null) {
                            accountDao.updateAccountBalance(advancesAccount.id, -advance.amount, now)
                        }
                    }
                }
                
                // Update payroll status
                payrollDao.markPayrollAsPaid(payrollId, PayrollStatus.PAID, transactionId.toInt(), now)
                
                transactionId
            }
        }
    }
    
    // =====================================================
    // WORKER ADVANCE OPERATIONS
    // =====================================================
    
    /**
     * Process a worker advance payment
     * يعالج دفع سلفة للعامل
     */
    suspend fun processWorkerAdvance(
        workerId: Int,
        amount: Double,
        sourceAccountId: Int,
        reason: String? = null,
        notes: String? = null
    ): Resource<Long> {
        if (amount <= 0) {
            return Resource.Error("المبلغ يجب أن يكون أكبر من صفر", errorCode = ErrorCode.INVALID_AMOUNT)
        }
        
        val worker = workerDao.getWorkerById(workerId)
            ?: return Resource.Error("العامل غير موجود", errorCode = ErrorCode.WORKER_NOT_FOUND)
        
        val sourceAccount = accountDao.getAccountById(sourceAccountId)
            ?: return Resource.Error("الحساب غير موجود", errorCode = ErrorCode.ACCOUNT_NOT_FOUND)
        
        if (sourceAccount.balance < amount) {
            return Resource.Error(
                "الرصيد غير كافي. المتاح: ${sourceAccount.balance}",
                errorCode = ErrorCode.INSUFFICIENT_BALANCE
            )
        }
        
        return safeCall {
            database.withTransaction {
                val now = LocalDateTime.now().format(dateTimeFormatter)
                val date = LocalDateTime.now().format(dateFormatter)
                
                // Create the expense transaction
                val transaction = Transaction(
                    type = TransactionType.EXPENSE,
                    amount = amount,
                    category = "Advance",
                    description = "سلفة للعامل: ${worker.name}",
                    sourceAccountId = sourceAccountId,
                    workerId = workerId,
                    date = date,
                    transactionState = TransactionState.CLEARED,
                    createdAt = now,
                    modifiedAt = now
                )
                
                val transactionId = transactionDao.insertTransaction(transaction)
                
                // Decrease source account (cash outflow)
                accountDao.updateAccountBalance(sourceAccountId, -amount, now)
                
                // Increase Advances Receivable (asset for the company)
                val advancesAccount = accountDao.getAccountByCode(AccountCodes.ADVANCES_RECEIVABLE)
                if (advancesAccount != null) {
                    accountDao.updateAccountBalance(advancesAccount.id, amount, now)
                }
                
                // Create journal entry
                journalEntryDao.insertJournalEntry(
                    JournalEntry(
                        entryDate = date,
                        description = "سلفة للعامل: ${worker.name}",
                        debitAccountId = advancesAccount?.id ?: sourceAccountId,
                        creditAccountId = sourceAccountId,
                        amount = amount,
                        referenceType = JournalReferenceType.ADVANCE,
                        createdAt = now
                    )
                )
                
                // Create advance record
                val advance = WorkerAdvance(
                    workerId = workerId,
                    amount = amount,
                    reason = reason,
                    date = date,
                    accountId = sourceAccountId,
                    transactionId = transactionId.toInt(),
                    isSettled = false,
                    notes = notes,
                    createdAt = now
                )
                
                workerAdvanceDao.insertAdvance(advance)
            }
        }
    }
    
    // =====================================================
    // PROFIT & LOSS REPORTING
    // =====================================================
    
    /**
     * Get P&L report for a project
     * يحصل على تقرير الأرباح والخسائر للمشروع
     */
    suspend fun getProjectProfitLoss(projectId: Int): Resource<ProfitLossReport> {
        val projectPL = transactionDao.getProjectProfitLossById(projectId)
            ?: return Resource.Error("المشروع غير موجود", errorCode = ErrorCode.PROJECT_NOT_FOUND)
        
        return safeCall {
            // Get wage costs for project
            val wagesCost = payrollDao.getTotalByStatusAndProject(PayrollStatus.PAID, projectId) ?: 0.0
            
            // Calculate material costs from category
            val categorySummaries = transactionDao.getCategorySummary(
                TransactionType.EXPENSE, 
                "2000-01-01", 
                "2099-12-31"
            )
            val materialsCost = categorySummaries.find { it.category == "Material" }?.total ?: 0.0
            
            val otherCosts = projectPL.totalExpense - wagesCost - materialsCost
            val profitMargin = if (projectPL.totalIncome > 0) {
                (projectPL.netProfit / projectPL.totalIncome) * 100
            } else 0.0
            
            ProfitLossReport(
                projectId = projectId,
                projectName = projectPL.projectName,
                totalIncome = projectPL.totalIncome,
                totalExpense = projectPL.totalExpense,
                wagesCost = wagesCost,
                materialsCost = materialsCost,
                otherCosts = otherCosts,
                netProfit = projectPL.netProfit,
                profitMargin = profitMargin
            )
        }
    }
    
    /**
     * Get all projects P&L summary
     */
    suspend fun getAllProjectsProfitLoss(): Resource<List<ProjectProfitLoss>> = safeCall {
        transactionDao.getProjectProfitLossSummary()
    }
    
    // =====================================================
    // JOURNAL ENTRY OPERATIONS
    // =====================================================
    
    fun getAllJournalEntries(): Flow<List<JournalEntry>> =
        journalEntryDao.getAllJournalEntries()
    
    fun getJournalEntriesWithDetails(): Flow<List<JournalEntryWithDetails>> =
        journalEntryDao.getAllJournalEntriesWithDetails()
    
    fun getJournalEntriesInRange(startDate: String, endDate: String): Flow<List<JournalEntryWithDetails>> =
        journalEntryDao.getJournalEntriesInRange(startDate, endDate)
    
    fun getJournalEntriesByAccount(accountId: Int): Flow<List<JournalEntry>> =
        journalEntryDao.getJournalEntriesByAccount(accountId)
    
    // =====================================================
    // BANK STATEMENT GENERATION
    // =====================================================
    
    /**
     * Generate comprehensive bank statement data
     * ينشئ بيانات كشف الحساب البنكي الشامل
     * 
     * @param accountId Target account ID
     * @param startDate Period start (format: yyyy-MM-dd)
     * @param endDate Period end (format: yyyy-MM-dd)
     * @param includeAnalytics Whether to include analytics data
     */
    suspend fun generateBankStatementData(
        accountId: Int,
        startDate: String,
        endDate: String,
        includeAnalytics: Boolean = true
    ): Resource<com.bashmaqawa.pdf.models.BankStatementData> {
        val account = accountDao.getAccountById(accountId)
            ?: return Resource.Error("الحساب غير موجود", errorCode = ErrorCode.ACCOUNT_NOT_FOUND)
        
        return safeCall {
            val now = java.time.LocalDateTime.now()
            val periodStartDate = java.time.LocalDate.parse(startDate)
            val periodEndDate = java.time.LocalDate.parse(endDate)
            
            // Calculate opening balance (balance before the period start)
            val openingBalance = calculateOpeningBalance(accountId, startDate)
            
            // Get transactions with running balance
            val transactions = getTransactionsWithRunningBalance(accountId, startDate, endDate, openingBalance)
            
            // Calculate totals
            var totalDebits = 0.0
            var totalCredits = 0.0
            transactions.forEach { txn ->
                totalDebits += txn.debitAmount
                totalCredits += txn.creditAmount
            }
            
            val closingBalance = openingBalance + totalCredits - totalDebits
            
            // Determine credit/debit indicator for opening balance
            val openingIndicator = if (openingBalance >= 0) 
                com.bashmaqawa.pdf.models.CreditDebitIndicator.CREDIT 
            else 
                com.bashmaqawa.pdf.models.CreditDebitIndicator.DEBIT
            
            val closingIndicator = if (closingBalance >= 0) 
                com.bashmaqawa.pdf.models.CreditDebitIndicator.CREDIT 
            else 
                com.bashmaqawa.pdf.models.CreditDebitIndicator.DEBIT
            
            // Generate analytics if requested
            val analytics = if (includeAnalytics && transactions.isNotEmpty()) {
                generateStatementAnalytics(transactions, openingBalance, periodStartDate, periodEndDate)
            } else null
            
            // Generate unique statement ID
            val statementId = "BS-${account.id}-${startDate.replace("-", "")}-${endDate.replace("-", "")}"
            
            com.bashmaqawa.pdf.models.BankStatementData(
                statementId = statementId,
                sequenceNumber = 1,
                accountId = account.id,
                accountName = account.name,
                accountNumber = account.accountNumber,
                iban = null,
                bankName = account.bankName,
                branchName = null,
                branchCode = null,
                accountType = account.type?.name ?: "UNKNOWN",
                currency = "EGP",
                periodStart = periodStartDate,
                periodEnd = periodEndDate,
                generationDate = now,
                openingBalance = com.bashmaqawa.pdf.models.BalanceInfo(
                    amount = kotlin.math.abs(openingBalance),
                    type = com.bashmaqawa.pdf.models.BalanceType.OPENING,
                    date = periodStartDate,
                    creditDebitIndicator = openingIndicator
                ),
                closingBalance = com.bashmaqawa.pdf.models.BalanceInfo(
                    amount = kotlin.math.abs(closingBalance),
                    type = com.bashmaqawa.pdf.models.BalanceType.CLOSING,
                    date = periodEndDate,
                    creditDebitIndicator = closingIndicator
                ),
                availableBalance = null,
                totalDebits = totalDebits,
                totalCredits = totalCredits,
                transactionCount = transactions.size,
                transactions = transactions,
                analytics = analytics,
                companyInfo = null
            )
        }
    }
    
    /**
     * Calculate opening balance for account at specific date
     * يحسب الرصيد الافتتاحي للحساب في تاريخ محدد
     * 
     * This sums all transactions affecting the account BEFORE the start date.
     */
    suspend fun calculateOpeningBalance(accountId: Int, upToDate: String): Double {
        // Get current account balance
        val currentBalance = accountDao.getAccountBalance(accountId) ?: 0.0
        
        // Get all transactions from the start date to now and reverse them
        val transactions = transactionDao.getTransactionsByAccountId(accountId).first()
        
        var balanceChange = 0.0
        for (txn in transactions) {
            val txnDate = txn.date ?: continue
            if (txnDate >= upToDate) {
                // This transaction is in or after the period, reverse its effect
                val amount = txn.amount ?: 0.0
                when (txn.type) {
                    TransactionType.INCOME -> {
                        if (txn.effectiveSourceAccountId == accountId) {
                            balanceChange -= amount // Reverse income
                        }
                    }
                    TransactionType.EXPENSE -> {
                        if (txn.effectiveSourceAccountId == accountId) {
                            balanceChange += amount // Reverse expense
                        }
                    }
                    TransactionType.TRANSFER -> {
                        if (txn.effectiveSourceAccountId == accountId) {
                            balanceChange += amount // Reverse source
                        }
                        if (txn.destinationAccountId == accountId) {
                            balanceChange -= amount // Reverse destination
                        }
                    }
                    null -> { /* No-op */ }
                }
            }
        }
        
        return currentBalance + balanceChange
    }
    
    /**
     * Get transactions with running balance calculation
     * يحصل على المعاملات مع حساب الرصيد التراكمي
     */
    private suspend fun getTransactionsWithRunningBalance(
        accountId: Int,
        startDate: String,
        endDate: String,
        openingBalance: Double
    ): List<com.bashmaqawa.pdf.models.StatementTransaction> {
        val transactions = transactionDao.getTransactionsInDateRange(startDate, endDate).first()
            .filter { txn -> 
                txn.effectiveSourceAccountId == accountId || txn.destinationAccountId == accountId 
            }
            .sortedBy { it.date }
        
        val result = mutableListOf<com.bashmaqawa.pdf.models.StatementTransaction>()
        var runningBalance = openingBalance
        var sequenceNumber = 1
        
        for (txn in transactions) {
            if (txn.transactionState == TransactionState.VOID) continue
            
            val amount = txn.amount ?: 0.0
            val isCredit: Boolean
            
            // Determine if this is a credit or debit for THIS account
            when (txn.type) {
                TransactionType.INCOME -> {
                    isCredit = txn.effectiveSourceAccountId == accountId
                    if (isCredit) runningBalance += amount
                }
                TransactionType.EXPENSE -> {
                    isCredit = false
                    if (txn.effectiveSourceAccountId == accountId) runningBalance -= amount
                }
                TransactionType.TRANSFER -> {
                    isCredit = txn.destinationAccountId == accountId
                    if (txn.effectiveSourceAccountId == accountId) runningBalance -= amount
                    if (txn.destinationAccountId == accountId) runningBalance += amount
                }
                null -> {
                    isCredit = false
                }
            }
            
            val txnDate = java.time.LocalDate.parse(txn.date ?: startDate)
            
            val status = when (txn.transactionState) {
                TransactionState.PENDING -> com.bashmaqawa.pdf.models.StatementTransactionStatus.PENDING
                TransactionState.CLEARED -> com.bashmaqawa.pdf.models.StatementTransactionStatus.CLEARED
                TransactionState.VOID -> com.bashmaqawa.pdf.models.StatementTransactionStatus.VOID
            }
            
            result.add(
                com.bashmaqawa.pdf.models.StatementTransaction(
                    id = txn.id,
                    sequenceNumber = sequenceNumber++,
                    valueDate = txnDate,
                    bookingDate = txnDate,
                    transactionType = txn.type?.name ?: "UNKNOWN",
                    description = txn.description ?: "",
                    narrativeText = null,
                    amount = amount,
                    creditDebitIndicator = if (isCredit) 
                        com.bashmaqawa.pdf.models.CreditDebitIndicator.CREDIT 
                    else 
                        com.bashmaqawa.pdf.models.CreditDebitIndicator.DEBIT,
                    runningBalance = runningBalance,
                    referenceNumber = txn.referenceNumber,
                    checkNumber = null,
                    counterpartyName = null,
                    counterpartyAccount = null,
                    category = txn.category,
                    subCategory = null,
                    status = status,
                    isReconciled = txn.isReconciled
                )
            )
        }
        
        return result
    }
    
    /**
     * Generate statement analytics for period
     * ينشئ تحليلات الكشف للفترة
     */
    private fun generateStatementAnalytics(
        transactions: List<com.bashmaqawa.pdf.models.StatementTransaction>,
        openingBalance: Double,
        periodStart: java.time.LocalDate,
        periodEnd: java.time.LocalDate
    ): com.bashmaqawa.pdf.models.StatementAnalytics {
        if (transactions.isEmpty()) {
            return com.bashmaqawa.pdf.models.StatementAnalytics(
                averageDailyBalance = openingBalance,
                highestBalance = openingBalance,
                lowestBalance = openingBalance
            )
        }
        
        // Calculate daily balances
        val dailyBalances = mutableListOf<com.bashmaqawa.pdf.models.DailyBalance>()
        var currentBalance = openingBalance
        var currentDate = periodStart
        var transactionIdx = 0
        
        while (!currentDate.isAfter(periodEnd)) {
            // Apply all transactions for this date
            while (transactionIdx < transactions.size && 
                   transactions[transactionIdx].bookingDate == currentDate) {
                currentBalance = transactions[transactionIdx].runningBalance
                transactionIdx++
            }
            
            dailyBalances.add(com.bashmaqawa.pdf.models.DailyBalance(currentDate, currentBalance))
            currentDate = currentDate.plusDays(1)
        }
        
        // Calculate statistics
        val balances = dailyBalances.map { it.closingBalance }
        val averageBalance = balances.average()
        val highestBalance = balances.maxOrNull() ?: openingBalance
        val lowestBalance = balances.minOrNull() ?: openingBalance
        
        // Find largest debit and credit
        val debits = transactions.filter { it.creditDebitIndicator == com.bashmaqawa.pdf.models.CreditDebitIndicator.DEBIT }
        val credits = transactions.filter { it.creditDebitIndicator == com.bashmaqawa.pdf.models.CreditDebitIndicator.CREDIT }
        
        val largestDebit = debits.maxByOrNull { it.amount }?.let { txn ->
            com.bashmaqawa.pdf.models.TransactionSummary(txn.description, txn.amount, txn.bookingDate)
        }
        
        val largestCredit = credits.maxByOrNull { it.amount }?.let { txn ->
            com.bashmaqawa.pdf.models.TransactionSummary(txn.description, txn.amount, txn.bookingDate)
        }
        
        // Category breakdown
        val categoryBreakdown = transactions
            .groupBy { it.category ?: "أخرى" }
            .map { (category, txns) ->
                val total = txns.sumOf { it.amount }
                val totalAllCategories = transactions.sumOf { it.amount }
                com.bashmaqawa.pdf.models.CategoryAnalysis(
                    category = category,
                    totalAmount = total,
                    transactionCount = txns.size,
                    percentage = if (totalAllCategories > 0) (total / totalAllCategories * 100).toFloat() else 0f
                )
            }
            .sortedByDescending { it.totalAmount }
        
        return com.bashmaqawa.pdf.models.StatementAnalytics(
            averageDailyBalance = averageBalance,
            highestBalance = highestBalance,
            lowestBalance = lowestBalance,
            largestDebit = largestDebit,
            largestCredit = largestCredit,
            categoryBreakdown = categoryBreakdown,
            dailyBalanceTrend = dailyBalances
        )
    }
}
