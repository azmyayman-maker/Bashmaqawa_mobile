package com.bashmaqawa.presentation.screens.workforce

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bashmaqawa.data.database.entities.Attendance
import com.bashmaqawa.data.database.entities.AttendanceStatus
import com.bashmaqawa.data.database.entities.Worker
import com.bashmaqawa.data.database.entities.WorkerAdvance
import com.bashmaqawa.data.database.entities.WorkerDeduction
import com.bashmaqawa.data.repository.AttendanceRepository
import com.bashmaqawa.data.repository.WorkerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Worker Transaction for display
 */
data class WorkerTransaction(
    val id: Int,
    val type: String, // "سلفة" or "خصم"
    val amount: Double,
    val description: String?,
    val date: String,
    val isDeduction: Boolean
)

/**
 * Worker Detail UI State
 * حالة واجهة تفاصيل العامل
 */
data class WorkerDetailUiState(
    val isLoading: Boolean = true,
    val worker: Worker? = null,
    val categoryName: String? = null,
    
    // Financial Summary
    val totalEarned: Double = 0.0,      // إجمالي المستحقات
    val totalAdvances: Double = 0.0,     // إجمالي السلف
    val totalDeductions: Double = 0.0,   // إجمالي الخصومات
    val netBalance: Double = 0.0,        // صافي المستحقات
    
    // Attendance Summary (last 30 days)
    val presentDays: Int = 0,
    val absentDays: Int = 0,
    val halfDays: Int = 0,
    val overtimeDays: Int = 0,
    val recentAttendance: List<Attendance> = emptyList(),
    
    // Transaction History
    val transactions: List<WorkerTransaction> = emptyList(),
    
    // UI State
    val selectedTab: Int = 0, // 0: Financial, 1: Attendance, 2: Transactions
    val showEditSheet: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showAddAdvanceSheet: Boolean = false,
    val showAddDeductionSheet: Boolean = false,
    
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val isSaving: Boolean = false
)

/**
 * Worker Detail ViewModel
 * ViewModel لتفاصيل العامل
 */
@HiltViewModel
class WorkerDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val workerRepository: WorkerRepository,
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {
    
    private val workerId: Int = savedStateHandle.get<Int>("workerId") ?: 0
    
    private val _uiState = MutableStateFlow(WorkerDetailUiState())
    val uiState: StateFlow<WorkerDetailUiState> = _uiState.asStateFlow()
    
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    
    init {
        loadWorkerDetails()
        loadAttendanceHistory()
        loadTransactionHistory()
    }
    
    private fun loadWorkerDetails() {
        viewModelScope.launch {
            try {
                workerRepository.getWorkerByIdFlow(workerId)
                    .catch { e ->
                        _uiState.update { it.copy(errorMessage = e.message, isLoading = false) }
                    }
                    .collect { worker ->
                        worker?.let { w ->
                            _uiState.update { state ->
                                state.copy(
                                    worker = w,
                                    isLoading = false
                                )
                            }
                            calculateFinancials()
                        } ?: run {
                            _uiState.update { it.copy(isLoading = false, errorMessage = "العامل غير موجود") }
                        }
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message, isLoading = false) }
            }
        }
    }
    
    private fun loadAttendanceHistory() {
        viewModelScope.launch {
            try {
                // Get attendance for last 30 days
                val endDate = LocalDate.now()
                val startDate = endDate.minusDays(30)
                
                attendanceRepository.getWorkerAttendanceInRange(
                    workerId = workerId,
                    startDate = startDate.format(dateFormatter),
                    endDate = endDate.format(dateFormatter)
                ).catch { /* Handle error */ }
                .collect { attendance ->
                    val presentDays = attendance.count { it.status == AttendanceStatus.PRESENT }
                    val absentDays = attendance.count { it.status == AttendanceStatus.ABSENT }
                    val halfDays = attendance.count { it.status == AttendanceStatus.HALF_DAY }
                    val overtimeDays = attendance.count { it.status == AttendanceStatus.OVERTIME }
                    
                    _uiState.update { state ->
                        state.copy(
                            presentDays = presentDays,
                            absentDays = absentDays,
                            halfDays = halfDays,
                            overtimeDays = overtimeDays,
                            recentAttendance = attendance.take(10)
                        )
                    }
                    calculateFinancials()
                }
            } catch (e: Exception) {
                // Silently handle
            }
        }
    }
    
    private fun loadTransactionHistory() {
        viewModelScope.launch {
            try {
                // Load advances
                val advancesFlow = workerRepository.getAdvancesByWorkerId(workerId)
                val deductionsFlow = workerRepository.getDeductionsByWorkerId(workerId)
                
                // Combine both flows
                combine(advancesFlow, deductionsFlow) { advances, deductions ->
                    Pair(advances, deductions)
                }.catch { /* Handle error */ }
                .collect { (advances, deductions) ->
                    val workerTransactions = mutableListOf<WorkerTransaction>()
                    
                    // Add advances
                    advances.forEach { advance ->
                        workerTransactions.add(
                            WorkerTransaction(
                                id = advance.id,
                                type = "سلفة",
                                amount = advance.amount,
                                description = advance.reason,
                                date = advance.date,
                                isDeduction = false
                            )
                        )
                    }
                    
                    // Add deductions
                    deductions.forEach { deduction ->
                        workerTransactions.add(
                            WorkerTransaction(
                                id = deduction.id,
                                type = "خصم",
                                amount = deduction.amount,
                                description = deduction.reason,
                                date = deduction.date,
                                isDeduction = true
                            )
                        )
                    }
                    
                    // Sort by date descending
                    workerTransactions.sortByDescending { it.date }
                    
                    val totalAdvances = advances.sumOf { it.amount }
                    val totalDeductions = deductions.sumOf { it.amount }
                    
                    _uiState.update { state ->
                        state.copy(
                            transactions = workerTransactions,
                            totalAdvances = totalAdvances,
                            totalDeductions = totalDeductions
                        )
                    }
                    calculateFinancials()
                }
            } catch (e: Exception) {
                // Silently handle
            }
        }
    }
    
    private fun calculateFinancials() {
        val state = _uiState.value
        val worker = state.worker ?: return
        val dailyRate = worker.dailyRate ?: 0.0
        
        // Calculate total earned from attendance
        val fullDayEarnings = state.presentDays * dailyRate
        val halfDayEarnings = state.halfDays * (dailyRate / 2)
        val overtimeEarnings = state.overtimeDays * (dailyRate * 1.5)
        val totalEarned = fullDayEarnings + halfDayEarnings + overtimeEarnings
        
        val netBalance = totalEarned - state.totalAdvances - state.totalDeductions
        
        _uiState.update { 
            it.copy(
                totalEarned = totalEarned,
                netBalance = netBalance
            )
        }
    }
    
    fun onTabSelected(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }
    
    fun showEditSheet() {
        _uiState.update { it.copy(showEditSheet = true) }
    }
    
    fun hideEditSheet() {
        _uiState.update { it.copy(showEditSheet = false) }
    }
    
    fun showDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }
    
    fun hideDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }
    
    fun showAddAdvanceSheet() {
        _uiState.update { it.copy(showAddAdvanceSheet = true) }
    }
    
    fun hideAddAdvanceSheet() {
        _uiState.update { it.copy(showAddAdvanceSheet = false) }
    }
    
    fun showAddDeductionSheet() {
        _uiState.update { it.copy(showAddDeductionSheet = true) }
    }
    
    fun hideAddDeductionSheet() {
        _uiState.update { it.copy(showAddDeductionSheet = false) }
    }
    
    fun updateWorker(
        name: String,
        phone: String?,
        dailyRate: Double?,
        onSuccess: () -> Unit
    ) {
        val currentWorker = _uiState.value.worker ?: return
        _uiState.update { it.copy(isSaving = true) }
        
        viewModelScope.launch {
            try {
                val updatedWorker = currentWorker.copy(
                    name = name,
                    phone = phone,
                    dailyRate = dailyRate
                )
                workerRepository.updateWorker(updatedWorker)
                
                _uiState.update { 
                    it.copy(
                        worker = updatedWorker,
                        isSaving = false,
                        showEditSheet = false,
                        successMessage = "تم تحديث بيانات العامل"
                    )
                }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isSaving = false,
                        errorMessage = e.message ?: "حدث خطأ"
                    )
                }
            }
        }
    }
    
    fun deleteWorker(onSuccess: () -> Unit) {
        _uiState.update { it.copy(isSaving = true) }
        
        viewModelScope.launch {
            try {
                workerRepository.deleteWorker(workerId)
                _uiState.update { 
                    it.copy(
                        isSaving = false,
                        successMessage = "تم حذف العامل"
                    )
                }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isSaving = false,
                        errorMessage = e.message ?: "حدث خطأ"
                    )
                }
            }
        }
    }
    
    fun addAdvance(amount: Double, description: String?) {
        _uiState.update { it.copy(isSaving = true) }
        
        viewModelScope.launch {
            try {
                val advance = WorkerAdvance(
                    workerId = workerId,
                    amount = amount,
                    reason = description ?: "سلفة للعامل",
                    date = LocalDate.now().format(dateFormatter)
                )
                workerRepository.insertAdvance(advance)
                
                _uiState.update { 
                    it.copy(
                        isSaving = false,
                        showAddAdvanceSheet = false,
                        successMessage = "تم إضافة السلفة"
                    )
                }
                loadTransactionHistory()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isSaving = false,
                        errorMessage = e.message ?: "حدث خطأ"
                    )
                }
            }
        }
    }
    
    fun addDeduction(amount: Double, description: String?) {
        _uiState.update { it.copy(isSaving = true) }
        
        viewModelScope.launch {
            try {
                val deduction = WorkerDeduction(
                    workerId = workerId,
                    amount = amount,
                    reason = description ?: "خصم من العامل",
                    date = LocalDate.now().format(dateFormatter)
                )
                workerRepository.insertDeduction(deduction)
                
                _uiState.update { 
                    it.copy(
                        isSaving = false,
                        showAddDeductionSheet = false,
                        successMessage = "تم إضافة الخصم"
                    )
                }
                loadTransactionHistory()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isSaving = false,
                        errorMessage = e.message ?: "حدث خطأ"
                    )
                }
            }
        }
    }
    
    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
