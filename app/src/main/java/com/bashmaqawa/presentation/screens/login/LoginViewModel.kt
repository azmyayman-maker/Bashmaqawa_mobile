package com.bashmaqawa.presentation.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bashmaqawa.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Login ViewModel
 * فيو موديل تسجيل الدخول
 * Handles business logic, state management, and validation for the login screen
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.OnUsernameChange -> {
                _uiState.update { 
                    it.copy(
                        username = event.username,
                        usernameValidation = FieldValidationState(isValid = true),
                        authState = LoginAuthState.Idle
                    ) 
                }
            }
            is LoginEvent.OnPasswordChange -> {
                _uiState.update { 
                    it.copy(
                        password = event.password,
                        passwordValidation = FieldValidationState(isValid = true),
                        authState = LoginAuthState.Idle
                    ) 
                }
            }
            is LoginEvent.OnRememberMeChange -> {
                _uiState.update { it.copy(rememberMe = event.rememberMe) }
            }
            is LoginEvent.OnTogglePasswordVisibility -> {
                _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }
            is LoginEvent.OnLoginClick -> {
                login()
            }
            is LoginEvent.OnBiometricClick -> {
                // Biometric logic is triggered from UI, but we track availability here
                // Logic to initiate biometric is handled in UI layer via side effects
            }
            is LoginEvent.OnForgotPasswordClick -> {
                // Navigate to forgot password
            }
            is LoginEvent.OnRegisterClick -> {
                register()
            }
            is LoginEvent.ClearError -> {
                _uiState.update { it.copy(authState = LoginAuthState.Idle) }
            }
            is LoginEvent.ResetShakeState -> {
                _uiState.update { 
                    it.copy(
                        usernameValidation = it.usernameValidation.copy(shouldShake = false),
                        passwordValidation = it.passwordValidation.copy(shouldShake = false)
                    ) 
                }
            }
        }
    }

    private fun validateFields(): Boolean {
        val username = _uiState.value.username.trim()
        val password = _uiState.value.password

        var isValid = true
        
        // Validate Username
        if (username.isBlank()) {
            _uiState.update {
                it.copy(
                    usernameValidation = FieldValidationState(
                        isValid = false,
                        errorMessage = "اسم المستخدم مطلوب",
                        shouldShake = true
                    )
                )
            }
            isValid = false
        } else if (username.length < 3) {
            _uiState.update {
                it.copy(
                    usernameValidation = FieldValidationState(
                        isValid = false,
                        errorMessage = "اسم المستخدم قصير جداً",
                        shouldShake = true
                    )
                )
            }
            isValid = false
        }

        // Validate Password
        if (password.isBlank()) {
            _uiState.update {
                it.copy(
                    passwordValidation = FieldValidationState(
                        isValid = false,
                        errorMessage = "كلمة المرور مطلوبة",
                        shouldShake = true
                    )
                )
            }
            isValid = false
        } else if (password.length < 4) {
             _uiState.update {
                it.copy(
                    passwordValidation = FieldValidationState(
                        isValid = false,
                        errorMessage = "كلمة المرور يجب أن تكون 4 أحرف على الأقل",
                        shouldShake = true
                    )
                )
            }
            isValid = false
        }

        return isValid
    }

    private fun login() {
        if (!validateFields()) return

        viewModelScope.launch {
            _uiState.update { it.copy(authState = LoginAuthState.Loading) }
            
            // Simulate network delay for better UX
            delay(1000)

            try {
                val user = authRepository.authenticate(
                    username = _uiState.value.username.trim(),
                    password = _uiState.value.password
                )

                if (user != null) {
                    _uiState.update { 
                        it.copy(
                            authState = LoginAuthState.Success,
                            isLoggedIn = true
                        ) 
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            authState = LoginAuthState.Error("اسم المستخدم أو كلمة المرور غير صحيحة")
                        ) 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        authState = LoginAuthState.Error("حدث خطأ في الاتصال: ${e.localizedMessage}")
                    ) 
                }
            }
        }
    }

    private fun register() {
         if (!validateFields()) return

         viewModelScope.launch {
            _uiState.update { it.copy(authState = LoginAuthState.Loading) }
             
            delay(1000) // Simulate delay

            try {
                // Create user logic for setup mode only
                 val user = com.bashmaqawa.data.database.entities.User(
                    username = _uiState.value.username.trim(),
                    passwordHash = _uiState.value.password, // Ideally hashed
                    role = com.bashmaqawa.data.database.entities.UserRole.ADMIN,
                    displayName = "المدير",
                    createdAt = java.time.LocalDateTime.now().toString()
                )
                
                authRepository.insertUser(user)
                
                _uiState.update { 
                    it.copy(
                        authState = LoginAuthState.Success,
                        isLoggedIn = true
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        authState = LoginAuthState.Error("فشل إنشاء الحساب: ${e.localizedMessage}")
                    ) 
                }
            }
        }
    }

    /**
     * Called when biometric authentication is successful
     */
    fun onBiometricSuccess() {
        viewModelScope.launch {
            _uiState.update { it.copy(authState = LoginAuthState.Loading) }
            delay(800)
             _uiState.update { 
                it.copy(
                    authState = LoginAuthState.Success,
                    isLoggedIn = true
                ) 
            }
        }
    }

    fun onBiometricError(error: String) {
        _uiState.update { 
            it.copy(
                authState = LoginAuthState.Error(error)
            ) 
        }
    }
}
