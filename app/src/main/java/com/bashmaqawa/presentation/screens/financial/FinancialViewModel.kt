package com.bashmaqawa.presentation.screens.financial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bashmaqawa.data.database.dao.CategorySummary
import com.bashmaqawa.data.database.dao.TransactionWithDetails
import com.bashmaqawa.data.database.entities.Account
import com.bashmaqawa.data.database.entities.AccountType
import com.bashmaqawa.data.database.entities.Transaction
import com.bashmaqawa.data.database.entities.TransactionType
import com.bashmaqawa.data.repository.FinancialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

// =====================================================
// STATE CLASSES - Separate sub-states to minimize recompositions
// =====================================================

/**
 * Dashboard State - Contains aggregated financial metrics
 * حالة لوحة المعلومات - تحتوي على المقاييس المالية المجمعة
 */
data class DashboardState(
    val totalBalance: Double = 0.0,
    val totalAssets: Double = 0.0,
    val totalLiabilities: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val monthlyExpenses: Double = 0.0,
    val trendPercentage: Float = 0f,
    val isPositiveTrend: Boolean = true,
    val incomeChartData: List<Float> = emptyList(),
    val expenseChartData: List<Float> = emptyList(),
    val categorySummary: List<CategorySummary> = emptyList(),
    val isLoading: Boolean = true
)

/**
 * Transaction List State - Contains transaction data and filters
 * حالة قائمة المعاملات - تحتوي على بيانات المعاملات والفلاتر
 */
data class TransactionListState(
    val transactions: List<TransactionWithDetails> = emptyList(),
    val groupedTransactions: Map<String, List<TransactionWithDetails>> = emptyMap(),
    val selectedFilter: TransactionFilter = TransactionFilter.ALL,
    val selectedTimeRange: TimeRange = TimeRange.MONTH,
    val isLoading: Boolean = false
)

/**
 * Main Financial UI State - Composing sub-states
 * حالة الواجهة المالية الرئيسية - تجميع الحالات الفرعية
 */
data class FinancialUiState(
    val dashboardState: DashboardState = DashboardState(),
    val transactionListState: TransactionListState = TransactionListState(),
    val accounts: List<Account> = emptyList(),
    val selectedTab: Int = 0, // 0: Accounts, 1: Transactions
    val isSaving: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
) {
    // Computed properties for backward compatibility
    val totalBalance: Double
        get() = accounts.sumOf { it.balance }
    
    val hasNoAccounts: Boolean
        get() = accounts.isEmpty()
    
    val hasNoTransactions: Boolean
        get() = transactionListState.transactions.isEmpty()
    
    val transactions: List<TransactionWithDetails>
        get() = transactionListState.transactions
        
    val isLoading: Boolean
        get() = dashboardState.isLoading
}

/**
 * Financial ViewModel with MVVM + MVI architecture
 * فيو موديل مالي مع معمارية MVVM + MVI
 * 
 * Features:
 * - Concurrent data fetching for dashboard
 * - Sub-state management for minimal recomposition
 * - Time range and filter support
 * - Trend calculation
 */
@HiltViewModel
class FinancialViewModel @Inject constructor(
    private val financialRepository: FinancialRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(FinancialUiState())
    val uiState: StateFlow<FinancialUiState> = _uiState.asStateFlow()
    
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    
    init {
        loadData()
        loadFinancialDashboard()
    }
    
    // =====================================================
    // DATA LOADING
    // =====================================================
    
    /**
     * Load accounts data as a reactive flow
     */
    private fun loadData() {
        viewModelScope.launch {
            financialRepository.getAllActiveAccounts()
                .catch { e: Throwable ->
                    _uiState.update { it.copy(errorMessage = e.message) }
                }
                .collect { accounts: List<Account> ->
                    _uiState.update { it.copy(accounts = accounts) }
                }
        }
    }
    
    /**
     * Load financial dashboard with concurrent data fetching
     * تحميل لوحة المعلومات المالية مع الجلب المتزامن للبيانات
     */
    fun loadFinancialDashboard() {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(dashboardState = it.dashboardState.copy(isLoading = true)) 
            }
            
            try {
                val today = LocalDate.now()
                val timeRange = _uiState.value.transactionListState.selectedTimeRange
                
                val (startDate, previousStartDate) = getDateRangeForTimeRange(today, timeRange)
                
                // Concurrent data fetching
                coroutineScope {
                    val assetsDeferred = async { financialRepository.getTotalAssets() }
                    val liabilitiesDeferred = async { financialRepository.getTotalLiabilities() }
                    val currentIncomeDeferred = async { 
                        financialRepository.getTotalIncomeInRange(
                            startDate.format(dateFormatter),
                            today.format(dateFormatter)
                        )
                    }
                    val currentExpenseDeferred = async { 
                        financialRepository.getTotalExpenseInRange(
                            startDate.format(dateFormatter),
                            today.format(dateFormatter)
                        )
                    }
                    val previousIncomeDeferred = async {
                        financialRepository.getTotalIncomeInRange(
                            previousStartDate.format(dateFormatter),
                            startDate.minusDays(1).format(dateFormatter)
                        )
                    }
                    val previousExpenseDeferred = async {
                        financialRepository.getTotalExpenseInRange(
                            previousStartDate.format(dateFormatter),
                            startDate.minusDays(1).format(dateFormatter)
                        )
                    }
                    val categorySummaryDeferred = async {
                        financialRepository.getExpenseCategorySummary(
                            startDate.format(dateFormatter),
                            today.format(dateFormatter)
                        )
                    }
                    
                    val totalAssets = assetsDeferred.await()
                    val totalLiabilities = liabilitiesDeferred.await()
                    val currentIncome = currentIncomeDeferred.await()
                    val currentExpense = currentExpenseDeferred.await()
                    val previousIncome = previousIncomeDeferred.await()
                    val previousExpense = previousExpenseDeferred.await()
                    val categorySummary = categorySummaryDeferred.await()
                    
                    // Calculate trend
                    val currentNet = currentIncome - currentExpense
                    val previousNet = previousIncome - previousExpense
                    val trendPercentage = if (previousNet != 0.0) {
                        ((currentNet - previousNet) / kotlin.math.abs(previousNet) * 100).toFloat()
                    } else {
                        if (currentNet > 0) 100f else 0f
                    }
                    
                    // Generate chart data (last 7 data points)
                    val incomeChartData = generateChartData(timeRange, true)
                    val expenseChartData = generateChartData(timeRange, false)
                    
                    _uiState.update { state ->
                        state.copy(
                            dashboardState = DashboardState(
                                totalBalance = totalAssets - totalLiabilities,
                                totalAssets = totalAssets,
                                totalLiabilities = totalLiabilities,
                                monthlyIncome = currentIncome,
                                monthlyExpenses = currentExpense,
                                trendPercentage = trendPercentage,
                                isPositiveTrend = trendPercentage >= 0,
                                incomeChartData = incomeChartData,
                                expenseChartData = expenseChartData,
                                categorySummary = categorySummary,
                                isLoading = false
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        dashboardState = it.dashboardState.copy(isLoading = false),
                        errorMessage = e.message
                    )
                }
            }
        }
        
        // Load transactions in parallel
        loadTransactions()
    }
    
    /**
     * Load transactions with current filter and time range
     * تحميل المعاملات مع الفلتر والنطاق الزمني الحالي
     */
    private fun loadTransactions() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(transactionListState = it.transactionListState.copy(isLoading = true))
            }
            
            val today = LocalDate.now()
            val timeRange = _uiState.value.transactionListState.selectedTimeRange
            val (startDate, _) = getDateRangeForTimeRange(today, timeRange)
            
            financialRepository.getTransactionsWithDetailsInRange(
                startDate = startDate.format(dateFormatter),
                endDate = today.format(dateFormatter)
            ).catch { e: Throwable ->
                _uiState.update { it.copy(errorMessage = e.message) }
            }.collect { transactions: List<TransactionWithDetails> ->
                val filteredTransactions = applyTransactionFilter(
                    transactions, 
                    _uiState.value.transactionListState.selectedFilter
                )
                val groupedTransactions = groupTransactionsByDate(filteredTransactions)
                
                _uiState.update { state ->
                    state.copy(
                        transactionListState = state.transactionListState.copy(
                            transactions = filteredTransactions,
                            groupedTransactions = groupedTransactions,
                            isLoading = false
                        )
                    )
                }
            }
        }
    }
    
    // =====================================================
    // FILTERS AND TIME RANGE
    // =====================================================
    
    /**
     * Update selected time range
     */
    fun updateTimeRange(range: TimeRange) {
        _uiState.update { 
            it.copy(
                transactionListState = it.transactionListState.copy(selectedTimeRange = range)
            )
        }
        loadFinancialDashboard()
    }
    
    /**
     * Update transaction filter
     */
    fun updateTransactionFilter(filter: TransactionFilter) {
        _uiState.update { 
            it.copy(
                transactionListState = it.transactionListState.copy(selectedFilter = filter)
            )
        }
        // Reapply filter to existing transactions
        val currentTransactions = _uiState.value.transactionListState.transactions
        viewModelScope.launch {
            // Re-fetch to apply filter properly
            loadTransactions()
        }
    }
    
    /**
     * Apply filter to transactions
     */
    private fun applyTransactionFilter(
        transactions: List<TransactionWithDetails>,
        filter: TransactionFilter
    ): List<TransactionWithDetails> {
        return when (filter) {
            TransactionFilter.ALL -> transactions
            TransactionFilter.INCOME -> transactions.filter { it.type == "INCOME" }
            TransactionFilter.EXPENSE -> transactions.filter { it.type == "EXPENSE" }
            TransactionFilter.BY_PROJECT -> transactions.filter { it.projectId != null }
            TransactionFilter.BY_WORKER -> transactions.filter { it.workerId != null }
        }
    }
    
    /**
     * Group transactions by date for sticky headers
     * تجميع المعاملات حسب التاريخ للرؤوس الثابتة
     */
    private fun groupTransactionsByDate(
        transactions: List<TransactionWithDetails>
    ): Map<String, List<TransactionWithDetails>> {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val weekAgo = today.minusDays(7)
        val twoWeeksAgo = today.minusDays(14)
        
        return transactions.groupBy { transaction ->
            val date = try {
                LocalDate.parse(transaction.date ?: "", dateFormatter)
            } catch (e: Exception) {
                null
            }
            
            when {
                date == null -> "أخرى"
                date == today -> "اليوم"
                date == yesterday -> "أمس"
                date.isAfter(weekAgo) -> "هذا الأسبوع"
                date.isAfter(twoWeeksAgo) -> "الأسبوع الماضي"
                else -> "أقدم"
            }
        }
    }
    
    /**
     * Get date range based on selected time range
     */
    private fun getDateRangeForTimeRange(
        today: LocalDate,
        timeRange: TimeRange
    ): Pair<LocalDate, LocalDate> {
        return when (timeRange) {
            TimeRange.WEEK -> {
                Pair(today.minusDays(7), today.minusDays(14))
            }
            TimeRange.MONTH -> {
                Pair(today.minusMonths(1), today.minusMonths(2))
            }
            TimeRange.YEAR -> {
                Pair(today.minusYears(1), today.minusYears(2))
            }
        }
    }
    
    /**
     * Generate sample chart data based on time range
     * Note: In production, this would fetch actual daily/weekly/monthly data
     */
    private suspend fun generateChartData(timeRange: TimeRange, isIncome: Boolean): List<Float> {
        val today = LocalDate.now()
        val points = when (timeRange) {
            TimeRange.WEEK -> 7
            TimeRange.MONTH -> 30
            TimeRange.YEAR -> 12
        }
        
        // Generate data points for the chart
        val data = mutableListOf<Float>()
        for (i in points - 1 downTo 0) {
            val date = when (timeRange) {
                TimeRange.WEEK -> today.minusDays(i.toLong())
                TimeRange.MONTH -> today.minusDays(i.toLong())
                TimeRange.YEAR -> today.minusMonths(i.toLong())
            }
            
            val amount = if (isIncome) {
                financialRepository.getTodayIncome(date.format(dateFormatter))
            } else {
                financialRepository.getTodayExpenses(date.format(dateFormatter))
            }
            data.add(amount.toFloat())
        }
        
        return data
    }
    
    // =====================================================
    // TAB SELECTION
    // =====================================================
    
    fun onTabSelected(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
    }
    
    // =====================================================
    // CRUD OPERATIONS
    // =====================================================
    
    /**
     * Add account with callbacks for UI feedback
     */
    fun addAccount(
        name: String,
        type: AccountType,
        balance: Double,
        accountNumber: String?,
        bankName: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (name.isBlank()) {
            onError("اسم الحساب مطلوب")
            return
        }
        
        _uiState.update { it.copy(isSaving = true) }
        
        viewModelScope.launch {
            try {
                val account = Account(
                    name = name.trim(),
                    type = type,
                    balance = balance,
                    accountNumber = accountNumber?.takeIf { it.isNotBlank() },
                    bankName = bankName?.takeIf { it.isNotBlank() }
                )
                financialRepository.insertAccount(account)
                
                _uiState.update { 
                    it.copy(isSaving = false, successMessage = "تم إضافة الحساب بنجاح") 
                }
                loadFinancialDashboard() // Refresh dashboard
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isSaving = false, errorMessage = e.message ?: "حدث خطأ") 
                }
                onError(e.message ?: "حدث خطأ")
            }
        }
    }
    
    /**
     * Add transaction with callbacks for UI feedback
     */
    fun addTransaction(
        type: TransactionType,
        amount: Double,
        category: String,
        description: String?,
        accountId: Int?,
        projectId: Int?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (amount <= 0) {
            onError("المبلغ مطلوب")
            return
        }
        
        _uiState.update { it.copy(isSaving = true) }
        
        viewModelScope.launch {
            try {
                val transaction = Transaction(
                    type = type,
                    amount = amount,
                    category = category,
                    description = description?.takeIf { it.isNotBlank() },
                    accountId = accountId,
                    projectId = projectId,
                    date = LocalDate.now().format(dateFormatter)
                )
                financialRepository.insertTransaction(transaction)
                
                _uiState.update { 
                    it.copy(isSaving = false, successMessage = "تم إضافة المعاملة بنجاح") 
                }
                loadFinancialDashboard() // Refresh dashboard
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isSaving = false, errorMessage = e.message ?: "حدث خطأ") 
                }
                onError(e.message ?: "حدث خطأ")
            }
        }
    }
    
    // =====================================================
    // UTILITY FUNCTIONS
    // =====================================================
    
    suspend fun getTodayExpenses(): Double {
        val today = LocalDate.now().format(dateFormatter)
        return financialRepository.getTodayExpenses(today)
    }
    
    fun refresh() {
        loadData()
        loadFinancialDashboard()
    }
    
    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
