package com.bashmaqawa.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView

/**
 * Haptic Feedback Utilities
 * أدوات التغذية الراجعة اللمسية
 */
object HapticFeedback {
    
    /**
     * Light tap feedback for button clicks
     * تغذية راجعة خفيفة للنقرات
     */
    fun lightTap(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }
    
    /**
     * Medium feedback for confirmations
     * تغذية راجعة متوسطة للتأكيدات
     */
    fun confirm(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    }
    
    /**
     * Strong feedback for errors/warnings
     * تغذية راجعة قوية للأخطاء
     */
    fun error(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.REJECT)
    }
    
    /**
     * Long press feedback
     * تغذية راجعة للضغط المطول
     */
    fun longPress(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }
    
    /**
     * Custom vibration duration
     * اهتزاز مخصص
     */
    fun vibrate(context: Context, duration: Long = 50) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }
    
    /**
     * Success pattern vibration
     * نمط اهتزاز النجاح
     */
    fun successPattern(context: Context) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Short-Short pattern for success
            val pattern = longArrayOf(0, 30, 50, 30)
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            val pattern = longArrayOf(0, 30, 50, 30)
            vibrator.vibrate(pattern, -1)
        }
    }
}

/**
 * Composable to get current View for haptic feedback
 * دالة للحصول على العرض الحالي للتغذية الراجعة
 */
@Composable
fun rememberHapticView(): View {
    return LocalView.current
}
