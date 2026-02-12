package com.bashmaqawa.presentation.screens.login

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.bashmaqawa.R
import com.bashmaqawa.presentation.theme.AppColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * State-of-the-Art Login Screen
 * شاشة تسجيل الدخول المتطورة
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    isSetupMode: Boolean = false,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Biometric Authentication Setup
    val biometricManager = remember { BiometricManager.from(context) }
    val isBiometricAvailable = remember {
        biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
    }
    
    val executor = remember { ContextCompat.getMainExecutor(context) }
    val biometricPrompt = remember {
        if (context is FragmentActivity) {
            BiometricPrompt(context, executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    viewModel.onBiometricSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    viewModel.onBiometricError(errString.toString())
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    viewModel.onBiometricError("فشلت عملية التحقق من البصمة")
                }
            })
        } else null
    }

    val promptInfo = remember {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle(context.getString(R.string.biometric_prompt_title))
            .setSubtitle(context.getString(R.string.biometric_prompt_subtitle))
            .setNegativeButtonText(context.getString(R.string.cancel))
            .build()
    }

    // Effect: Handle Success Navigation
    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            delay(500) // Allow success animation to play
            onLoginSuccess()
        }
    }

    // Effect: Reset shake state after animation
    LaunchedEffect(uiState.usernameValidation.shouldShake, uiState.passwordValidation.shouldShake) {
        if (uiState.usernameValidation.shouldShake || uiState.passwordValidation.shouldShake) {
            delay(500)
            viewModel.onEvent(LoginEvent.ResetShakeState)
        }
    }

    // Animated Background Container
    AnimatedLoginBackground {
        
        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .navigationBarsPadding()
                .imePadding(), // Handle keyboard visibility
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            
            // Staggered Entry Animations
            var startAnimation by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { startAnimation = true }
            
            // 1. Logo Animation
            AnimatedVisibility(
                visible = startAnimation,
                enter = fadeIn(animationSpec = tween(600)) + 
                        slideInVertically(initialOffsetY = { -50 }, animationSpec = tween(600))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LogoContainer {
                        Image(
                            painter = painterResource(id = R.mipmap.ic_launcher),
                            contentDescription = null,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Text(
                        text = if (isSetupMode) stringResource(R.string.setup_title) else stringResource(R.string.login_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // 2. Glassmorphic Card Animation
            AnimatedVisibility(
                visible = startAnimation,
                enter = fadeIn(animationSpec = tween(800, delayMillis = 200)) + 
                        slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(800, delayMillis = 200))
            ) {
                GlassmorphicCard {
                    Text(
                        text = if (isSetupMode) stringResource(R.string.setup_button) else stringResource(R.string.login_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Username Field
                    PremiumTextField(
                        value = uiState.username,
                        onValueChange = { viewModel.onEvent(LoginEvent.OnUsernameChange(it)) },
                        label = stringResource(R.string.username_hint),
                        leadingIcon = Icons.Filled.Person,
                        isError = uiState.usernameValidation.isValid.not(),
                        errorMessage = uiState.usernameValidation.errorMessage,
                        shouldShake = uiState.usernameValidation.shouldShake,
                        onShakeComplete = { /* Handled by reset event */ },
                        imeAction = ImeAction.Next
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Password Field
                    PremiumTextField(
                        value = uiState.password,
                        onValueChange = { viewModel.onEvent(LoginEvent.OnPasswordChange(it)) },
                        label = stringResource(R.string.password_hint),
                        leadingIcon = Icons.Filled.Lock,
                        isPassword = true,
                        isPasswordVisible = uiState.isPasswordVisible,
                        onTogglePasswordVisibility = { viewModel.onEvent(LoginEvent.OnTogglePasswordVisibility) },
                        isError = uiState.passwordValidation.isValid.not(),
                        errorMessage = uiState.passwordValidation.errorMessage,
                        shouldShake = uiState.passwordValidation.shouldShake,
                        imeAction = ImeAction.Done,
                        onImeAction = { viewModel.onEvent(LoginEvent.OnLoginClick) }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Remember Me & Forgot Password (Only in Login Mode)
                    if (!isSetupMode) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 4.dp)
                            ) {
                                Checkbox(
                                    checked = uiState.rememberMe,
                                    onCheckedChange = { viewModel.onEvent(LoginEvent.OnRememberMeChange(it)) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = AppColors.Accent,
                                        uncheckedColor = Color.White.copy(alpha = 0.6f),
                                        checkmarkColor = Color.White
                                    )
                                )
                                Text(
                                    text = stringResource(R.string.remember_me),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                            
                            TextButton(onClick = { viewModel.onEvent(LoginEvent.OnForgotPasswordClick) }) {
                                Text(
                                    text = stringResource(R.string.forgot_password),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppColors.Accent,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Global Error Message
                    AnimatedVisibility(
                        visible = uiState.errorMessage != null,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .background(
                                    color = AppColors.Error.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = AppColors.Error.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                text = uiState.errorMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppColors.Error,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                    
                    // Login Button
                    GradientButton(
                        text = if (isSetupMode) stringResource(R.string.setup_button) else stringResource(R.string.login_button),
                        onClick = { 
                            if (isSetupMode) {
                                viewModel.onEvent(LoginEvent.OnRegisterClick)
                            } else {
                                viewModel.onEvent(LoginEvent.OnLoginClick) 
                            }
                        },
                        isLoading = uiState.isLoading,
                        enabled = uiState.canAttemptLogin
                    )
                    
                    // Biometric Section
                    if (!isSetupMode && isBiometricAvailable) {
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        OrDivider()
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        BiometricButton(
                            onClick = { biometricPrompt?.authenticate(promptInfo) },
                            enabled = !uiState.isLoading
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "الدخول بالبصمة",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            
            // Footer Info
            if (!isSetupMode) {
                Spacer(modifier = Modifier.height(32.dp))
                AnimatedVisibility(
                    visible = startAnimation,
                    enter = fadeIn(animationSpec = tween(1000, delayMillis = 600))
                ) {
                    Text(
                        text = "Bashmaqawa v1.0.0",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}
