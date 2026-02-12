package com.bashmaqawa.presentation.screens.transaction.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bashmaqawa.data.database.entities.TransactionType
import com.bashmaqawa.presentation.theme.AppColors
import java.text.NumberFormat
import java.util.Locale

/**
 * Balance Preview Card
 * بطاقة معاينة الرصيد
 * 
 * Shows real-time balance preview:
 * - Current account balance
 * - Transaction amount with direction indicator
 * - Projected balance after transaction
 * - Insufficient balance warning
 */
@Composable
fun BalancePreviewCard(
    currentBalance: Double,
    transactionAmount: Double,
    transactionType: TransactionType,
    accountName: String?,
    isInsufficientBalance: Boolean,
    modifier: Modifier = Modifier
) {
    // Skip if no account selected or no amount
    if (accountName == null || transactionAmount <= 0) return
    
    val projectedBalance = when (transactionType) {
        TransactionType.EXPENSE -> currentBalance - transactionAmount
        TransactionType.INCOME -> currentBalance + transactionAmount
        TransactionType.TRANSFER -> currentBalance - transactionAmount
    }
    
    // Animate colors based on balance status
    val projectedColor by animateColorAsState(
        targetValue = when {
            isInsufficientBalance -> AppColors.Error
            projectedBalance >= 0 -> AppColors.Success
            else -> AppColors.Error
        },
        animationSpec = tween(300),
        label = "projectedColor"
    )
    
    val cardBackgroundColor by animateColorAsState(
        targetValue = if (isInsufficientBalance) 
            AppColors.Error.copy(alpha = 0.08f) 
        else 
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        animationSpec = tween(300),
        label = "cardBgColor"
    )
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = cardBackgroundColor
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "معاينة الرصيد",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = accountName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Balance breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Current balance
                BalanceItem(
                    label = "الرصيد الحالي",
                    amount = currentBalance,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Arrow indicator
                TransactionArrow(
                    transactionType = transactionType,
                    amount = transactionAmount
                )
                
                // Projected balance
                BalanceItem(
                    label = "بعد المعاملة",
                    amount = projectedBalance,
                    color = projectedColor,
                    isBold = true
                )
            }
            
            // Insufficient balance warning
            AnimatedVisibility(
                visible = isInsufficientBalance,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppColors.Error.copy(alpha = 0.1f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = AppColors.Error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "⚠️ الرصيد غير كافي لإتمام هذه المعاملة",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.Error,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun BalanceItem(
    label: String,
    amount: Double,
    color: androidx.compose.ui.graphics.Color,
    isBold: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = formatCurrency(amount),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
private fun TransactionArrow(
    transactionType: TransactionType,
    amount: Double
) {
    val color = when (transactionType) {
        TransactionType.EXPENSE -> AppColors.Error
        TransactionType.INCOME -> AppColors.Success
        TransactionType.TRANSFER -> AppColors.Primary
    }
    
    val icon = when (transactionType) {
        TransactionType.EXPENSE -> Icons.Filled.ArrowDownward
        TransactionType.INCOME -> Icons.Filled.ArrowUpward
        TransactionType.TRANSFER -> Icons.Filled.ArrowDownward
    }
    
    val prefix = when (transactionType) {
        TransactionType.EXPENSE -> "-"
        TransactionType.INCOME -> "+"
        TransactionType.TRANSFER -> "-"
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(color.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$prefix${formatCurrency(amount)}",
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("ar", "EG"))
    formatter.minimumFractionDigits = 2
    formatter.maximumFractionDigits = 2
    return "${formatter.format(amount)} ج.م"
}
