package com.bashmaqawa.presentation.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bashmaqawa.core.DateRange
import com.bashmaqawa.data.database.dao.CategorySummary
import com.bashmaqawa.data.database.entities.Account
import com.bashmaqawa.data.database.entities.PayrollEntry
import com.bashmaqawa.data.database.entities.Project
import com.bashmaqawa.data.database.entities.Worker
import com.bashmaqawa.data.repository.FinancialRepository
import com.bashmaqawa.data.repository.WorkerRepository
import com.bashmaqawa.pdf.PdfReportEngine
import com.bashmaqawa.pdf.models.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Report ViewModel
 * ViewModel التقارير
 * 
 * Handles report generation logic with proper state management.
 * Integrates with repositories for data fetching and PdfReportEngine for generation.
 */
@HiltViewModel
class ReportViewModel @Inject constructor(
    private val pdfEngine: PdfReportEngine,
    private val financialRepository: FinancialRepository,
    private val workerRepository: WorkerRepository
) : ViewModel() {
    
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
    
    // ══════════════════════════════════════════════════════════════════════
    // UI STATE
    // ══════════════════════════════════════════════════════════════════════
    
    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()
    
    val progress: StateFlow<PdfGenerationProgress> = pdfEngine.progress
    
    // ══════════════════════════════════════════════════════════════════════
    // DATA LOADING
    // ══════════════════════════════════════════════════════════════════════
    
    init {
        loadAccounts()
        loadWorkers()
    }
    
    private fun loadAccounts() {
        viewModelScope.launch {
            financialRepository.getAllActiveAccounts().collect { accounts ->
                _uiState.update { it.copy(availableAccounts = accounts) }
            }
        }
    }
    
    private fun loadWorkers() {
        viewModelScope.launch {
            workerRepository.getAllWorkers().collect { workers ->
                _uiState.update { it.copy(availableWorkers = workers) }
            }
        }
    }
    
    // ══════════════════════════════════════════════════════════════════════
    // REPORT CONFIGURATION
    // ══════════════════════════════════════════════════════════════════════
    
    fun selectReportType(type: ReportType) {
        _uiState.update { it.copy(selectedReportType = type) }
    }
    
    fun selectAccount(account: Account?) {
        _uiState.update { it.copy(selectedAccount = account) }
    }
    
    fun selectProject(project: Project?) {
        _uiState.update { it.copy(selectedProject = project) }
    }
    
    fun selectWorker(worker: Worker?) {
        _uiState.update { it.copy(selectedWorker = worker) }
    }
    
    fun setDateRange(range: DateRange) {
        _uiState.update { it.copy(dateRange = range) }
    }
    
    fun setThisMonth() {
        setDateRange(DateRange.currentMonth())
    }
    
    fun setThisWeek() {
        setDateRange(DateRange.currentWeek())
    }
    
    fun setLast30Days() {
        setDateRange(DateRange.lastDays(30))
    }
    
    // ══════════════════════════════════════════════════════════════════════
    // REPORT GENERATION
    // ══════════════════════════════════════════════════════════════════════
    
    fun generateReport() {
        val state = _uiState.value
        val reportType = state.selectedReportType ?: return
        
        _uiState.update { it.copy(isGenerating = true, error = null) }
        
        viewModelScope.launch {
            try {
                val result = when (reportType) {
                    ReportType.ACCOUNT_STATEMENT -> generateAccountStatement()
                    ReportType.PROFIT_LOSS -> generateProfitLoss()
                    ReportType.TRANSACTION_LEDGER -> generateTransactionLedger()
                    ReportType.PAYROLL_SUMMARY -> generatePayrollReport()
                    ReportType.PROJECT_REPORT -> generateProjectReport()
                    ReportType.JOURNAL_ENTRIES -> generateJournalEntries()
                    ReportType.WORKER_REPORT -> generateWorkerReport()
                    ReportType.ANALYTICS_SUMMARY -> generateAnalytics()
                    ReportType.BANK_STATEMENT -> {
                        // Bank Statement has its own dedicated screen
                        PdfGenerationResult.Failure.NoData
                    }
                }
                
                handleResult(result)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        error = e.message ?: "خطأ غير متوقع"
                    )
                }
            }
        }
    }
    
    private fun handleResult(result: PdfGenerationResult) {
        when (result) {
            is PdfGenerationResult.Success -> {
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        generatedFile = result.file,
                        pageCount = result.pageCount,
                        generationTimeMs = result.generationTimeMs
                    )
                }
            }
            is PdfGenerationResult.Failure -> {
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        error = result.getMessage()
                    )
                }
            }
        }
    }
    
    // ══════════════════════════════════════════════════════════════════════
    // INDIVIDUAL REPORT GENERATORS
    // ══════════════════════════════════════════════════════════════════════
    
    private suspend fun generateAccountStatement(): PdfGenerationResult {
        val state = _uiState.value
        val account = state.selectedAccount
            ?: return PdfGenerationResult.Failure.NoData
        val range = state.dateRange
        
        // Fetch transactions for this account
        val transactions = financialRepository.getTransactionsByAccountId(account.id).first()
        
        // Filter by date range
        val filteredTransactions = transactions.filter { tx ->
            tx.date != null && range.contains(tx.date)
        }
        
        // Calculate balances
        var runningBalance = account.balance
        var totalDebits = 0.0
        var totalCredits = 0.0
        
        val transactionRows = filteredTransactions.map { tx ->
            val isDebit = tx.sourceAccountId == account.id
            val amount = tx.amount ?: 0.0
            val debit = if (isDebit) amount else 0.0
            val credit = if (!isDebit) amount else 0.0
            
            if (isDebit) {
                totalDebits += amount
            } else {
                totalCredits += amount
            }
            
            TransactionRow(
                id = tx.id,
                date = tx.date ?: "",
                description = tx.description ?: tx.category ?: "",
                category = tx.category,
                referenceNumber = tx.referenceNumber,
                debit = debit,
                credit = credit,
                balance = runningBalance,
                type = tx.type?.name ?: "",
                state = tx.transactionState?.name ?: ""
            )
        }
        
        val openingBalance = account.balance - totalCredits + totalDebits
        
        val data = AccountStatementData(
            accountId = account.id,
            accountName = account.name,
            accountCode = account.accountCode,
            accountType = account.type?.name,
            bankName = account.bankName,
            accountNumber = account.accountNumber,
            openingBalance = openingBalance,
            closingBalance = account.balance,
            totalDebits = totalDebits,
            totalCredits = totalCredits,
            transactions = transactionRows
        )
        
        val config = ReportConfig(
            reportType = ReportType.ACCOUNT_STATEMENT,
            dateRange = range,
            accountId = account.id
        )
        
        return pdfEngine.generateAccountStatement(data, config)
    }
    
    private suspend fun generateProfitLoss(): PdfGenerationResult {
        val state = _uiState.value
        val range = state.dateRange
        
        // Fetch category summaries (suspend functions return List, not Flow)
        val expenseSummary: List<CategorySummary> = financialRepository.getExpenseCategorySummary(
            range.startDateString,
            range.endDateString
        )
        
        val incomeSummary: List<CategorySummary> = financialRepository.getIncomeCategorySummary(
            range.startDateString,
            range.endDateString
        )
        
        val incomeCategories = incomeSummary.map { cs ->
            CategoryTotal(category = cs.category, amount = cs.total, transactionCount = cs.count)
        }
        val expenseCategories = expenseSummary.map { cs ->
            CategoryTotal(category = cs.category, amount = cs.total, transactionCount = cs.count)
        }
        
        val totalIncome = incomeCategories.sumOf { it.amount }
        val totalExpenses = expenseCategories.sumOf { it.amount }
        
        val data = ProfitLossData(
            periodStart = range.startDateString,
            periodEnd = range.endDateString,
            incomeCategories = incomeCategories,
            expenseCategories = expenseCategories,
            totalIncome = totalIncome,
            totalExpenses = totalExpenses,
            netProfit = totalIncome - totalExpenses
        )
        
        val config = ReportConfig(
            reportType = ReportType.PROFIT_LOSS,
            dateRange = range
        )
        
        return pdfEngine.generateProfitLossReport(data, config)
    }
    
    private suspend fun generateTransactionLedger(): PdfGenerationResult {
        val state = _uiState.value
        val range = state.dateRange
        
        val transactions = financialRepository.getTransactionsInDateRange(
            range.startDateString,
            range.endDateString
        ).first()
        
        val transactionRows = transactions.map { tx ->
            val amount = tx.amount ?: 0.0
            TransactionRow(
                id = tx.id,
                date = tx.date ?: "",
                description = tx.description ?: tx.category ?: "",
                category = tx.category,
                referenceNumber = tx.referenceNumber,
                debit = if (tx.type?.name == "EXPENSE") amount else 0.0,
                credit = if (tx.type?.name == "INCOME") amount else 0.0,
                balance = 0.0, // Not applicable for ledger
                type = tx.type?.name ?: "",
                state = tx.transactionState?.name ?: ""
            )
        }
        
        val config = ReportConfig(
            reportType = ReportType.TRANSACTION_LEDGER,
            dateRange = range
        )
        
        return pdfEngine.generateTransactionLedger(
            transactionRows,
            range.startDateString,
            range.endDateString,
            config
        )
    }
    
    private suspend fun generatePayrollReport(): PdfGenerationResult {
        val state = _uiState.value
        val range = state.dateRange
        
        // Fetch payroll entries with worker info (PayrollWithWorker is flat)
        val payrollEntries = financialRepository.getPayrollEntriesWithWorker().first()
        
        val payrollRows = payrollEntries.map { entry ->
            PayrollRow(
                workerId = entry.workerId,
                workerName = entry.workerName,
                category = entry.projectName, // Use project as category
                daysWorked = entry.daysPresent + (entry.halfDays * 0.5),
                dailyRate = entry.dailyRate,
                grossWage = entry.grossWage,
                deductions = entry.deductions,
                advances = entry.advancesDeducted,
                netPay = entry.netWage,
                status = entry.status
            )
        }
        
        val data = PayrollReportData(
            periodStart = range.startDateString,
            periodEnd = range.endDateString,
            entries = payrollRows,
            totalGrossWages = payrollRows.sumOf { it.grossWage },
            totalDeductions = payrollRows.sumOf { it.deductions },
            totalAdvances = payrollRows.sumOf { it.advances },
            totalNetPay = payrollRows.sumOf { it.netPay }
        )
        
        val config = ReportConfig(
            reportType = ReportType.PAYROLL_SUMMARY,
            dateRange = range
        )
        
        return pdfEngine.generatePayrollReport(data, config)
    }
    
    private suspend fun generateProjectReport(): PdfGenerationResult {
        val state = _uiState.value
        val project = state.selectedProject
            ?: return PdfGenerationResult.Failure.NoData
        
        // Get P&L for project using the repository method that returns Resource
        return when (val result = financialRepository.getProjectProfitLoss(project.id)) {
            is com.bashmaqawa.core.Resource.Success -> {
                val pl = result.data
                val data = ProjectReportData(
                    projectId = project.id,
                    projectName = project.name,
                    clientName = project.clientName,
                    location = project.location,
                    startDate = project.startDate,
                    endDate = project.endDate,
                    status = project.status.name,
                    totalIncome = pl.totalIncome,
                    totalExpenses = pl.totalExpense, // Note: totalExpense not totalExpenses
                    netProfit = pl.netProfit,
                    incomeBreakdown = emptyList(), // Simplified - no breakdown in basic P&L
                    expenseBreakdown = emptyList()
                )
                
                val config = ReportConfig(
                    reportType = ReportType.PROJECT_REPORT,
                    projectId = project.id
                )
                
                pdfEngine.generateProjectReport(data, config)
            }
            is com.bashmaqawa.core.Resource.Error -> {
                PdfGenerationResult.Failure.RenderError(0, Throwable(result.message))
            }
            is com.bashmaqawa.core.Resource.Loading -> {
                PdfGenerationResult.Failure.NoData
            }
        }
    }
    
    private suspend fun generateJournalEntries(): PdfGenerationResult {
        val state = _uiState.value
        val range = state.dateRange
        
        val entries = financialRepository.getJournalEntriesInRange(
            range.startDateString,
            range.endDateString
        ).first()
        
        val journalRows = entries.map { entry ->
            JournalEntryRow(
                id = entry.id,
                date = entry.entryDate, // JournalEntry uses entryDate
                description = entry.description,
                debitAccountName = "حساب #${entry.debitAccountId}",
                creditAccountName = "حساب #${entry.creditAccountId}",
                amount = entry.amount,
                referenceType = entry.referenceType?.toString(),
                isReversing = entry.isReversing
            )
        }
        
        val config = ReportConfig(
            reportType = ReportType.JOURNAL_ENTRIES,
            dateRange = range
        )
        
        return pdfEngine.generateJournalEntriesReport(
            journalRows,
            range.startDateString,
            range.endDateString,
            config
        )
    }
    
    private suspend fun generateWorkerReport(): PdfGenerationResult {
        val state = _uiState.value
        val worker = state.selectedWorker
            ?: return PdfGenerationResult.Failure.NoData
        
        val range = state.dateRange
        
        // Get payroll entries for this worker
        val payrollEntries: List<PayrollEntry> = financialRepository.getPayrollEntriesByWorkerId(worker.id).first()
        
        val payrollRows = payrollEntries.map { entry ->
            PayrollRow(
                workerId = entry.workerId,
                workerName = worker.name,
                category = worker.role, // Use role as category, categoryId needs join
                daysWorked = entry.daysPresent + (entry.halfDays * 0.5),
                dailyRate = entry.dailyRate,
                grossWage = entry.grossWage,
                deductions = entry.deductions,
                advances = entry.advancesDeducted,
                netPay = entry.netWage,
                status = entry.status.name
            )
        }
        
        val data = PayrollReportData(
            periodStart = range.startDateString,
            periodEnd = range.endDateString,
            entries = payrollRows,
            totalGrossWages = payrollRows.sumOf { it.grossWage },
            totalDeductions = payrollRows.sumOf { it.deductions },
            totalAdvances = payrollRows.sumOf { it.advances },
            totalNetPay = payrollRows.sumOf { it.netPay }
        )
        
        val config = ReportConfig(
            reportType = ReportType.WORKER_REPORT,
            dateRange = range,
            workerId = worker.id
        )
        
        return pdfEngine.generatePayrollReport(data, config)
    }
    
    private suspend fun generateAnalytics(): PdfGenerationResult {
        val state = _uiState.value
        val range = state.dateRange
        
        val totalIncome = financialRepository.getTotalIncomeInRange(
            range.startDateString, 
            range.endDateString
        )
        
        val totalExpense = financialRepository.getTotalExpenseInRange(
            range.startDateString, 
            range.endDateString
        )
        
        val netProfit = totalIncome - totalExpense
        
        val periodTitle = "${range.startDate.format(dateFormatter)} - ${range.endDate.format(dateFormatter)}"
        
        return pdfEngine.generateAnalyticsSummary(
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            netProfit = netProfit,
            activeProjects = 0, // Placeholder
            totalWorkers = 0,   // Placeholder
            periodTitle = periodTitle
        )
    }
    
    // ══════════════════════════════════════════════════════════════════════
    // FILE OPERATIONS
    // ══════════════════════════════════════════════════════════════════════
    
    fun shareReport() {
        _uiState.value.generatedFile?.let { file ->
            pdfEngine.sharePdf(file)
        }
    }
    
    fun openReport() {
        _uiState.value.generatedFile?.let { file ->
            pdfEngine.openPdf(file)
        }
    }
    
    fun cancelGeneration() {
        pdfEngine.cancelGeneration()
        _uiState.update { it.copy(isGenerating = false) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun clearResult() {
        _uiState.update { 
            it.copy(
                generatedFile = null,
                pageCount = 0,
                generationTimeMs = 0
            )
        }
    }
}

/**
 * Report UI State
 */
data class ReportUiState(
    val selectedReportType: ReportType? = null,
    val selectedAccount: Account? = null,
    val selectedProject: Project? = null,
    val selectedWorker: Worker? = null,
    val dateRange: DateRange = DateRange.currentMonth(),
    val availableAccounts: List<Account> = emptyList(),
    val availableProjects: List<Project> = emptyList(),
    val availableWorkers: List<Worker> = emptyList(),
    val isGenerating: Boolean = false,
    val error: String? = null,
    val generatedFile: File? = null,
    val pageCount: Int = 0,
    val generationTimeMs: Long = 0
) {
    val hasResult: Boolean get() = generatedFile != null
    
    val requiresAccountSelection: Boolean
        get() = selectedReportType == ReportType.ACCOUNT_STATEMENT
    
    val requiresProjectSelection: Boolean
        get() = selectedReportType == ReportType.PROJECT_REPORT
    
    val requiresWorkerSelection: Boolean
        get() = selectedReportType == ReportType.WORKER_REPORT
    
    val canGenerate: Boolean
        get() = when {
            selectedReportType == null -> false
            requiresAccountSelection && selectedAccount == null -> false
            requiresProjectSelection && selectedProject == null -> false
            requiresWorkerSelection && selectedWorker == null -> false
            else -> true
        }
}
