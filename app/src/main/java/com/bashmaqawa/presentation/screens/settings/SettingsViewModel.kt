package com.bashmaqawa.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bashmaqawa.data.database.AppDatabase
import com.bashmaqawa.data.preferences.PreferencesRepository
import com.bashmaqawa.data.utils.DummyDataGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


data class SettingsUiState(
    val isDarkMode: Boolean = false,

    val companyName: String = "بشمقاول للمقاولات",
    val isLoading: Boolean = false,
    val isGeneratingData: Boolean = false,
    val message: String? = null,
    
    // Dialog states
    val showPasswordDialog: Boolean = false,

    val showCompanyNameDialog: Boolean = false,
    val isChangingPassword: Boolean = false,
    val restartApp: Boolean = false,
    val navigateToBackup: Boolean = false
)

// Local state for transient UI events
data class LocalSettingsState(
    val isLoading: Boolean = false,
    val isGeneratingData: Boolean = false,
    val message: String? = null,
    val showPasswordDialog: Boolean = false,

    val showCompanyNameDialog: Boolean = false,
    val isChangingPassword: Boolean = false,
    val restartApp: Boolean = false,
    val navigateToBackup: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val database: AppDatabase,
    private val preferencesRepository: PreferencesRepository,
    private val dummyDataGenerator: DummyDataGenerator,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {

    // Internal mutable state for transient events
    private val _localState = MutableStateFlow(LocalSettingsState())

    // Combine persistence and local state into one UI State
    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesRepository.isDarkMode,
        preferencesRepository.companyName,
        _localState
    ) { values ->
        val isDarkMode = values[0] as Boolean
        val companyName = values[1] as String
        val localState = values[2] as LocalSettingsState
        
        SettingsUiState(
            isDarkMode = isDarkMode,

            companyName = companyName,
            isLoading = localState.isLoading,
            isGeneratingData = localState.isGeneratingData,
            message = localState.message,
            showPasswordDialog = localState.showPasswordDialog,

            showCompanyNameDialog = localState.showCompanyNameDialog,
            isChangingPassword = localState.isChangingPassword,
            restartApp = localState.restartApp,
            navigateToBackup = localState.navigateToBackup
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setDarkMode(enabled)
        }
    }


    
    fun setCompanyName(name: String) {
        viewModelScope.launch {
            preferencesRepository.setCompanyName(name)
            _localState.update { 
                it.copy(
                    showCompanyNameDialog = false,
                    message = "تم تحديث اسم الشركة"
                )
            }
        }
    }
    
    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _localState.update { it.copy(isChangingPassword = true) }
            try {
                // Verify current password
                val isValid = preferencesRepository.verifyPassword(currentPassword)
                if (!isValid) {
                    _localState.update { 
                        it.copy(
                            isChangingPassword = false,
                            message = "كلمة المرور الحالية غير صحيحة"
                        )
                    }
                    return@launch
                }
                
                // Set new password
                preferencesRepository.setPassword(newPassword)
                _localState.update { 
                    it.copy(
                        isChangingPassword = false,
                        showPasswordDialog = false,
                        message = "تم تغيير كلمة المرور بنجاح"
                    )
                }
            } catch (e: Exception) {
                _localState.update { 
                    it.copy(
                        isChangingPassword = false,
                        message = "فشل تغيير كلمة المرور: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun showPasswordDialog() {
        _localState.update { it.copy(showPasswordDialog = true) }
    }
    
    fun hidePasswordDialog() {
        _localState.update { it.copy(showPasswordDialog = false) }
    }
    

    
    fun showCompanyNameDialog() {
        _localState.update { it.copy(showCompanyNameDialog = true) }
    }
    
    fun hideCompanyNameDialog() {
        _localState.update { it.copy(showCompanyNameDialog = false) }
    }

    fun factoryReset() {
        viewModelScope.launch(Dispatchers.IO) {
            _localState.update { it.copy(isLoading = true) }
            try {
                database.clearAllTables()
                _localState.update { 
                    it.copy(
                        isLoading = false,
                        message = "تم إعادة ضبط المصنع بنجاح"
                    ) 
                }
            } catch (e: Exception) {
                _localState.update { 
                    it.copy(
                        isLoading = false,
                        message = "فشل إعادة الضبط: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    fun createBackup() {
        _localState.update { it.copy(navigateToBackup = true) }
    }
    
    fun restoreBackup() {
        _localState.update { it.copy(navigateToBackup = true) }
    }
    
    fun onBackupNavigationConsumed() {
        _localState.update { it.copy(navigateToBackup = false) }
    }

    fun clearMessage() {
        _localState.update { it.copy(message = null) }
    }

    fun generateDummyData() {
        viewModelScope.launch {
            _localState.update { it.copy(isGeneratingData = true) }
            try {
                dummyDataGenerator.generateData()
                _localState.update { 
                    it.copy(
                        isGeneratingData = false,
                        message = "تم إنشاء البيانات الافتراضية بنجاح"
                    ) 
                }
            } catch (e: Exception) {
                _localState.update { 
                    it.copy(
                        isGeneratingData = false,
                        message = "فشل إنشاء البيانات: ${e.message}"
                    ) 
                }
            }
        }
    }
}
