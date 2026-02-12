package com.bashmaqawa.presentation.screens.workforce

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bashmaqawa.data.database.entities.SkillLevel
import com.bashmaqawa.data.database.entities.Worker
import com.bashmaqawa.data.database.entities.WorkerCategory
import com.bashmaqawa.data.database.entities.WorkerCategorySeedData
import com.bashmaqawa.data.database.entities.WorkerStatus
import com.bashmaqawa.data.repository.WorkerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddWorkerViewModel @Inject constructor(
    private val workerRepository: WorkerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddWorkerUiState())
    val uiState: StateFlow<AddWorkerUiState> = _uiState.asStateFlow()

    // Expose static categories strictly for selection
    val categories = WorkerCategorySeedData.categories

    init {
        // Set default category
        _uiState.update { it.copy(selectedCategory = categories.firstOrNull()) }
    }

    fun onNameChange(newValue: String) {
        _uiState.update { it.copy(name = newValue, nameError = null) }
    }

    fun onPhoneChange(newValue: String) {
        // Simple numeric filter
        if (newValue.all { it.isDigit() }) {
            _uiState.update { it.copy(phone = newValue, phoneError = null) }
        }
    }

    fun onWhatsappPhoneChange(newValue: String) {
        if (newValue.all { it.isDigit() }) {
            _uiState.update { it.copy(whatsappPhone = newValue) }
        }
    }

    fun onNationalIdChange(newValue: String) {
        if (newValue.all { it.isDigit() } && newValue.length <= 14) {
             _uiState.update { it.copy(nationalId = newValue) }
        }
    }

    fun onDailyRateChange(newValue: String) {
        // Allow decimal points
        if (newValue.all { it.isDigit() || it == '.' }) {
             _uiState.update { it.copy(dailyRate = newValue, dailyRateError = null) }
        }
    }

    fun onNotesChange(newValue: String) {
        _uiState.update { it.copy(notes = newValue) }
    }

    fun onSkillLevelSelected(level: SkillLevel) {
        _uiState.update { it.copy(selectedSkillLevel = level) }
    }

    fun onCategorySelected(category: WorkerCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun saveWorker() {
        if (!validateInput()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val currentState = _uiState.value
                val newWorker = Worker(
                    name = currentState.name,
                    role = currentState.selectedCategory?.name ?: "عامل", // Use category name as role
                    dailyRate = currentState.dailyRate.toDoubleOrNull(),
                    phone = currentState.phone.ifBlank { null },
                    whatsappPhone = currentState.whatsappPhone.ifBlank { null },
                    nationalId = currentState.nationalId.ifBlank { null },
                    skillLevel = currentState.selectedSkillLevel,
                    categoryId = currentState.selectedCategory?.id,
                    notes = currentState.notes.ifBlank { null },
                    status = WorkerStatus.ACTIVE
                )

                workerRepository.insertWorker(newWorker)
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isSaving = false, 
                        error = "حدث خطأ أثناء الحفظ: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    fun resetSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    private fun validateInput(): Boolean {
        var isValid = true
        val currentState = _uiState.value

        if (currentState.name.isBlank()) {
            _uiState.update { it.copy(nameError = "اسم العامل مطلوب") }
            isValid = false
        }

        if (currentState.dailyRate.isNotBlank() && currentState.dailyRate.toDoubleOrNull() == null) {
            _uiState.update { it.copy(dailyRateError = "قيمة غير صحيحة") }
            isValid = false
        }
        
        return isValid
    }
}
