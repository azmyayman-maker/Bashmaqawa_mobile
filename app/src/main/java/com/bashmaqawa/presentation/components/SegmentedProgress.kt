package com.bashmaqawa.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bashmaqawa.presentation.theme.AppColors

/**
 * Segmented Progress Bar for Attendance Visualization
 * شريط تقدم متعدد الأقسام لعرض الحضور
 * 
 * Displays multiple colored segments representing different statuses
 * (present, absent, late, not recorded).
 */
@Composable
fun SegmentedProgress(
    segments: List<ProgressSegment>,
    modifier: Modifier = Modifier,
    height: Dp = 12.dp,
    cornerRadius: Dp = 6.dp,
    animated: Boolean = true,
    animationDuration: Int = 800
) {
    val total = segments.sumOf { it.value.toDouble() }.toFloat()
    if (total == 0f) return
    
    // Animation progress
    var animationPlayed by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationPlayed || !animated) 1f else 0f,
        animationSpec = tween(
            durationMillis = animationDuration,
            easing = FastOutSlowInEasing
        ),
        label = "segmentAnimation"
    )
    
    LaunchedEffect(Unit) {
        animationPlayed = true
    }
    
    Row(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(AppColors.Gray200)
    ) {
        segments.forEachIndexed { index, segment ->
            val weight = (segment.value / total) * animatedProgress
            if (weight > 0) {
                Box(
                    modifier = Modifier
                        .weight(weight.coerceAtLeast(0.001f))
                        .fillMaxHeight()
                        .then(
                            // Apply corner radius only to first/last segments
                            when {
                                index == 0 && index == segments.lastIndex -> 
                                    Modifier.clip(RoundedCornerShape(cornerRadius))
                                index == 0 -> 
                                    Modifier.clip(RoundedCornerShape(
                                        topStart = cornerRadius,
                                        bottomStart = cornerRadius
                                    ))
                                index == segments.lastIndex -> 
                                    Modifier.clip(RoundedCornerShape(
                                        topEnd = cornerRadius,
                                        bottomEnd = cornerRadius
                                    ))
                                else -> Modifier
                            }
                        )
                        .background(segment.color)
                )
            }
        }
    }
}

/**
 * Attendance Segmented Progress - Predefined for attendance statuses
 * شريط تقدم الحضور - معرف مسبقاً لحالات الحضور
 */
@Composable
fun AttendanceSegmentedProgress(
    presentCount: Int,
    absentCount: Int,
    lateCount: Int,
    notRecordedCount: Int,
    modifier: Modifier = Modifier,
    height: Dp = 12.dp
) {
    val segments = listOf(
        ProgressSegment(
            value = presentCount.toFloat(),
            color = AppColors.AttendancePresent,
            label = "حاضر"
        ),
        ProgressSegment(
            value = lateCount.toFloat(),
            color = AppColors.AttendanceHalfDay,
            label = "متأخر"
        ),
        ProgressSegment(
            value = absentCount.toFloat(),
            color = AppColors.AttendanceAbsent,
            label = "غائب"
        ),
        ProgressSegment(
            value = notRecordedCount.toFloat(),
            color = AppColors.Gray400,
            label = "لم يسجل"
        )
    ).filter { it.value > 0 }
    
    SegmentedProgress(
        segments = segments,
        modifier = modifier,
        height = height
    )
}

/**
 * Simple Two-Segment Progress (Income/Expense)
 * شريط تقدم بقسمين (إيرادات/مصروفات)
 */
@Composable
fun IncomeExpenseProgress(
    income: Double,
    expense: Double,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp
) {
    val segments = listOf(
        ProgressSegment(
            value = income.toFloat(),
            color = AppColors.Success,
            label = "إيرادات"
        ),
        ProgressSegment(
            value = expense.toFloat(),
            color = AppColors.Error,
            label = "مصروفات"
        )
    ).filter { it.value > 0 }
    
    SegmentedProgress(
        segments = segments,
        modifier = modifier,
        height = height
    )
}

/**
 * Progress Segment Data Class
 * فئة بيانات قسم التقدم
 */
data class ProgressSegment(
    val value: Float,
    val color: Color,
    val label: String = ""
)

/**
 * Animated Progress Bar with single color
 * شريط تقدم متحرك بلون واحد
 */
@Composable
fun AnimatedProgressBar(
    progress: Float, // 0f to 1f
    modifier: Modifier = Modifier,
    progressColor: Color = AppColors.Primary,
    backgroundColor: Color = AppColors.Gray200,
    height: Dp = 8.dp,
    cornerRadius: Dp = 4.dp,
    animationDuration: Int = 600
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(
            durationMillis = animationDuration,
            easing = FastOutSlowInEasing
        ),
        label = "progressBarAnimation"
    )
    
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .clip(RoundedCornerShape(cornerRadius))
                .background(progressColor)
        )
    }
}
