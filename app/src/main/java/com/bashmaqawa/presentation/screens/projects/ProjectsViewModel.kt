package com.bashmaqawa.presentation.screens.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bashmaqawa.data.database.entities.Project
import com.bashmaqawa.data.database.entities.ProjectStatus
import com.bashmaqawa.data.repository.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Projects UI State
 * حالة واجهة المشاريع
 */
data class ProjectsUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val projects: List<Project> = emptyList(),
    val selectedTab: Int = 0, // 0: All, 1: Active, 2: Completed, 3: Paused
    val searchQuery: String = "",
    val errorMessage: String? = null
) {
    val filteredProjects: List<Project>
        get() = when (selectedTab) {
            0 -> projects
            1 -> projects.filter { it.status == ProjectStatus.ACTIVE }
            2 -> projects.filter { it.status == ProjectStatus.COMPLETED }
            3 -> projects.filter { it.status == ProjectStatus.PAUSED }
            else -> projects
        }.let { list ->
            if (searchQuery.isBlank()) list
            else list.filter { 
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.clientName?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    
    val activeCount: Int get() = projects.count { it.status == ProjectStatus.ACTIVE }
    val completedCount: Int get() = projects.count { it.status == ProjectStatus.COMPLETED }
    val pausedCount: Int get() = projects.count { it.status == ProjectStatus.PAUSED }
    val pendingCount: Int get() = projects.count { it.status == ProjectStatus.PENDING }
}

/**
 * Projects ViewModel
 * فيو موديل المشاريع
 */
@HiltViewModel
class ProjectsViewModel @Inject constructor(
    private val projectRepository: ProjectRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProjectsUiState())
    val uiState: StateFlow<ProjectsUiState> = _uiState.asStateFlow()
    
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    
    init {
        loadProjects()
    }
    
    private fun loadProjects(isRefreshing: Boolean = false) {
        viewModelScope.launch {
            if (!isRefreshing) {
                _uiState.update { it.copy(isLoading = true) }
            }
            
            projectRepository.getAllProjects()
                .catch { e: Throwable ->
                    _uiState.update { it.copy(errorMessage = e.message, isLoading = false, isRefreshing = false) }
                }
                .collect { projects: List<Project> ->
                    _uiState.update { it.copy(projects = projects, isLoading = false, isRefreshing = false) }
                }
        }
    }
    
    fun onTabSelected(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
    }
    
    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
    
    fun addProject(name: String, clientName: String, location: String) {
        viewModelScope.launch {
            val project = Project(
                name = name,
                clientName = clientName,
                location = location,
                status = ProjectStatus.PENDING,
                startDate = LocalDateTime.now().format(dateTimeFormatter)
            )
            projectRepository.insertProject(project)
        }
    }
    
    fun updateProjectStatus(projectId: Int, status: ProjectStatus) {
        viewModelScope.launch {
            projectRepository.updateProjectStatus(projectId, status)
        }
    }
    
    fun deleteProject(projectId: Int) {
        viewModelScope.launch {
            projectRepository.deleteProject(projectId)
        }
    }
    
    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadProjects(isRefreshing = true)
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
