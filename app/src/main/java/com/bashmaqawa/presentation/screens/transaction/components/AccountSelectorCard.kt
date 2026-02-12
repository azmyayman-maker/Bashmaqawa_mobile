package com.bashmaqawa.presentation.screens.transaction.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bashmaqawa.data.database.entities.Account
import com.bashmaqawa.data.database.entities.AccountType
import com.bashmaqawa.presentation.theme.AppColors
import java.text.NumberFormat
import java.util.Locale

/**
 * Account Selector Card
 * بطاقة اختيار الحساب
 * 
 * Displays selected account with balance or placeholder
 * for account selection. Glassmorphic styling.
 */
@Composable
fun AccountSelectorCard(
    selectedAccount: Account?,
    label: String = "الحساب",
    placeholder: String = "اختر الحساب",
    onClick: () -> Unit,
    isRequired: Boolean = true,
    hasError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = when {
            hasError -> AppColors.Error
            selectedAccount != null -> AppColors.Primary.copy(alpha = 0.5f)
            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        },
        animationSpec = tween(200),
        label = "borderColor"
    )
    
    Column(modifier = modifier.fillMaxWidth()) {
        // Label
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (isRequired) {
                Text(
                    text = " *",
                    style = MaterialTheme.typography.titleSmall,
                    color = AppColors.Error
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Card
        Surface(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            border = BorderStroke(
                width = if (hasError) 2.dp else 1.dp,
                color = borderColor
            ),
            enabled = enabled
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectedAccount != null) {
                    // Account icon
                    AccountIcon(
                        accountType = selectedAccount.type,
                        modifier = Modifier.size(48.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Account info
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedAccount.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "الرصيد: ${formatCurrency(selectedAccount.balance)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (selectedAccount.balance >= 0) 
                                AppColors.Success else AppColors.Error
                        )
                    }
                } else {
                    // Placeholder state
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountBalance,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Chevron indicator (RTL aware - points left for Arabic)
                Icon(
                    imageVector = Icons.Filled.ChevronLeft,
                    contentDescription = "اختيار",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        // Error message
        if (hasError && !errorMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.Error
            )
        }
    }
}

/**
 * Account icon based on account type
 */
@Composable
private fun AccountIcon(
    accountType: AccountType?,
    modifier: Modifier = Modifier
) {
    val icon: ImageVector
    val gradientColors: List<androidx.compose.ui.graphics.Color>
    
    when (accountType) {
        AccountType.CASH_BOX -> {
            icon = Icons.Filled.Money
            gradientColors = listOf(AppColors.Success, AppColors.Success.copy(alpha = 0.7f))
        }
        AccountType.BANK -> {
            icon = Icons.Filled.AccountBalance
            gradientColors = listOf(AppColors.Primary, AppColors.PrimaryDark)
        }
        AccountType.WALLET -> {
            icon = Icons.Filled.Wallet
            gradientColors = listOf(AppColors.Accent, AppColors.AccentDark)
        }
        AccountType.RECEIVABLE -> {
            icon = Icons.Filled.PersonAdd
            gradientColors = listOf(AppColors.Info, AppColors.Info.copy(alpha = 0.7f))
        }
        AccountType.PAYABLE -> {
            icon = Icons.Filled.Receipt
            gradientColors = listOf(AppColors.Warning, AppColors.Warning.copy(alpha = 0.7f))
        }
        null -> {
            icon = Icons.Filled.AccountBalance
            gradientColors = listOf(AppColors.Gray500, AppColors.Gray600)
        }
    }
    
    Box(
        modifier = modifier
            .background(
                brush = Brush.linearGradient(gradientColors),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppColors.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Format currency for display
 */
private fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("ar", "EG"))
    formatter.minimumFractionDigits = 2
    formatter.maximumFractionDigits = 2
    return "${formatter.format(amount)} ج.م"
}
