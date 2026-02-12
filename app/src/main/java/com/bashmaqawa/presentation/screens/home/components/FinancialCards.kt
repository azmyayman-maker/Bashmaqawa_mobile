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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bashmaqawa.R
import com.bashmaqawa.presentation.components.TrendBadge
import com.bashmaqawa.presentation.screens.home.FinancialState
import com.bashmaqawa.presentation.theme.AppColors
import com.bashmaqawa.presentation.theme.CustomShapes
import com.bashmaqawa.utils.CurrencyFormatter

/**
 * Financial Overview Cards - 2x2 Grid
 * بطاقات النظرة المالية العامة - شبكة 2x2
 */
@Composable
fun FinancialCards(
    financialState: FinancialState,
    onAssetsClick: () -> Unit = {},
    onLiabilitiesClick: () -> Unit = {},
    onCashClick: () -> Unit = {},
    onBankClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section Title
        Text(
            text = "📊 النظرة المالية",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        // Top Row: Assets & Liabilities
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FinancialSummaryCard(
                title = stringResource(R.string.financial_assets),
                value = financialState.totalAssets,
                trend = financialState.assetsTrend,
                icon = Icons.Filled.TrendingUp,
                iconTint = AppColors.Success,
                modifier = Modifier.weight(1f),
                onClick = onAssetsClick
            )
            
            FinancialSummaryCard(
                title = stringResource(R.string.financial_liabilities),
                value = financialState.totalLiabilities,
                trend = financialState.liabilitiesTrend,
                icon = Icons.Filled.TrendingDown,
                iconTint = AppColors.Error,
                modifier = Modifier.weight(1f),
                onClick = onLiabilitiesClick
            )
        }
        
        // Bottom Row: Cash & Bank
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BalanceCard(
                title = stringResource(R.string.cash_box),
                value = financialState.cashBalance,
                icon = Icons.Filled.AccountBalanceWallet,
                iconTint = AppColors.Accent,
                modifier = Modifier.weight(1f),
                onClick = onCashClick
            )
            
            BalanceCard(
                title = stringResource(R.string.bank_account),
                value = financialState.bankBalance,
                icon = Icons.Filled.AccountBalance,
                iconTint = AppColors.Primary,
                modifier = Modifier.weight(1f),
                onClick = onBankClick
            )
        }
    }
}

/**
 * Financial Summary Card with Trend
 * بطاقة ملخص مالي مع الاتجاه
 */
@Composable
fun FinancialSummaryCard(
    title: String,
    value: Double,
    trend: Double,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier,
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header row with icon and trend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CustomShapes.Card)
                        .background(iconTint.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                if (trend != 0.0) {
                    TrendBadge(percentage = trend)
                }
            }
            
            // Value - responsive sizing for large numbers
            Text(
                text = CurrencyFormatter.format(value),
                style = when {
                    value >= 10_000_000 -> MaterialTheme.typography.bodyLarge
                    value >= 1_000_000 -> MaterialTheme.typography.titleMedium
                    else -> MaterialTheme.typography.titleLarge
                },
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Simple Balance Card (no trend)
 * بطاقة رصيد بسيطة
 */
@Composable
fun BalanceCard(
    title: String,
    value: Double,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier,
        shape = CustomShapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = CurrencyFormatter.format(value),
                    style = when {
                        value >= 10_000_000 -> MaterialTheme.typography.bodyLarge
                        value >= 1_000_000 -> MaterialTheme.typography.titleSmall
                        else -> MaterialTheme.typography.titleMedium
                    },
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Today's Financial Summary Row
 * صف ملخص اليوم المالي
 */
@Composable
fun TodayFinancialRow(
    todayIncome: Double,
    todayExpenses: Double,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Income card
        Card(
            modifier = Modifier.weight(1f),
            shape = CustomShapes.Card,
            colors = CardDefaults.cardColors(
                containerColor = AppColors.SuccessLight
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowDownward,
                    contentDescription = null,
                    tint = AppColors.Success,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = "إيرادات اليوم",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.Success
                    )
                    Text(
                        text = CurrencyFormatter.format(todayIncome),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Success
                    )
                }
            }
        }
        
        // Expense card
        Card(
            modifier = Modifier.weight(1f),
            shape = CustomShapes.Card,
            colors = CardDefaults.cardColors(
                containerColor = AppColors.ErrorLight
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowUpward,
                    contentDescription = null,
                    tint = AppColors.Error,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = "مصروفات اليوم",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.Error
                    )
                    Text(
                        text = CurrencyFormatter.format(todayExpenses),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Error
                    )
                }
            }
        }
    }
}
