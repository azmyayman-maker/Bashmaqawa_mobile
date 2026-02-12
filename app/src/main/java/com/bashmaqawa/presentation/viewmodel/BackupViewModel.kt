package com.bashmaqawa.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bashmaqawa.data.preferences.PreferencesRepository
import com.bashmaqawa.data.repository.BackupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface BackupUiState {
    data object Idle : BackupUiState
    data object Loading : BackupUiState
    data class Success(val message: String) : BackupUiState
    data class Error(val message: String) : BackupUiState
}

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupRepository: BackupRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BackupUiState>(BackupUiState.Idle)
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    val lastBackupTimestamp: StateFlow<Long> = preferencesRepository.lastBackupTimestamp
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0L
        )

    fun createBackup(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = BackupUiState.Loading
            backupRepository.createBackup(uri)
                .onSuccess {
                    val timestamp = System.currentTimeMillis()
                    preferencesRepository.setLastBackupTimestamp(timestamp)
                    _uiState.value = BackupUiState.Success("تم إنشاء النسخة الاحتياطية بنجاح")
                }
                .onFailure { error ->
                    _uiState.value = BackupUiState.Error(error.message ?: "فشل إنشاء النسخة الاحتياطية")
                }
        }
    }

    fun restoreBackup(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = BackupUiState.Loading
            backupRepository.restoreBackup(uri)
                .onSuccess {
                    _uiState.value = BackupUiState.Success("تم استعادة النسخة الاحتياطية بنجاح. يرجى إعادة تشغيل التطبيق.")
                }
                .onFailure { error ->
                    _uiState.value = BackupUiState.Error(error.message ?: "فشل استعادة النسخة الاحتياطية")
                }
        }
    }
    
    fun resetState() {
        _uiState.value = BackupUiState.Idle
    }
}
