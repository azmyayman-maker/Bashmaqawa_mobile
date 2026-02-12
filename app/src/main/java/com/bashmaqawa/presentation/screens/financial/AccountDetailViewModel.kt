package com.bashmaqawa.presentation.screens.financial

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bashmaqawa.data.database.dao.TransactionWithDetails
import com.bashmaqawa.data.database.entities.Account
import com.bashmaqawa.data.database.entities.TransactionState
import com.bashmaqawa.data.database.entities.TransactionType
import com.bashmaqawa.data.repository.FinancialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.math.abs

/**
 * Account Detail ViewModel
 * فيو موديل تفاصيل الحساب
 * 
 * Features:
 * - Concurrent data fetching for performance
 * - Sub-state management for minimal recomposition
 * - Time range and filter support
 * - Trend calculation based on historical data
 */
@HiltViewModel
class AccountDetailViewModel @Inject constructor(
    private val financialRepository: FinancialRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val accountId: Int = savedStateHandle.get<Int>("accountId") ?: 0
    
    private val _uiState = MutableStateFlow(AccountDetailUiState())
    val uiState: StateFlow<AccountDetailUiState> = _uiState.asStateFlow()
    
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    
    init {
        loadAccountData()
    }
    
    // =====================================================
    // DATA LOADING
    // =====================================================
    
    /**
     * Load all account data concurrently
     * تحميل جميع بيانات الحساب بشكل متزامن
     */
    fun loadAccountData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                coroutineScope {
                    val accountDeferred = async { loadAccountInfo() }
                    val transactionsDeferred = async { loadTransactions() }
                    val journalDeferred = async { loadJournalEntries() }
                    
                    awaitAll(accountDeferred, transactionsDeferred, journalDeferred)
                }
                
                // Load analytics after we have transactions
                loadAnalytics()
                
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = e.message ?: "حدث خطأ أثناء تحميل البيانات"
                    ) 
                }
            }
        }
    }
    
    /**
     * Load account information and calculate trend
     */
    private suspend fun loadAccountInfo() {
        val account = financialRepository.getAccountById(accountId) ?: return
        
        // Calculate trend based on last 30 days
        val today = LocalDate.now()
        val thirtyDaysAgo = today.minusDays(30)
        
        val transactions = financialRepository.getTransactionsByAccountId(accountId).first()
        val recentTransactions = transactions.filter { tx ->
            tx.date?.let { date ->
                try {
                    val txDate = LocalDate.parse(date.substring(0, 10))
                    txDate.isAfter(thirtyDaysAgo) || txDate.isEqual(thirtyDaysAgo)
                } catch (e: Exception) { false }
            } ?: false
        }
        
        // Calculate net change in last 30 days
        var netChange = 0.0
        recentTransactions.forEach { tx ->
            val amount = tx.amount ?: 0.0
            when (tx.type) {
                TransactionType.INCOME -> netChange += amount
                TransactionType.EXPENSE -> netChange -= amount
                TransactionType.TRANSFER -> {
                    if (tx.sourceAccountId == accountId) netChange -= amount
                    if (tx.destinationAccountId == accountId) netChange += amount
                }
                null -> {}
            }
        }
        
        val (trend, percentage) = when {
            netChange > 0 -> BalanceTrend.INCREASING to (netChange / maxOf(account.balance - netChange, 1.0) * 100)
            netChange < 0 -> BalanceTrend.DECREASING to (abs(netChange) / maxOf(account.balance + abs(netChange), 1.0) * 100)
            else -> BalanceTrend.STABLE to 0.0
        }
        
        // Calculate pending balance
        val pendingTransactions = transactions.filter { it.transactionState == TransactionState.PENDING }
        var pendingBalance = 0.0
        pendingTransactions.forEach { tx ->
            val amount = tx.amount ?: 0.0
            when (tx.type) {
                TransactionType.INCOME -> pendingBalance += amount
                TransactionType.EXPENSE -> pendingBalance -= amount
                TransactionType.TRANSFER -> {
                    if (tx.sourceAccountId == accountId) pendingBalance -= amount
                    if (tx.destinationAccountId == accountId) pendingBalance += amount
                }
                null -> {}
            }
        }
        
        _uiState.update { state ->
            state.copy(
                accountState = state.accountState.copy(
                    account = account,
                    balanceTrend = trend,
                    trendPercentage = percentage,
                    lastUpdated = account.updatedAt,
                    pendingBalance = pendingBalance,
                    availableBalance = account.balance
                )
            )
        }
    }
    
    /**
     * Load transactions for this account
     */
    private suspend fun loadTransactions() {
        _uiState.update { it.copy(transactionState = it.transactionState.copy(isLoading = true)) }
        
        val transactions = financialRepository.getTransactionsByAccountId(accountId).first()
        val currentFilter = _uiState.value.transactionState.filter
        val searchQuery = _uiState.value.transactionState.searchQuery
        
        // Convert to TransactionWithDetails
        val transactionsWithDetails = transactions.map { tx ->
            TransactionWithDetails(
                id = tx.id,
                projectId = tx.projectId,
                accountId = tx.accountId,
                sourceAccountId = tx.sourceAccountId,
                destinationAccountId = tx.destinationAccountId,
                amount = tx.amount,
                category = tx.category,
                type = tx.type?.name,
                transactionState = tx.transactionState.name,
                description = tx.description,
                date = tx.date,
                costCenterId = tx.costCenterId,
                costCenterName = null,
                workerId = tx.workerId,
                workerName = null,
                projectName = null,
                accountName = null,
                sourceAccountName = null,
                destinationAccountName = null,
                invoiceImage = tx.invoiceImage,
                referenceNumber = tx.referenceNumber,
                paymentMethod = tx.paymentMethod,
                isReconciled = tx.isReconciled ?: false,
                createdAt = tx.createdAt,
                modifiedAt = tx.modifiedAt
            )
        }
        
        val filteredTransactions = filterTransactions(transactionsWithDetails, currentFilter, searchQuery)
        val groupedTransactions = groupTransactionsByDate(filteredTransactions)
        
        _uiState.update { state ->
            state.copy(
                transactionState = state.transactionState.copy(
                    transactions = transactionsWithDetails,
                    groupedTransactions = groupedTransactions,
                    isLoading = false
                )
            )
        }
    }
    
    /**
     * Load journal entries for this account
     */
    private suspend fun loadJournalEntries() {
        _uiState.update { it.copy(journalState = it.journalState.copy(isLoading = true)) }
        
        val journalEntries = financialRepository.getJournalEntriesByAccount(accountId).first()
        
        // Get account names for each entry
        val entriesWithDetails = journalEntries.map { entry ->
            val debitAccount = financialRepository.getAccountById(entry.debitAccountId)
            val creditAccount = financialRepository.getAccountById(entry.creditAccountId)
            JournalEntryWithDetails(
                entry = entry,
                debitAccountName = debitAccount?.name ?: "غير معروف",
                creditAccountName = creditAccount?.name ?: "غير معروف"
            )
        }
        
        _uiState.update { state ->
            state.copy(
                journalState = state.journalState.copy(
                    journalEntries = entriesWithDetails,
                    isLoading = false
                )
            )
        }
    }
    
    /**
     * Load analytics data for selected time range
     */
    private suspend fun loadAnalytics() {
        _uiState.update { it.copy(analyticsState = it.analyticsState.copy(isLoadingChart = true)) }
        
        val timeRange = _uiState.value.analyticsState.selectedTimeRange
        val today = LocalDate.now()
        val startDate = if (timeRange.days > 0) today.minusDays(timeRange.days.toLong()) else LocalDate.of(2020, 1, 1)
        
        val transactions = _uiState.value.transactionState.transactions.filter { tx ->
            tx.date?.let { date ->
                try {
                    val txDate = LocalDate.parse(date.substring(0, 10))
                    txDate.isAfter(startDate) || txDate.isEqual(startDate)
                } catch (e: Exception) { false }
            } ?: false
        }
        
        // Calculate metrics
        var totalInflow = 0.0
        var totalOutflow = 0.0
        var largestTransaction = 0.0
        
        transactions.forEach { tx ->
            val amount = tx.amount ?: 0.0
            when (tx.type) {
                "INCOME" -> {
                    totalInflow += amount
                    if (amount > largestTransaction) largestTransaction = amount
                }
                "EXPENSE" -> {
                    totalOutflow += amount
                    if (amount > largestTransaction) largestTransaction = amount
                }
                "TRANSFER" -> {
                    if (tx.destinationAccountId == accountId) {
                        totalInflow += amount
                    }
                    if (tx.sourceAccountId == accountId) {
                        totalOutflow += amount
                    }
                    if (amount > largestTransaction) largestTransaction = amount
                }
                null -> {}
            }
        }
        
        val transactionCount = transactions.size
        val averageTransactionSize = if (transactionCount > 0) (totalInflow + totalOutflow) / transactionCount else 0.0
        
        // Generate chart data - convert back to Transaction entities for the chart function
        val txEntities = convertToTransactionEntities(transactions)
        val chartData = generateChartData(txEntities, startDate, today)
        
        _uiState.update { state ->
            state.copy(
                analyticsState = state.analyticsState.copy(
                    totalInflow = totalInflow,
                    totalOutflow = totalOutflow,
                    netChange = totalInflow - totalOutflow,
                    averageTransactionSize = averageTransactionSize,
                    transactionCount = transactionCount,
                    largestTransaction = largestTransaction,
                    chartData = chartData,
                    isLoadingChart = false
                )
            )
        }
    }
    
    /**
     * Convert TransactionWithDetails to Transaction entities for chart data
     */
    private fun convertToTransactionEntities(txDetails: List<TransactionWithDetails>): List<com.bashmaqawa.data.database.entities.Transaction> {
        return txDetails.map { tx ->
            com.bashmaqawa.data.database.entities.Transaction(
                id = tx.id,
                sourceAccountId = tx.sourceAccountId,
                destinationAccountId = tx.destinationAccountId,
                amount = tx.amount,
                category = tx.category,
                type = when (tx.type) {
                    "INCOME" -> TransactionType.INCOME
                    "EXPENSE" -> TransactionType.EXPENSE
                    "TRANSFER" -> TransactionType.TRANSFER
                    else -> null
                },
                transactionState = when (tx.transactionState) {
                    "PENDING" -> TransactionState.PENDING
                    "CLEARED" -> TransactionState.CLEARED
                    "VOID" -> TransactionState.VOID
                    else -> TransactionState.PENDING
                },
                description = tx.description,
                date = tx.date,
                projectId = tx.projectId,
                workerId = tx.workerId,
                accountId = tx.accountId,
                costCenterId = tx.costCenterId,
                invoiceImage = tx.invoiceImage,
                referenceNumber = tx.referenceNumber,
                paymentMethod = tx.paymentMethod,
                isReconciled = tx.isReconciled,
                createdAt = tx.createdAt,
                modifiedAt = tx.modifiedAt
            )
        }
    }
    
    /**
     * Generate chart data points for balance trend
     */
    private fun generateChartData(
        transactions: List<com.bashmaqawa.data.database.entities.Transaction>,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<ChartDataPoint> {
        val account = _uiState.value.accountState.account ?: return emptyList()
        val chartData = mutableListOf<ChartDataPoint>()
        
        // Group transactions by date
        val transactionsByDate = transactions.groupBy { tx ->
            tx.date?.substring(0, 10) ?: ""
        }
        
        // Calculate running balance going backwards from current balance
        var runningBalance = account.balance
        val daysBetween = ChronoUnit.DAYS.between(startDate, endDate).toInt()
        
        // Create data points (simplified - just showing balance at intervals)
        val interval = maxOf(daysBetween / 10, 1) // Show ~10 data points
        
        var currentDate = endDate
        while (!currentDate.isBefore(startDate)) {
            chartData.add(0, ChartDataPoint(
                date = currentDate.format(dateFormatter),
                balance = runningBalance,
                label = currentDate.dayOfMonth.toString()
            ))
            
            // Adjust balance for transactions on this date
            val dateStr = currentDate.format(dateFormatter)
            transactionsByDate[dateStr]?.forEach { tx ->
                val amount = tx.amount ?: 0.0
                when (tx.type) {
                    TransactionType.INCOME -> runningBalance -= amount
                    TransactionType.EXPENSE -> runningBalance += amount
                    TransactionType.TRANSFER -> {
                        if (tx.sourceAccountId == accountId) runningBalance += amount
                        if (tx.destinationAccountId == accountId) runningBalance -= amount
                    }
                    null -> {}
                }
            }
            
            currentDate = currentDate.minusDays(interval.toLong())
        }
        
        return chartData
    }
    
    // =====================================================
    // FILTERING & SEARCHING
    // =====================================================
    
    /**
     * Filter transactions by type and search query
     */
    private fun filterTransactions(
        transactions: List<TransactionWithDetails>,
        filter: AccountTransactionFilter,
        searchQuery: String
    ): List<TransactionWithDetails> {
        return transactions.filter { tx ->
            // Filter by type (tx.type is String? in TransactionWithDetails)
            val matchesFilter = when (filter) {
                AccountTransactionFilter.ALL -> true
                AccountTransactionFilter.INCOME -> tx.type == "INCOME"
                AccountTransactionFilter.EXPENSE -> tx.type == "EXPENSE"
                AccountTransactionFilter.TRANSFER -> tx.type == "TRANSFER"
            }
            
            // Filter by search query
            val matchesSearch = if (searchQuery.isBlank()) true else {
                tx.description?.contains(searchQuery, ignoreCase = true) == true ||
                tx.category?.contains(searchQuery, ignoreCase = true) == true ||
                tx.amount?.toString()?.contains(searchQuery) == true
            }
            
            matchesFilter && matchesSearch
        }
    }
    
    /**
     * Group transactions by date with smart headers
     * تجميع المعاملات حسب التاريخ مع رؤوس ذكية
     */
    private fun groupTransactionsByDate(
        transactions: List<TransactionWithDetails>
    ): Map<String, List<TransactionWithDetails>> {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val weekStart = today.minusDays(7)
        val monthStart = today.minusDays(30)
        
        return transactions
            .sortedByDescending { it.date }
            .groupBy { tx ->
                val dateStr = tx.date?.substring(0, 10) ?: return@groupBy "أخرى"
                try {
                    val txDate = LocalDate.parse(dateStr)
                    when {
                        txDate.isEqual(today) -> "اليوم"
                        txDate.isEqual(yesterday) -> "أمس"
                        txDate.isAfter(weekStart) -> "هذا الأسبوع"
                        txDate.isAfter(monthStart) -> "هذا الشهر"
                        else -> txDate.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
                    }
                } catch (e: Exception) {
                    "أخرى"
                }
            }
    }
    
    // =====================================================
    // UI ACTIONS
    // =====================================================
    
    /**
     * Update selected tab
     */
    fun selectTab(tab: AccountDetailTab) {
        _uiState.update { it.copy(selectedTab = tab) }
        
        // Load related entities if needed
        if (tab == AccountDetailTab.RELATED && _uiState.value.relatedState.linkedWorkers.isEmpty()) {
            loadRelatedEntities()
        }
    }
    
    /**
     * Update time range for analytics
     */
    fun updateTimeRange(range: AccountTimeRange) {
        _uiState.update { state ->
            state.copy(
                analyticsState = state.analyticsState.copy(selectedTimeRange = range)
            )
        }
        viewModelScope.launch {
            loadAnalytics()
        }
    }
    
    /**
     * Update transaction filter
     */
    fun updateFilter(filter: AccountTransactionFilter) {
        val transactions = _uiState.value.transactionState.transactions
        val searchQuery = _uiState.value.transactionState.searchQuery
        val filtered = filterTransactions(transactions, filter, searchQuery)
        val grouped = groupTransactionsByDate(filtered)
        
        _uiState.update { state ->
            state.copy(
                transactionState = state.transactionState.copy(
                    filter = filter,
                    groupedTransactions = grouped
                )
            )
        }
    }
    
    /**
     * Update search query
     */
    fun updateSearchQuery(query: String) {
        val transactions = _uiState.value.transactionState.transactions
        val filter = _uiState.value.transactionState.filter
        val filtered = filterTransactions(transactions, filter, query)
        val grouped = groupTransactionsByDate(filtered)
        
        _uiState.update { state ->
            state.copy(
                transactionState = state.transactionState.copy(
                    searchQuery = query,
                    groupedTransactions = grouped
                )
            )
        }
    }
    
    /**
     * Toggle transaction expansion
     */
    fun toggleTransactionExpansion(transactionId: Int) {
        _uiState.update { state ->
            val currentExpanded = state.transactionState.expandedTransactionId
            state.copy(
                transactionState = state.transactionState.copy(
                    expandedTransactionId = if (currentExpanded == transactionId) null else transactionId
                )
            )
        }
    }
    
    /**
     * Toggle journal entry expansion
     */
    fun toggleJournalExpansion(entryId: Int) {
        _uiState.update { state ->
            val currentExpanded = state.journalState.expandedEntryId
            state.copy(
                journalState = state.journalState.copy(
                    expandedEntryId = if (currentExpanded == entryId) null else entryId
                )
            )
        }
    }
    
    /**
     * Show/hide edit bottom sheet
     */
    fun toggleEditSheet(show: Boolean) {
        _uiState.update { it.copy(showEditSheet = show) }
    }
    
    /**
     * Show/hide delete confirmation
     */
    fun toggleDeleteConfirmation(show: Boolean) {
        _uiState.update { it.copy(showDeleteConfirmation = show) }
    }
    
    /**
     * Toggle FAB menu
     */
    fun toggleFabMenu(show: Boolean) {
        _uiState.update { it.copy(showFabMenu = show) }
    }
    
    // =====================================================
    // ACCOUNT OPERATIONS
    // =====================================================
    
    /**
     * Update account details
     */
    fun updateAccount(
        name: String,
        details: String?,
        bankName: String?,
        accountNumber: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val currentAccount = _uiState.value.accountState.account ?: return@launch
                
                val updatedAccount = currentAccount.copy(
                    name = name,
                    details = details,
                    bankName = bankName,
                    accountNumber = accountNumber,
                    updatedAt = java.time.LocalDateTime.now().toString()
                )
                
                financialRepository.updateAccount(updatedAccount)
                
                _uiState.update { state ->
                    state.copy(
                        accountState = state.accountState.copy(account = updatedAccount),
                        showEditSheet = false,
                        successMessage = "تم تحديث الحساب بنجاح"
                    )
                }
                
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "حدث خطأ أثناء تحديث الحساب")
            }
        }
    }
    
    /**
     * Delete account with safeguards
     */
    fun deleteAccount(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val account = _uiState.value.accountState.account ?: return@launch
                
                // Check safeguards
                if (account.isSystemAccount) {
                    onError("لا يمكن حذف حساب النظام")
                    return@launch
                }
                
                if (account.balance != 0.0) {
                    onError("لا يمكن حذف حساب له رصيد")
                    return@launch
                }
                
                val pendingTransactions = _uiState.value.transactionState.transactions
                    .filter { it.transactionState == "PENDING" }
                
                if (pendingTransactions.isNotEmpty()) {
                    onError("لا يمكن حذف حساب له معاملات معلقة")
                    return@launch
                }
                
                // Deactivate instead of hard delete for audit trail
                financialRepository.deactivateAccount(accountId)
                
                _uiState.update { it.copy(showDeleteConfirmation = false) }
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "حدث خطأ أثناء حذف الحساب")
            }
        }
    }
    
    /**
     * Load related entities (workers and projects)
     */
    private fun loadRelatedEntities() {
        viewModelScope.launch {
            _uiState.update { it.copy(relatedState = it.relatedState.copy(isLoading = true)) }
            
            try {
                val transactions = _uiState.value.transactionState.transactions
                
                // Group by worker
                val workerTransactions = transactions
                    .filter { it.workerId != null }
                    .groupBy { it.workerId!! }
                
                // Group by project
                val projectTransactions = transactions
                    .filter { it.projectId != null }
                    .groupBy { it.projectId!! }
                
                // For now, create simple info objects
                // In production, would fetch worker/project names from repositories
                val linkedWorkers = workerTransactions.map { (workerId, txList) ->
                    val totalAdvance = txList
                        .filter { it.category?.contains("سلف", ignoreCase = true) == true }
                        .sumOf { it.amount ?: 0.0 }
                    LinkedWorkerInfo(
                        workerId = workerId,
                        workerName = "عامل #$workerId",
                        outstandingAdvance = totalAdvance,
                        lastTransactionDate = txList.maxByOrNull { it.date ?: "" }?.date
                    )
                }
                
                val linkedProjects = projectTransactions.map { (projectId, txList) ->
                    LinkedProjectInfo(
                        projectId = projectId,
                        projectName = "مشروع #$projectId",
                        totalTransactions = txList.sumOf { it.amount ?: 0.0 },
                        transactionCount = txList.size
                    )
                }
                
                _uiState.update { state ->
                    state.copy(
                        relatedState = state.relatedState.copy(
                            linkedWorkers = linkedWorkers,
                            linkedProjects = linkedProjects,
                            isLoading = false
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(relatedState = it.relatedState.copy(isLoading = false)) }
            }
        }
    }
    
    // =====================================================
    // UTILITIES
    // =====================================================
    
    /**
     * Refresh all data
     */
    fun refresh() {
        loadAccountData()
    }
    
    /**
     * Clear success message
     */
    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
