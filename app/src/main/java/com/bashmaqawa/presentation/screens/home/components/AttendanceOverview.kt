package com.bashmaqawa.presentation.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bashmaqawa.presentation.components.AttendanceSegmentedProgress
import com.bashmaqawa.presentation.screens.home.AttendanceState
import com.bashmaqawa.presentation.theme.AppColors
import com.bashmaqawa.presentation.theme.CustomShapes

/**
 * Today's Attendance Overview
 * نظرة عامة على حضور اليوم
 */
@Composable
fun AttendanceOverview(
    state: AttendanceState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CustomShapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📅 حضور اليوم",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Badge(
                    containerColor = if (state.totalWorkers > 0 && state.recordedCount == state.totalWorkers)
                        AppColors.SuccessLight
                    else
                        AppColors.WarningLight,
                    contentColor = if (state.totalWorkers > 0 && state.recordedCount == state.totalWorkers)
                        AppColors.Success
                    else
                        AppColors.Warning
                ) {
                    Text(
                        text = "${state.recordedCount}/${state.totalWorkers}",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            
            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AttendanceStatItem(
                    icon = Icons.Filled.CheckCircle,
                    label = "حاضر",
                    value = state.presentCount,
                    color = AppColors.AttendancePresent
                )
                
                AttendanceStatItem(
                    icon = Icons.Filled.Cancel,
                    label = "غائب",
                    value = state.absentCount,
                    color = AppColors.AttendanceAbsent
                )
                
                AttendanceStatItem(
                    icon = Icons.Filled.Schedule,
                    label = "متأخر",
                    value = state.lateCount,
                    color = AppColors.AttendanceHalfDay
                )
                
                AttendanceStatItem(
                    icon = Icons.Filled.HourglassEmpty,
                    label = "لم يسجل",
                    value = state.notRecordedCount,
                    color = AppColors.Gray500
                )
            }
            
            // Segmented Progress Bar
            if (state.totalWorkers > 0) {
                AttendanceSegmentedProgress(
                    presentCount = state.presentCount,
                    absentCount = state.absentCount,
                    lateCount = state.lateCount,
                    notRecordedCount = state.notRecordedCount,
                    modifier = Modifier.fillMaxWidth(),
                    height = 10.dp
                )
            }
        }
    }
}

/**
 * Attendance Stat Item
 * عنصر إحصائية حضور
 */
@Composable
private fun AttendanceStatItem(
    icon: ImageVector,
    label: String,
    value: Int,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CustomShapes.Card)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Compact Attendance Summary (Alternative Layout)
 * ملخص حضور مضغوط
 */
@Composable
fun CompactAttendanceSummary(
    state: AttendanceState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CustomShapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "حضور اليوم",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${state.presentCount}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Success
                    )
                    Text(
                        text = "من ${state.totalWorkers}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Quick stats badges
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (state.absentCount > 0) {
                    Badge(
                        containerColor = AppColors.ErrorLight,
                        contentColor = AppColors.Error
                    ) {
                        Text("غياب: ${state.absentCount}")
                    }
                }
                if (state.notRecordedCount > 0) {
                    Badge(
                        containerColor = AppColors.WarningLight,
                        contentColor = AppColors.Warning
                    ) {
                        Text("معلق: ${state.notRecordedCount}")
                    }
                }
            }
        }
    }
}

/**
 * Attendance Quick Record Button
 * زر تسجيل الحضور السريع
 */
@Composable
fun AttendanceQuickRecordButton(
    pendingCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (pendingCount == 0) return
    
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.Warning
        )
    ) {
        Icon(
            imageVector = Icons.Filled.EditCalendar,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "تسجيل حضور $pendingCount عامل")
    }
}
