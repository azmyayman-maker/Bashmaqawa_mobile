package com.bashmaqawa.presentation.theme

import androidx.compose.ui.graphics.Color

/**
 * Color palette for Bashmaqawa app
 * نظام الألوان لتطبيق بشمقاول
 */
object AppColors {
    // Primary - Professional Blue
    val Primary = Color(0xFF1E88E5)
    val PrimaryDark = Color(0xFF1565C0)
    val PrimaryLight = Color(0xFF64B5F6)
    val PrimaryContainer = Color(0xFFD1E4FF)
    val OnPrimaryContainer = Color(0xFF001C38)
    
    // Accent - Gold
    val Accent = Color(0xFFFFB300)
    val AccentDark = Color(0xFFFF8F00)
    val AccentLight = Color(0xFFFFE54C)
    
    // Status Colors
    val Success = Color(0xFF43A047)
    val SuccessLight = Color(0xFFE8F5E9)
    val Warning = Color(0xFFFF9800)
    val WarningLight = Color(0xFFFFF3E0)
    val Error = Color(0xFFE53935)
    val ErrorLight = Color(0xFFFFEBEE)
    val Info = Color(0xFF2196F3)
    val InfoLight = Color(0xFFE3F2FD)
    
    // Neutral Colors
    val White = Color(0xFFFFFFFF)
    val Black = Color(0xFF000000)
    val Gray50 = Color(0xFFFAFAFA)
    val Gray100 = Color(0xFFF5F5F5)
    val Gray200 = Color(0xFFEEEEEE)
    val Gray300 = Color(0xFFE0E0E0)
    val Gray400 = Color(0xFFBDBDBD)
    val Gray500 = Color(0xFF9E9E9E)
    val Gray600 = Color(0xFF757575)
    val Gray700 = Color(0xFF616161)
    val Gray800 = Color(0xFF424242)
    val Gray900 = Color(0xFF212121)
    
    // Dark Mode Colors
    val DarkBackground = Color(0xFF121212)
    val DarkSurface = Color(0xFF1E1E1E)
    val DarkCard = Color(0xFF2D2D2D)
    val DarkElevated = Color(0xFF383838)
    
    // Light Mode Colors
    val LightBackground = Color(0xFFF8F9FA)
    val LightSurface = Color(0xFFFFFFFF)
    val LightCard = Color(0xFFFFFFFF)
    
    // Attendance Status Colors
    val AttendancePresent = Color(0xFF4CAF50)
    val AttendanceAbsent = Color(0xFFF44336)
    val AttendanceHalfDay = Color(0xFFFF9800)
    val AttendanceOvertime = Color(0xFF2196F3)
    
    // Project Status Colors
    val ProjectPending = Color(0xFF9E9E9E)
    val ProjectActive = Color(0xFF4CAF50)
    val ProjectCompleted = Color(0xFF2196F3)
    val ProjectPaused = Color(0xFFFF9800)
    
    // Gradients
    val PrimaryGradient = listOf(Primary, PrimaryDark)
    val AccentGradient = listOf(Accent, AccentDark)
    val SuccessGradient = listOf(Success, Color(0xFF2E7D32))
    val DarkGradient = listOf(DarkSurface, DarkBackground)
}

// Light Color Scheme
val LightColorScheme = androidx.compose.material3.lightColorScheme(
    primary = AppColors.Primary,
    onPrimary = AppColors.White,
    primaryContainer = AppColors.PrimaryContainer,
    onPrimaryContainer = AppColors.OnPrimaryContainer,
    secondary = AppColors.Accent,
    onSecondary = AppColors.Black,
    secondaryContainer = AppColors.AccentLight,
    onSecondaryContainer = AppColors.Black,
    tertiary = AppColors.Info,
    onTertiary = AppColors.White,
    background = AppColors.LightBackground,
    onBackground = AppColors.Gray900,
    surface = AppColors.LightSurface,
    onSurface = AppColors.Gray900,
    surfaceVariant = AppColors.Gray100,
    onSurfaceVariant = AppColors.Gray700,
    error = AppColors.Error,
    onError = AppColors.White,
    errorContainer = AppColors.ErrorLight,
    onErrorContainer = AppColors.Error,
    outline = AppColors.Gray400,
    outlineVariant = AppColors.Gray200
)

// Dark Color Scheme
val DarkColorScheme = androidx.compose.material3.darkColorScheme(
    primary = AppColors.PrimaryLight,
    onPrimary = AppColors.Black,
    primaryContainer = AppColors.PrimaryDark,
    onPrimaryContainer = AppColors.White,
    secondary = AppColors.AccentLight,
    onSecondary = AppColors.Black,
    secondaryContainer = AppColors.AccentDark,
    onSecondaryContainer = AppColors.White,
    tertiary = AppColors.InfoLight,
    onTertiary = AppColors.Black,
    background = AppColors.DarkBackground,
    onBackground = AppColors.White,
    surface = AppColors.DarkSurface,
    onSurface = AppColors.White,
    surfaceVariant = AppColors.DarkCard,
    onSurfaceVariant = AppColors.Gray300,
    error = AppColors.Error,
    onError = AppColors.White,
    errorContainer = AppColors.ErrorLight,
    onErrorContainer = AppColors.Error,
    outline = AppColors.Gray600,
    outlineVariant = AppColors.Gray700
)
