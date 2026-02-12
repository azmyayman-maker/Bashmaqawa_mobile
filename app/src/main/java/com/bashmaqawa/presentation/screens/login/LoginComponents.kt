package com.bashmaqawa.presentation.screens.login

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bashmaqawa.presentation.theme.AppColors
import kotlin.math.sin

/**
 * Premium Login Components - Reusable UI elements for the login screen
 * مكونات تسجيل الدخول المميزة - عناصر واجهة قابلة لإعادة الاستخدام
 */

// ============================================================================
// GLASSMORPHIC CARD
// ============================================================================

/**
 * A glassmorphic card with blur effect and translucent background
 * بطاقة زجاجية مع تأثير الضبابية وخلفية شفافة
 */
@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.3f),
                        Color.White.copy(alpha = 0.1f),
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.12f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}

// ============================================================================
// ANIMATED BACKGROUND
// ============================================================================

/**
 * Animated background with moving gradient blobs
 * خلفية متحركة مع فقاعات تدرج متحركة
 */
@Composable
fun AnimatedLoginBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "background_animation")
    
    // Primary blob animation
    val blob1Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob1"
    )
    
    // Secondary blob animation
    val blob2Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob2"
    )
    
    // Color shift animation
    val colorShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "colorShift"
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        AppColors.PrimaryDark,
                        lerpColor(AppColors.Primary, AppColors.PrimaryDark, colorShift),
                        AppColors.Primary
                    )
                )
            )
    ) {
        // Animated blob 1 - Top left
        Box(
            modifier = Modifier
                .size(350.dp)
                .offset(
                    x = (-100 + blob1Offset * 50).dp,
                    y = (-80 + blob1Offset * 30).dp
                )
                .blur(120.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AppColors.Accent.copy(alpha = 0.35f),
                            AppColors.Accent.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
        
        // Animated blob 2 - Bottom right
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.BottomEnd)
                .offset(
                    x = (80 - blob2Offset * 60).dp,
                    y = (100 - blob2Offset * 40).dp
                )
                .blur(100.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AppColors.PrimaryLight.copy(alpha = 0.4f),
                            AppColors.PrimaryLight.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
        
        // Animated blob 3 - Center right (subtle)
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.CenterEnd)
                .offset(
                    x = (50 - blob1Offset * 30).dp,
                    y = (-50 + blob2Offset * 80).dp
                )
                .blur(80.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AppColors.AccentLight.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
        
        content()
    }
}

// ============================================================================
// PREMIUM TEXT FIELD
// ============================================================================

/**
 * Premium styled text field with animation and validation support
 * حقل نص مميز مع دعم الحركة والتحقق
 */
@Composable
fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
    shouldShake: Boolean = false,
    onShakeComplete: () -> Unit = {},
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onTogglePasswordVisibility: () -> Unit = {},
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {}
) {
    val shakeOffset by animateFloatAsState(
        targetValue = if (shouldShake) 1f else 0f,
        animationSpec = if (shouldShake) {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessHigh
            )
        } else {
            tween(0)
        },
        finishedListener = { onShakeComplete() },
        label = "shake"
    )
    
    // Calculate shake translation
    val shakeTranslation = if (shouldShake) {
        sin(shakeOffset * 4 * Math.PI).toFloat() * 10f
    } else 0f
    
    Column(modifier = modifier.graphicsLayer { translationX = shakeTranslation }) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { 
                Text(
                    text = label,
                    fontWeight = FontWeight.Medium
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = if (isError) AppColors.Error else Color.White.copy(alpha = 0.7f)
                )
            },
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = onTogglePasswordVisibility) {
                        Icon(
                            imageVector = if (isPasswordVisible) 
                                Icons.Filled.VisibilityOff 
                            else 
                                Icons.Filled.Visibility,
                            contentDescription = if (isPasswordVisible) "إخفاء كلمة المرور" else "إظهار كلمة المرور",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            } else null,
            visualTransformation = if (isPassword && !isPasswordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            isError = isError,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onNext = { onImeAction() },
                onDone = { onImeAction() }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isError) AppColors.Error else AppColors.Accent,
                unfocusedBorderColor = if (isError) AppColors.Error.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.4f),
                focusedLabelColor = if (isError) AppColors.Error else AppColors.Accent,
                unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                cursorColor = AppColors.Accent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                errorBorderColor = AppColors.Error,
                errorLabelColor = AppColors.Error,
                errorCursorColor = AppColors.Error,
                focusedContainerColor = Color.White.copy(alpha = 0.05f),
                unfocusedContainerColor = Color.Transparent
            )
        )
        
        // Error message
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.Error,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

// ============================================================================
// GRADIENT BUTTON
// ============================================================================

/**
 * Premium gradient button with glow effect and press animation
 * زر تدرج مميز مع تأثير التوهج والضغط
 */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: ImageVector? = null
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "buttonScale"
    )
    
    val glowAlpha by animateFloatAsState(
        targetValue = if (enabled && !isLoading) 0.6f else 0f,
        animationSpec = tween(300),
        label = "glowAlpha"
    )
    
    Box(
        modifier = modifier
            .scale(scale)
            .drawBehind {
                // Glow effect
                if (glowAlpha > 0) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                AppColors.Accent.copy(alpha = glowAlpha * 0.4f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = size.maxDimension * 0.7f
                        )
                    )
                }
            }
    ) {
        Button(
            onClick = {
                if (!isLoading && enabled) {
                    onClick()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = enabled && !isLoading,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = if (enabled && !isLoading) {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    AppColors.Accent,
                                    AppColors.AccentDark
                                )
                            )
                        } else {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    AppColors.Gray600,
                                    AppColors.Gray700
                                )
                            )
                        },
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (leadingIcon != null) {
                            Icon(
                                imageVector = leadingIcon,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                        }
                        Text(
                            text = text,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// BIOMETRIC BUTTON
// ============================================================================

/**
 * Biometric authentication button with fingerprint icon
 * زر المصادقة الحيوية بأيقونة بصمة الإصبع
 */
@Composable
fun BiometricButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "biometric_pulse")
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    val scale = if (enabled) pulseScale else 1f
    
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .size(64.dp)
            .scale(scale),
        enabled = enabled,
        shape = CircleShape,
        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
            brush = Brush.linearGradient(
                colors = if (enabled) {
                    listOf(
                        AppColors.Accent.copy(alpha = 0.8f),
                        AppColors.AccentLight.copy(alpha = 0.6f)
                    )
                } else {
                    listOf(
                        Color.White.copy(alpha = 0.2f),
                        Color.White.copy(alpha = 0.1f)
                    )
                }
            )
        ),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White.copy(alpha = 0.08f),
            contentColor = if (enabled) AppColors.Accent else Color.White.copy(alpha = 0.4f)
        )
    ) {
        Icon(
            imageVector = Icons.Outlined.Fingerprint,
            contentDescription = "تسجيل الدخول بالبصمة",
            modifier = Modifier.size(32.dp)
        )
    }
}

// ============================================================================
// UTILITY FUNCTIONS
// ============================================================================

/**
 * Linear interpolation between two colors
 */
private fun lerpColor(start: Color, end: Color, fraction: Float): Color {
    return Color(
        red = start.red + (end.red - start.red) * fraction,
        green = start.green + (end.green - start.green) * fraction,
        blue = start.blue + (end.blue - start.blue) * fraction,
        alpha = start.alpha + (end.alpha - start.alpha) * fraction
    )
}

/**
 * Shake animation modifier for error states
 * مُعدِّل حركة الاهتزاز لحالات الخطأ
 */
fun Modifier.shakeOnError(
    trigger: Boolean,
    onShakeComplete: () -> Unit = {}
): Modifier = composed {
    val shakeController = remember { Animatable(0f) }
    
    LaunchedEffect(trigger) {
        if (trigger) {
            shakeController.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 400
                    0f at 0
                    -10f at 50
                    10f at 100
                    -10f at 150
                    10f at 200
                    -5f at 250
                    5f at 300
                    0f at 400
                }
            )
            onShakeComplete()
        }
    }
    
    graphicsLayer { translationX = shakeController.value }
}

// ============================================================================
// DECORATIVE ELEMENTS
// ============================================================================

/**
 * Animated decorative line separator
 */
@Composable
fun AnimatedDivider(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "divider_glow")
    
    val glowPosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "glowPos"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colorStops = arrayOf(
                        0f to Color.White.copy(alpha = 0.1f),
                        glowPosition - 0.1f to Color.White.copy(alpha = 0.1f),
                        glowPosition to Color.White.copy(alpha = 0.4f),
                        glowPosition + 0.1f to Color.White.copy(alpha = 0.1f),
                        1f to Color.White.copy(alpha = 0.1f)
                    )
                )
            )
    )
}

/**
 * Animated "OR" divider with text
 */
@Composable
fun OrDivider(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.3f)
                        )
                    )
                )
        )
        
        Text(
            text = "أو",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

/**
 * Logo container with glassmorphic styling
 */
@Composable
fun LogoContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(110.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.3f),
                        Color.White.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            ),
        contentAlignment = Alignment.Center,
        content = { content() }
    )
}
