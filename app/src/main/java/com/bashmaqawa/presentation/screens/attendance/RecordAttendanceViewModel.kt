package com.bashmaqawa.presentation.screens.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bashmaqawa.data.database.entities.Attendance
import com.bashmaqawa.data.database.entities.AttendanceStatus
import com.bashmaqawa.data.repository.AttendanceRepository
import com.bashmaqawa.data.repository.ProjectRepository
import com.bashmaqawa.data.repository.WorkerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class RecordAttendanceViewModel @Inject constructor(
    private val workerRepository: WorkerRepository,
    private val projectRepository: ProjectRepository,
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordAttendanceUiState())
    val uiState: StateFlow<RecordAttendanceUiState> = _uiState.asStateFlow()

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val displayDateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ar"))

    init {
        initializeData()
    }

    private fun initializeData() {
        val today = LocalDate.now()
        _uiState.update { 
            it.copy(
                date = today.format(dateFormatter),
                formattedDate = today.format(displayDateFormatter),
                isLoading = true
            ) 
        }

        loadWorkersAndProjects()
    }

    private fun loadWorkersAndProjects() {
        viewModelScope.launch {
            try {
                // Combine workers and projects flows
                combine(
                    workerRepository.getActiveWorkers(),
                    projectRepository.getAllProjects() // Assuming we want all or just active, checking repository capability
                ) { workers, projects ->
                    Pair(workers, projects)
                }.collect { (workers, projects) ->
                    _uiState.update { state ->
                        // Initialize worker items if empty, otherwise keep existing states (e.g. if projects just updated)
                        val workerItems = if (state.workers.isEmpty()) {
                            workers.map { worker ->
                                WorkerAttendanceItem(worker = worker)
                            }
                        } else {
                            // Merge new worker list with existing state if needed (simple reload for now)
                             workers.map { worker ->
                                WorkerAttendanceItem(worker = worker)
                            }
                        }
                        
                        state.copy(
                            workers = workerItems,
                            projects = projects,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onProjectSelected(projectId: Int) {
        _uiState.update { it.copy(selectedProjectId = projectId) }
    }

    fun onWorkerStatusChanged(workerId: Int, status: AttendanceStatus) {
        _uiState.update { state ->
            val updatedWorkers = state.workers.map { item ->
                if (item.worker.id == workerId) {
                    item.copy(status = status, isSelected = true)
                } else {
                    item
                }
            }
            state.copy(workers = updatedWorkers)
        }
    }

    fun onWorkerSelectionToggled(workerId: Int, isSelected: Boolean) {
        _uiState.update { state ->
            val updatedWorkers = state.workers.map { item ->
                if (item.worker.id == workerId) {
                    item.copy(isSelected = isSelected)
                } else {
                    item
                }
            }
            state.copy(workers = updatedWorkers)
        }
    }
    
    fun onSearchQueryChanged(query: String) {
        // In a real app, we might filter the list locally or call repository search.
        // For now, simpler local filtering could be done in UI or here.
        // Let's just update state, UI can filter.
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun saveAttendance() {
        val currentState = _uiState.value
        val selectedProjectId = currentState.selectedProjectId
        val date = currentState.date

        if (selectedProjectId == null) {
            _uiState.update { it.copy(error = "الرجاء اختيار المشروع أولاً") } // Please select project first
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val attendanceList = currentState.workers
                    .filter { it.isSelected }
                    .map { item ->
                        Attendance(
                            workerId = item.worker.id,
                            projectId = selectedProjectId,
                            date = date,
                            status = item.status,
                            notes = item.notes
                        )
                    }

                if (attendanceList.isNotEmpty()) {
                    attendanceRepository.insertBatchAttendance(attendanceList)
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                } else {
                     _uiState.update { it.copy(isSaving = false, error = "لم يتم تحديد أي عمال") } // No workers selected
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = "فشل الحفظ: ${e.message}") }
            }
        }
    }
    
    fun resetSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }
}
