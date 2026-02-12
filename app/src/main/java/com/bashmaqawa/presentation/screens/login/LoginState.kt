package com.bashmaqawa.presentation.screens.login

/**
 * Login UI State - Comprehensive state management for login screen
 * حالة واجهة تسجيل الدخول - إدارة شاملة للحالات
 */

/**
 * Sealed class representing the current authentication state
 */
sealed class LoginAuthState {
    /** Initial idle state - no action taken */
    data object Idle : LoginAuthState()
    
    /** Authentication in progress */
    data object Loading : LoginAuthState()
    
    /** Authentication successful */
    data object Success : LoginAuthState()
    
    /** Authentication failed with error message */
    data class Error(val message: String) : LoginAuthState()
}

/**
 * Field validation state for individual input fields
 */
data class FieldValidationState(
    val isValid: Boolean = true,
    val errorMessage: String? = null,
    val shouldShake: Boolean = false
)

/**
 * Comprehensive Login UI State
 * Contains all state needed for the login screen
 */
data class LoginUiState(
    // Input field values
    val username: String = "",
    val password: String = "",
    
    // Authentication state
    val authState: LoginAuthState = LoginAuthState.Idle,
    
    // Field validation states
    val usernameValidation: FieldValidationState = FieldValidationState(),
    val passwordValidation: FieldValidationState = FieldValidationState(),
    
    // UI preferences
    val rememberMe: Boolean = false,
    val isPasswordVisible: Boolean = false,
    
    // Biometric state
    val isBiometricAvailable: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    
    // Navigation flag
    val isLoggedIn: Boolean = false
) {
    /**
     * Helper properties for UI logic
     */
    val isLoading: Boolean
        get() = authState is LoginAuthState.Loading
    
    val errorMessage: String?
        get() = (authState as? LoginAuthState.Error)?.message
    
    val hasUsernameError: Boolean
        get() = !usernameValidation.isValid
    
    val hasPasswordError: Boolean
        get() = !passwordValidation.isValid
    
    val isFormValid: Boolean
        get() = username.isNotBlank() && password.isNotBlank() &&
                usernameValidation.isValid && passwordValidation.isValid
    
    val canAttemptLogin: Boolean
        get() = username.isNotBlank() && password.isNotBlank() && !isLoading
}

/**
 * Events that can be triggered from the login screen
 */
sealed class LoginEvent {
    data class OnUsernameChange(val username: String) : LoginEvent()
    data class OnPasswordChange(val password: String) : LoginEvent()
    data class OnRememberMeChange(val rememberMe: Boolean) : LoginEvent()
    data object OnTogglePasswordVisibility : LoginEvent()
    data object OnLoginClick : LoginEvent()
    data object OnBiometricClick : LoginEvent()
    data object OnForgotPasswordClick : LoginEvent()
    data object OnRegisterClick : LoginEvent()
    data object ClearError : LoginEvent()
    data object ResetShakeState : LoginEvent()
}
