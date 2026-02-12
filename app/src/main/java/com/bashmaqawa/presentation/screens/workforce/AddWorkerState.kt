package com.bashmaqawa.presentation.screens.workforce

import com.bashmaqawa.data.database.entities.SkillLevel
import com.bashmaqawa.data.database.entities.WorkerCategory

data class AddWorkerUiState(
    val name: String = "",
    val phone: String = "",
    val whatsappPhone: String = "",
    val nationalId: String = "",
    val dailyRate: String = "",
    val notes: String = "",
    val selectedSkillLevel: SkillLevel = SkillLevel.HELPER,
    val selectedCategory: WorkerCategory? = null,
    
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null,
    
    // Validation errors
    val nameError: String? = null,
    val dailyRateError: String? = null,
    val phoneError: String? = null
)
