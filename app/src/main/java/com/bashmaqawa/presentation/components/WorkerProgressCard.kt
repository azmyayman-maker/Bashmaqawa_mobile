package com.bashmaqawa.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.bashmaqawa.presentation.theme.AppColors

/**
 * Worker Progress Indicator Component
 * مكون مؤشر تقدم العامل
 * 
 * Shows hours completed vs. daily target with animated progress bar.
 * The color adapts based on progress percentage:
 * - 100%+ = Success (Green) - overtime
 * - 75-99% = Primary (Blue) - on track
 * - 50-74% = Warning (Orange) - needs attention
 * - <50% = Error (Red) - behind schedule
 */
@Composable
fun WorkerProgressIndicator(
    hoursCompleted: Float,
    dailyTarget: Float = 8f,
    modifier: Modifier = Modifier
) {
    val progress = (hoursCompleted / dailyTarget).coerceIn(0f, 1.5f)
    val displayProgress = progress.coerceIn(0f, 1f) // Cap visual at 100%
    
    val animatedProgress by animateFloatAsState(
        targetValue = displayProgress,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "progress_animation"
    )
    
    val progressColor = when {
        progress >= 1f -> AppColors.Success          // 100%+ = Green (overtime)
        progress >= 0.75f -> AppColors.Primary       // 75-99% = Blue (on track)
        progress >= 0.5f -> AppColors.Warning        // 50-74% = Orange (needs attention)
        else -> AppColors.Error                      // <50% = Red (behind schedule)
    }
    
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${hoursCompleted.toInt()}/${dailyTarget.toInt()} ساعة",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${(progress * 100).toInt().coerceAtMost(100)}%${if (progress > 1f) "+" else ""}",
                style = MaterialTheme.typography.labelSmall,
                color = progressColor
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = progressColor,
            trackColor = progressColor.copy(alpha = 0.2f),
            strokeCap = StrokeCap.Round
        )
    }
}

/**
 * Compact progress indicator for inline use
 * مؤشر تقدم مضغوط للاستخدام المضمن
 */
@Composable
fun CompactProgressIndicator(
    hoursCompleted: Float,
    dailyTarget: Float = 8f,
    modifier: Modifier = Modifier
) {
    val progress = (hoursCompleted / dailyTarget).coerceIn(0f, 1f)
    
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "compact_progress"
    )
    
    val progressColor = when {
        progress >= 1f -> AppColors.Success
        progress >= 0.75f -> AppColors.Primary
        progress >= 0.5f -> AppColors.Warning
        else -> AppColors.Error
    }
    
    LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = modifier.height(4.dp),
        color = progressColor,
        trackColor = progressColor.copy(alpha = 0.15f),
        strokeCap = StrokeCap.Round
    )
}
