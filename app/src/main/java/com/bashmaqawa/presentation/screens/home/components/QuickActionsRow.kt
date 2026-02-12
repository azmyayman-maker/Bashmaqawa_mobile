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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bashmaqawa.R
import com.bashmaqawa.presentation.theme.AppColors
import com.bashmaqawa.presentation.theme.CustomShapes

/**
 * Quick Actions Row - Enhanced with 6 actions
 * صف الإجراءات السريعة - محسّن مع 6 إجراءات
 */
@Composable
fun QuickActionsRow(
    onRecordAttendance: () -> Unit,
    onAddExpense: () -> Unit,
    onAddWorker: () -> Unit,
    onNewProject: () -> Unit,
    onAddIncome: () -> Unit,
    onTransfer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section Title
        Text(
            text = "⚡ ${stringResource(R.string.quick_actions)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        // Grid: 3x2 layout
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // First Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionButton(
                    title = stringResource(R.string.record_attendance),
                    icon = Icons.Filled.CheckCircle,
                    iconTint = AppColors.Success,
                    modifier = Modifier.weight(1f),
                    onClick = onRecordAttendance
                )
                
                QuickActionButton(
                    title = stringResource(R.string.add_expense),
                    icon = Icons.Filled.Receipt,
                    iconTint = AppColors.Error,
                    modifier = Modifier.weight(1f),
                    onClick = onAddExpense
                )
                
                QuickActionButton(
                    title = stringResource(R.string.add_worker),
                    icon = Icons.Filled.PersonAdd,
                    iconTint = AppColors.Primary,
                    modifier = Modifier.weight(1f),
                    onClick = onAddWorker
                )
            }
            
            // Second Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionButton(
                    title = stringResource(R.string.new_project),
                    icon = Icons.Filled.AddBusiness,
                    iconTint = AppColors.Accent,
                    modifier = Modifier.weight(1f),
                    onClick = onNewProject
                )
                
                QuickActionButton(
                    title = "إيراد جديد",
                    icon = Icons.Filled.AddCard,
                    iconTint = AppColors.Success,
                    modifier = Modifier.weight(1f),
                    onClick = onAddIncome
                )
                
                QuickActionButton(
                    title = "تحويل",
                    icon = Icons.Filled.SwapHoriz,
                    iconTint = AppColors.Info,
                    modifier = Modifier.weight(1f),
                    onClick = onTransfer
                )
            }
        }
    }
}

/**
 * Quick Action Button
 * زر إجراء سريع
 */
@Composable
fun QuickActionButton(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        shape = CustomShapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CustomShapes.Card)
                    .background(iconTint.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

/**
 * Compact Quick Actions (Horizontal Scroll Alternative)
 * إجراءات سريعة مضغوطة للتمرير الأفقي
 */
@Composable
fun CompactQuickActions(
    onRecordAttendance: () -> Unit,
    onAddExpense: () -> Unit,
    onAddIncome: () -> Unit,
    onTransfer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CompactActionChip(
            label = "حضور",
            icon = Icons.Filled.CheckCircle,
            color = AppColors.Success,
            modifier = Modifier.weight(1f),
            onClick = onRecordAttendance
        )
        
        CompactActionChip(
            label = "مصروف",
            icon = Icons.Filled.Receipt,
            color = AppColors.Error,
            modifier = Modifier.weight(1f),
            onClick = onAddExpense
        )
        
        CompactActionChip(
            label = "إيراد",
            icon = Icons.Filled.AddCard,
            color = AppColors.Success,
            modifier = Modifier.weight(1f),
            onClick = onAddIncome
        )
        
        CompactActionChip(
            label = "تحويل",
            icon = Icons.Filled.SwapHoriz,
            color = AppColors.Info,
            modifier = Modifier.weight(1f),
            onClick = onTransfer
        )
    }
}

/**
 * Compact Action Chip
 * شريحة إجراء مضغوطة
 */
@Composable
private fun CompactActionChip(
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = CustomShapes.Card,
        color = color.copy(alpha = 0.1f),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}
