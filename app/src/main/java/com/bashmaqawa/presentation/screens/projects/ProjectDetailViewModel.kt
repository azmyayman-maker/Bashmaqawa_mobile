package com.bashmaqawa.presentation.screens.projects

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bashmaqawa.data.database.entities.Project
import com.bashmaqawa.data.database.entities.ProjectStatus
import com.bashmaqawa.data.database.entities.Attendance
import com.bashmaqawa.data.database.entities.Transaction
import com.bashmaqawa.data.database.entities.TransactionType
import com.bashmaqawa.data.database.entities.Worker
import com.bashmaqawa.data.repository.AttendanceRepository
import com.bashmaqawa.data.repository.ProjectRepository
import com.bashmaqawa.data.repository.FinancialRepository
import com.bashmaqawa.data.repository.WorkerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Project Expense Summary
 */
data class ProjectExpense(
    val id: Int,
    val category: String,
    val amount: Double,
    val description: String?,
    val date: String
)

/**
 * Project Detail UI State
 * حالة واجهة تفاصيل المشروع
 */
data class ProjectDetailUiState(
    val isLoading: Boolean = true,
    val project: Project? = null,
    
    // Financial Summary
    val totalBudget: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val totalLabor: Double = 0.0,
    val totalMaterials: Double = 0.0,
    val remainingBudget: Double = 0.0,
    
    // Workers assigned to project (from attendance)
    val recentWorkers: List<Worker> = emptyList(),
    
    // Recent Attendance for this project
    val recentAttendance: List<Attendance> = emptyList(),
    val attendanceDays: Int = 0,
    
    // Expenses
    val expenses: List<ProjectExpense> = emptyList(),
    
    // UI State
    val selectedTab: Int = 0, // 0: Overview, 1: Workers, 2: Expenses
    val showEditSheet: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showAddExpenseSheet: Boolean = false,
    
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val isSaving: Boolean = false
)

/**
 * Project Detail ViewModel
 * ViewModel لتفاصيل المشروع
 */
@HiltViewModel
class ProjectDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val projectRepository: ProjectRepository,
    private val workerRepository: WorkerRepository,
    private val attendanceRepository: AttendanceRepository,
    private val financialRepository: FinancialRepository
) : ViewModel() {
    
    private val projectId: Int = savedStateHandle.get<Int>("projectId") ?: 0
    
    private val _uiState = MutableStateFlow(ProjectDetailUiState())
    val uiState: StateFlow<ProjectDetailUiState> = _uiState.asStateFlow()
    
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    
    init {
        loadProjectDetails()
        loadProjectAttendance()
        loadProjectExpenses()
    }
    
    private fun loadProjectDetails() {
        viewModelScope.launch {
            try {
                projectRepository.getProjectByIdFlow(projectId)
                    .catch { e ->
                        _uiState.update { it.copy(errorMessage = e.message, isLoading = false) }
                    }
                    .collect { project ->
                        project?.let { p ->
                            // Calculate budget from area and price per meter
                            val calculatedBudget = (p.areaSqm ?: 0.0) * (p.pricePerMeter ?: 0.0)
                            _uiState.update { state ->
                                state.copy(
                                    project = p,
                                    totalBudget = calculatedBudget,
                                    isLoading = false
                                )
                            }
                            loadBudgetFromCostCenters()
                        } ?: run {
                            _uiState.update { it.copy(isLoading = false, errorMessage = "المشروع غير موجود") }
                        }
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message, isLoading = false) }
            }
        }
    }
    
    private fun loadBudgetFromCostCenters() {
        viewModelScope.launch {
            try {
                val totalBudget = projectRepository.getTotalBudgetByProjectId(projectId)
                val totalSpent = projectRepository.getTotalSpentByProjectId(projectId)
                
                _uiState.update { state ->
                    state.copy(
                        totalBudget = if (totalBudget > 0) totalBudget else state.totalBudget,
                        totalExpenses = totalSpent
                    )
                }
                calculateFinancials()
            } catch (e: Exception) {
                // Silently handle
            }
        }
    }
    
    private fun loadProjectAttendance() {
        viewModelScope.launch {
            try {
                val endDate = LocalDate.now()
                val startDate = endDate.minusDays(30)
                
                attendanceRepository.getProjectAttendanceInRange(
                    projectId = projectId,
                    startDate = startDate.format(dateFormatter),
                    endDate = endDate.format(dateFormatter)
                ).catch { /* Handle error */ }
                .collect { attendance ->
                    // Estimate labor cost
                    val laborCost = attendance.sumOf { att ->
                        (att.hoursWorked ?: 8.0) * 50.0 // Default hourly rate
                    }
                    
                    _uiState.update { state ->
                        state.copy(
                            recentAttendance = attendance.take(10),
                            attendanceDays = attendance.size,
                            totalLabor = laborCost
                        )
                    }
                    calculateFinancials()
                }
            } catch (e: Exception) {
                // Silently handle
            }
        }
    }
    
    private fun loadProjectExpenses() {
        viewModelScope.launch {
            try {
                financialRepository.getTransactionsByProjectId(projectId)
                    .catch { /* Handle error */ }
                    .collect { transactions ->
                        val projectExpenses = transactions.mapNotNull { t ->
                            val amount = t.amount ?: return@mapNotNull null
                            ProjectExpense(
                                id = t.id,
                                category = t.category ?: "عام",
                                amount = amount,
                                description = t.description,
                                date = t.date ?: ""
                            )
                        }
                        
                        val totalMaterials = transactions
                            .filter { it.category?.contains("مواد") == true || it.category?.contains("material") == true }
                            .sumOf { it.amount ?: 0.0 }
                        
                        val totalExpenses = transactions.sumOf { it.amount ?: 0.0 }
                        
                        _uiState.update { state ->
                            state.copy(
                                expenses = projectExpenses,
                                totalExpenses = totalExpenses + state.totalExpenses,
                                totalMaterials = totalMaterials
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
        val remainingBudget = state.totalBudget - state.totalExpenses - state.totalLabor
        
        _uiState.update { 
            it.copy(remainingBudget = remainingBudget)
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
    
    fun showAddExpenseSheet() {
        _uiState.update { it.copy(showAddExpenseSheet = true) }
    }
    
    fun hideAddExpenseSheet() {
        _uiState.update { it.copy(showAddExpenseSheet = false) }
    }
    
    fun updateProjectStatus(status: ProjectStatus) {
        viewModelScope.launch {
            try {
                projectRepository.updateProjectStatus(projectId, status)
                _uiState.update { state ->
                    state.copy(
                        project = state.project?.copy(status = status),
                        successMessage = "تم تحديث حالة المشروع"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }
    
    fun updateProject(
        name: String,
        clientName: String?,
        location: String?,
        areaSqm: Double?,
        pricePerMeter: Double?,
        onSuccess: () -> Unit
    ) {
        val currentProject = _uiState.value.project ?: return
        _uiState.update { it.copy(isSaving = true) }
        
        viewModelScope.launch {
            try {
                val updatedProject = currentProject.copy(
                    name = name,
                    clientName = clientName,
                    location = location,
                    areaSqm = areaSqm,
                    pricePerMeter = pricePerMeter
                )
                projectRepository.updateProject(updatedProject)
                
                val calculatedBudget = (areaSqm ?: 0.0) * (pricePerMeter ?: 0.0)
                
                _uiState.update { 
                    it.copy(
                        project = updatedProject,
                        totalBudget = calculatedBudget,
                        isSaving = false,
                        showEditSheet = false,
                        successMessage = "تم تحديث بيانات المشروع"
                    )
                }
                calculateFinancials()
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
    
    fun deleteProject(onSuccess: () -> Unit) {
        _uiState.update { it.copy(isSaving = true) }
        
        viewModelScope.launch {
            try {
                projectRepository.deleteProject(projectId)
                _uiState.update { 
                    it.copy(
                        isSaving = false,
                        successMessage = "تم حذف المشروع"
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
    
    fun addExpense(category: String, amount: Double, description: String?) {
        _uiState.update { it.copy(isSaving = true) }
        
        viewModelScope.launch {
            try {
                val transaction = Transaction(
                    projectId = projectId,
                    type = TransactionType.EXPENSE,
                    amount = amount,
                    category = category,
                    description = description ?: "مصروف للمشروع",
                    date = LocalDate.now().format(dateFormatter)
                )
                financialRepository.insertTransaction(transaction)
                
                _uiState.update { 
                    it.copy(
                        isSaving = false,
                        showAddExpenseSheet = false,
                        successMessage = "تم إضافة المصروف"
                    )
                }
                loadProjectExpenses()
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
