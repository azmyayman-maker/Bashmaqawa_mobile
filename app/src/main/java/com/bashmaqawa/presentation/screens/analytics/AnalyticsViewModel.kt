package com.bashmaqawa.presentation.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bashmaqawa.data.database.dao.CategorySummary
import com.bashmaqawa.data.repository.FinancialRepository
import com.bashmaqawa.data.repository.ProjectRepository
import com.bashmaqawa.data.repository.WorkerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Analytics UI State
 * حالة واجهة التحليلات
 */
data class AnalyticsUiState(
    val isLoading: Boolean = true,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val expenseCategories: List<CategorySummary> = emptyList(),
    val incomeCategories: List<CategorySummary> = emptyList(),
    val activeProjectsCount: Int = 0,
    val totalWorkersCount: Int = 0,
    val selectedPeriod: Int = 0, // 0: This Month, 1: Last Month, 2: This Year
    val errorMessage: String? = null
) {
    val netProfit: Double get() = totalIncome - totalExpense
    val profitMargin: Double get() = if (totalIncome > 0) (netProfit / totalIncome) * 100 else 0.0
}

/**
 * Analytics ViewModel
 * فيو موديل التحليلات
 */
@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val financialRepository: FinancialRepository,
    private val projectRepository: ProjectRepository,
    private val workerRepository: WorkerRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()
    
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    
    init {
        loadAnalytics()
    }
    
    private fun loadAnalytics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val (startDate, endDate) = getDateRange(_uiState.value.selectedPeriod)
            
            try {
                // Load financial data
                val totalIncome = financialRepository.getTotalIncomeInRange(startDate, endDate)
                val totalExpense = financialRepository.getTotalExpenseInRange(startDate, endDate)
                val expenseCategories = financialRepository.getExpenseCategorySummary(startDate, endDate)
                val incomeCategories = financialRepository.getIncomeCategorySummary(startDate, endDate)
                
                // Load counts
                val activeProjects = projectRepository.getActiveProjectCount()
                val totalWorkers = workerRepository.getActiveWorkerCount()
                
                _uiState.update { 
                    it.copy(
                        totalIncome = totalIncome,
                        totalExpense = totalExpense,
                        expenseCategories = expenseCategories,
                        incomeCategories = incomeCategories,
                        activeProjectsCount = activeProjects,
                        totalWorkersCount = totalWorkers,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message, isLoading = false) }
            }
        }
    }
    
    fun onPeriodSelected(period: Int) {
        _uiState.update { it.copy(selectedPeriod = period) }
        loadAnalytics()
    }
    
    private fun getDateRange(period: Int): Pair<String, String> {
        val today = LocalDate.now()
        return when (period) {
            0 -> { // This Month
                val start = today.withDayOfMonth(1)
                start.format(dateFormatter) to today.format(dateFormatter)
            }
            1 -> { // Last Month
                val lastMonth = today.minusMonths(1)
                val start = lastMonth.withDayOfMonth(1)
                val end = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth())
                start.format(dateFormatter) to end.format(dateFormatter)
            }
            2 -> { // This Year
                val start = today.withDayOfYear(1)
                start.format(dateFormatter) to today.format(dateFormatter)
            }
            else -> {
                val start = today.withDayOfMonth(1)
                start.format(dateFormatter) to today.format(dateFormatter)
            }
        }
    }
    
    fun refresh() {
        loadAnalytics()
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
