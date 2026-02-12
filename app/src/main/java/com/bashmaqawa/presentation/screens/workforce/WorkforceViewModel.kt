package com.bashmaqawa.presentation.screens.workforce

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bashmaqawa.data.database.dao.WorkerWithCategory
import com.bashmaqawa.data.database.entities.Worker
import com.bashmaqawa.data.database.entities.WorkerStatus
import com.bashmaqawa.data.database.entities.SkillLevel
import com.bashmaqawa.data.repository.WorkerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Workforce UI State with proper state management
 * حالة واجهة القوى العاملة مع إدارة حالة صحيحة
 */
data class WorkforceUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isSaving: Boolean = false,
    val workers: List<WorkerWithCategory> = emptyList(),
    val selectedTab: Int = 0, // 0: Active, 1: Archived
    val searchQuery: String = "",
    val successMessage: String? = null,
    val errorMessage: String? = null
)

/**
 * Workforce ViewModel with full CRUD operations
 * فيو موديل القوى العاملة مع عمليات CRUD كاملة
 * 
 * FIX: Added addWorker() method to properly persist workers
 * FIX: StateFlow properly notifies UI of changes via repository-connected Flows
 */
@HiltViewModel
class WorkforceViewModel @Inject constructor(
    private val workerRepository: WorkerRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(WorkforceUiState())
    val uiState: StateFlow<WorkforceUiState> = _uiState.asStateFlow()
    
    // Debounce job for search - cancels previous search when new input arrives
    private var searchJob: Job? = null
    
    init {
        loadWorkers()
    }
    
    /**
     * Load workers based on current tab selection
     * FIX: Uses Flow.collect() so UI automatically updates when data changes
     */
    private fun loadWorkers() {
        val status = if (_uiState.value.selectedTab == 0) WorkerStatus.ACTIVE else WorkerStatus.ARCHIVED
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val flow = if (status == WorkerStatus.ACTIVE) {
                workerRepository.getActiveWorkersWithCategory()
            } else {
                // For archived, we need to get all and filter (or add repository method)
                workerRepository.getActiveWorkersWithCategory() // Placeholder - should filter archived
            }
            
            flow.catch { e: Throwable ->
                _uiState.update { it.copy(errorMessage = e.message, isLoading = false) }
            }.collect { workers: List<WorkerWithCategory> ->
                val filtered = if (_uiState.value.searchQuery.isBlank()) {
                    workers
                } else {
                    workers.filter { 
                        it.name.contains(_uiState.value.searchQuery, ignoreCase = true) 
                    }
                }
                _uiState.update { it.copy(workers = filtered, isLoading = false) }
            }
        }
    }
    
    /**
     * Switch between Active and Archived tabs
     */
    fun onTabSelected(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
        loadWorkers()
    }
    
    /**
     * Search workers by name with DEBOUNCE and INPUT SANITIZATION
     * البحث عن العمال بالاسم مع تأخير والتحقق من المدخلات
     * 
     * 1. Sanitizes input (trims whitespace, limits length)
     * 2. Cancels previous search if still pending
     * 3. Waits 300ms before triggering search (debounce)
     * 4. Handles empty query gracefully
     */
    fun onSearchQueryChange(query: String) {
        // 1. Sanitize input: trim whitespace, limit to 50 characters
        val sanitizedQuery = query.trim().take(50)
        
        _uiState.update { it.copy(searchQuery = sanitizedQuery) }
        
        // 2. Cancel previous search job if still running
        searchJob?.cancel()
        
        // 3. Debounce: wait 300ms before triggering search
        searchJob = viewModelScope.launch {
            delay(300) // Debounce delay
            
            // 4. Handle empty query gracefully - show all workers
            if (sanitizedQuery.isEmpty()) {
                loadWorkers()
                return@launch
            }
            
            // Perform filtered search within loaded data
            loadWorkers()
        }
    }
    
    /**
     * Add a new worker to the database
     * 
     * FIX: This method was missing - now properly wired to repository
     * FIX: Sets isSaving state for loading indicator
     * FIX: Shows success message for snackbar feedback
     */
    fun addWorker(
        name: String,
        phone: String?,
        dailyRate: Double?,
        skillLevel: SkillLevel = SkillLevel.HELPER,
        categoryId: Int? = null,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (name.isBlank()) {
            onError("اسم العامل مطلوب")
            return
        }
        
        _uiState.update { it.copy(isSaving = true) }
        
        viewModelScope.launch {
            try {
                val worker = Worker(
                    name = name.trim(),
                    phone = phone?.takeIf { it.isNotBlank() },
                    dailyRate = dailyRate,
                    skillLevel = skillLevel,
                    categoryId = categoryId,
                    status = WorkerStatus.ACTIVE
                )
                
                workerRepository.insertWorker(worker)
                
                _uiState.update { 
                    it.copy(
                        isSaving = false, 
                        successMessage = "تم إضافة العامل بنجاح"
                    ) 
                }
                
                onSuccess()
                
                // Data automatically refreshes via Flow collection
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
     * Archive a worker (soft delete)
     */
    fun archiveWorker(workerId: Int) {
        viewModelScope.launch {
            try {
                workerRepository.archiveWorker(workerId)
                _uiState.update { it.copy(successMessage = "تم أرشفة العامل") }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }
    
    /**
     * Restore an archived worker
     */
    fun activateWorker(workerId: Int) {
        viewModelScope.launch {
            try {
                workerRepository.activateWorker(workerId)
                _uiState.update { it.copy(successMessage = "تم تفعيل العامل") }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }
    
    /**
     * Clear success message after showing snackbar
     */
    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
    
    /**
     * Clear error message
     */
    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    /**
     * Refresh workers list for pull-to-refresh
     */
    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            loadWorkers()
            kotlinx.coroutines.delay(500) // Brief delay for visual feedback
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }
}
