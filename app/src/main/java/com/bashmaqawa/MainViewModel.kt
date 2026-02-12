package com.bashmaqawa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bashmaqawa.data.preferences.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import com.bashmaqawa.data.repository.AuthRepository
import com.bashmaqawa.presentation.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException

@HiltViewModel
class MainViewModel @Inject constructor(
    preferencesRepository: PreferencesRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val isDarkMode: StateFlow<Boolean> = preferencesRepository.isDarkMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )
        
    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination = _startDestination.asStateFlow()
    
    init {
        determineStartDestination()
    }
    
    private fun determineStartDestination() {
        viewModelScope.launch {
            try {
                // Add timeout to prevent indefinite waiting
                val userCount = withTimeout(3000) {
                    authRepository.getUserCount()
                }
                _startDestination.value = if (userCount > 0) Screen.Login.route else Screen.Setup.route
            } catch (e: TimeoutCancellationException) {
                // Timeout - default to login screen
                _startDestination.value = Screen.Login.route
            } catch (e: Exception) {
                // Any other error - default to setup screen for first time
                _startDestination.value = Screen.Setup.route
            }
        }
    }
}
