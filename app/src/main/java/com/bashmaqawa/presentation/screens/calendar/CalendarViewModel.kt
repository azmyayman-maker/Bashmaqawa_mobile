package com.bashmaqawa.presentation.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bashmaqawa.data.database.entities.Attendance
import com.bashmaqawa.data.database.entities.AttendanceStatus
import com.bashmaqawa.data.database.entities.Project
import com.bashmaqawa.data.database.entities.Worker
import com.bashmaqawa.data.repository.AttendanceRepository
import com.bashmaqawa.data.repository.ProjectRepository
import com.bashmaqawa.data.repository.WorkerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Calendar UI State with loading and feedback states
 * حالة واجهة التقويم مع حالات التحميل والتغذية الراجعة
 */
data class CalendarUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val selectedDate: LocalDate = LocalDate.now(),
    val attendanceRecords: List<Attendance> = emptyList(),
    val activeWorkers: List<Worker> = emptyList(),
    val activeProjects: List<Project> = emptyList(), // Active projects for dropdown
    val viewMode: Int = 0, // 0: Day, 1: Week, 2: Month
    val successMessage: String? = null,
    val errorMessage: String? = null
) {
    val hasRecords: Boolean get() = attendanceRecords.isNotEmpty()
    val presentCount: Int get() = attendanceRecords.count { it.status == AttendanceStatus.PRESENT }
    val absentCount: Int get() = attendanceRecords.count { it.status == AttendanceStatus.ABSENT }
}

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val attendanceRepository: AttendanceRepository,
    private val workerRepository: WorkerRepository,
    private val projectRepository: ProjectRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()
    
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    
    init {
        loadAttendanceForDate(LocalDate.now())
        loadActiveWorkers()
        loadActiveProjects()
    }
    
    private fun loadActiveWorkers() {
        viewModelScope.launch {
            workerRepository.getActiveWorkers()
                .catch { /* Handle error silently or log */ }
                .collect { workers ->
                    _uiState.update { it.copy(activeWorkers = workers) }
                }
        }
    }
    
    private fun loadActiveProjects() {
        viewModelScope.launch {
            projectRepository.getActiveProjects()
                .catch { /* Handle error silently or log */ }
                .collect { projects ->
                    _uiState.update { it.copy(activeProjects = projects) }
                }
        }
    }
    
    fun loadAttendanceForDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date, isLoading = true) }
        
        viewModelScope.launch {
            attendanceRepository.getAttendanceByDate(date.format(dateFormatter))
                .catch { e: Throwable ->
                    _uiState.update { it.copy(errorMessage = e.message, isLoading = false) }
                }
                .collect { records: List<Attendance> ->
                    _uiState.update { it.copy(attendanceRecords = records, isLoading = false) }
                }
        }
    }
    
    fun onViewModeChange(mode: Int) {
        _uiState.update { it.copy(viewMode = mode) }
    }
    
    fun onDateSelected(date: LocalDate) {
        loadAttendanceForDate(date)
    }
    
    /**
     * Add attendance with all fields for professional UI
     * إضافة حضور مع جميع الحقول للواجهة الاحترافية
     */
    fun addAttendance(
        workerId: Int,
        projectId: Int?,
        status: AttendanceStatus,
        hoursWorked: Double?,
        checkInTime: String? = null,
        checkOutTime: String? = null,
        overtimeHours: Double? = null,
        expensesJson: String? = null,
        notes: String? = null,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        _uiState.update { it.copy(isSaving = true) }
        
        viewModelScope.launch {
            try {
                val attendance = Attendance(
                    workerId = workerId,
                    projectId = projectId,
                    date = _uiState.value.selectedDate.format(dateFormatter),
                    status = status,
                    hoursWorked = hoursWorked,
                    checkInTime = checkInTime,
                    checkOutTime = checkOutTime,
                    overtimeHours = overtimeHours,
                    expensesJson = expensesJson,
                    notes = notes
                )
                attendanceRepository.insertAttendance(attendance)
                
                _uiState.update { 
                    it.copy(
                        isSaving = false,
                        successMessage = "تم إضافة سجل الحضور بنجاح"
                    )
                }
                
                onSuccess()
                loadAttendanceForDate(_uiState.value.selectedDate)
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isSaving = false,
                        errorMessage = e.message ?: "حدث خطأ"
                    )
                }
                onError(e.message ?: "حدث خطأ")
            }
        }
    }
    
    /**
     * Update attendance status with OPTIMISTIC UI pattern
     * تحديث حالة الحضور مع نمط التحديث المتفائل
     * 
     * 1. Cache previous state for potential rollback
     * 2. Update UI immediately (zero-latency feedback)
     * 3. Persist to backend asynchronously
     * 4. Rollback on failure with error message
     */
    fun updateAttendanceStatus(attendanceId: Int, newStatus: AttendanceStatus) {
        // 1. Cache previous state for rollback capability
        val previousRecords = _uiState.value.attendanceRecords.toList()
        
        // 2. Optimistically update UI immediately (instant feedback)
        _uiState.update { state ->
            state.copy(
                attendanceRecords = state.attendanceRecords.map { record ->
                    if (record.id == attendanceId) record.copy(status = newStatus)
                    else record
                }
            )
        }
        
        // 3. Persist to backend asynchronously
        viewModelScope.launch {
            try {
                attendanceRepository.updateAttendanceStatus(attendanceId, newStatus)
                // Success - UI already updated, just show confirmation
                _uiState.update { it.copy(successMessage = "تم تحديث الحالة") }
            } catch (e: Exception) {
                // 4. Rollback on failure - restore previous state
                _uiState.update { 
                    it.copy(
                        attendanceRecords = previousRecords,
                        errorMessage = e.message ?: "فشل تحديث الحالة"
                    )
                }
            }
        }
    }
    
    /**
     * Delete attendance with OPTIMISTIC UI pattern
     * حذف سجل الحضور مع نمط التحديث المتفائل
     */
    fun deleteAttendance(attendanceId: Int) {
        // Cache for rollback
        val previousRecords = _uiState.value.attendanceRecords.toList()
        
        // Optimistically remove from UI
        _uiState.update { state ->
            state.copy(
                attendanceRecords = state.attendanceRecords.filter { it.id != attendanceId }
            )
        }
        
        viewModelScope.launch {
            try {
                attendanceRepository.deleteAttendance(attendanceId)
                _uiState.update { it.copy(successMessage = "تم الحذف بنجاح") }
            } catch (e: Exception) {
                // Rollback on failure
                _uiState.update { 
                    it.copy(
                        attendanceRecords = previousRecords,
                        errorMessage = e.message ?: "فشل الحذف"
                    )
                }
            }
        }
    }
    
    fun refresh() {
        loadAttendanceForDate(_uiState.value.selectedDate)
    }
    
    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
