package com.bashmaqawa.presentation.screens.financial

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bashmaqawa.R
import com.bashmaqawa.data.database.dao.TransactionWithDetails
import com.bashmaqawa.data.database.entities.Account
import com.bashmaqawa.data.database.entities.AccountCategory
import com.bashmaqawa.data.database.entities.AccountType
import com.bashmaqawa.data.database.entities.JournalReferenceType
import com.bashmaqawa.data.database.entities.TransactionState
import com.bashmaqawa.data.database.entities.TransactionType
import com.bashmaqawa.presentation.theme.AppColors
import java.text.NumberFormat
import java.util.Locale

/**
 * Account Detail Components
 * مكونات تفاصيل الحساب
 * 
 * Premium UI components for the Account Detail Screen
 */

// =====================================================
// CURRENCY FORMATTING
// =====================================================

// Reusing the central formatter for consistency
fun formatCurrency(amount: Double): String = com.bashmaqawa.utils.CurrencyFormatter.format(amount)

// =====================================================
// ACCOUNT BALANCE CARD (Glassmorphic)
// =====================================================

/**
 * Premium glassmorphic balance card
 * بطاقة الرصيد الزجاجية المميزة
 */
@Composable
fun AccountBalanceCard(
    account: Account,
    trend: BalanceTrend,
    trendPercentage: Double,
    pendingBalance: Double,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = androidx.compose.foundation.isSystemInDarkTheme()
    
    // Animated balance
    var displayedBalance by remember { mutableDoubleStateOf(0.0) }
    val animatedBalance by animateFloatAsState(
        targetValue = account.balance.toFloat(),
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "balance_animation"
    )
    
    LaunchedEffect(animatedBalance) {
        displayedBalance = animatedBalance.toDouble()
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = AppColors.Primary.copy(alpha = 0.3f),
                spotColor = AppColors.Primary.copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) 
                AppColors.DarkCard.copy(alpha = 0.85f) 
            else 
                AppColors.White.copy(alpha = 0.9f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isDarkTheme) {
                            listOf(
                                AppColors.PrimaryDark.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        } else {
                            listOf(
                                AppColors.Primary.copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        }
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                // Account Type & Category Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Account Type Icon
                    AccountTypeIcon(
                        type = account.type,
                        size = 48.dp
                    )
                    
                    // Category Chip
                    AccountCategoryChip(category = account.category)
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Account Name
                Text(
                    text = account.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Account Code
                account.accountCode?.let { code ->
                    Text(
                        text = "رمز الحساب: $code",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Balance with Animation
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = formatCurrency(displayedBalance),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Trend Indicator
                    BalanceTrendIndicator(
                        trend = trend,
                        percentage = trendPercentage
                    )
                }
                
                // Pending Balance (if any)
                if (pendingBalance != 0.0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "معلق: ${formatCurrency(pendingBalance)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.Warning
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Bank Info (for bank accounts)
                if (account.type == AccountType.BANK) {
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        account.bankName?.let { bankName ->
                            Column {
                                Text(
                                    text = "البنك",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = bankName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        account.accountNumber?.let { accNum ->
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "رقم الحساب",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = accNum,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
                
                // System Account Badge
                if (account.isSystemAccount) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AppColors.Info.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = AppColors.Info
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "حساب نظام",
                                style = MaterialTheme.typography.labelSmall,
                                color = AppColors.Info
                            )
                        }
                    }
                }
            }
        }
    }
}

// =====================================================
// ACCOUNT TYPE ICON
// =====================================================

@Composable
fun AccountTypeIcon(
    type: AccountType?,
    size: androidx.compose.ui.unit.Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    val (icon, color, bgColor) = when (type) {
        AccountType.CASH_BOX -> Triple(Icons.Filled.AccountBalanceWallet, AppColors.Success, AppColors.SuccessLight)
        AccountType.BANK -> Triple(Icons.Filled.AccountBalance, AppColors.Primary, AppColors.PrimaryContainer)
        AccountType.WALLET -> Triple(Icons.Filled.CreditCard, AppColors.Accent, AppColors.AccentLight)
        AccountType.RECEIVABLE -> Triple(Icons.Filled.CallReceived, AppColors.Info, AppColors.InfoLight)
        AccountType.PAYABLE -> Triple(Icons.Filled.CallMade, AppColors.Warning, AppColors.WarningLight)
        null -> Triple(Icons.Filled.AccountCircle, AppColors.Gray500, AppColors.Gray200)
    }
    
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = type?.name,
            modifier = Modifier.size(size * 0.55f),
            tint = color
        )
    }
}

// =====================================================
// ACCOUNT CATEGORY CHIP
// =====================================================

@Composable
fun AccountCategoryChip(
    category: AccountCategory,
    modifier: Modifier = Modifier
) {
    val (label, color) = when (category) {
        AccountCategory.ASSET -> "أصول" to AppColors.Success
        AccountCategory.LIABILITY -> "خصوم" to AppColors.Error
        AccountCategory.EQUITY -> "حقوق ملكية" to AppColors.Primary
        AccountCategory.REVENUE -> "إيرادات" to AppColors.Accent
        AccountCategory.EXPENSE -> "مصروفات" to AppColors.Warning
    }
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

// =====================================================
// BALANCE TREND INDICATOR
// =====================================================

@Composable
fun BalanceTrendIndicator(
    trend: BalanceTrend,
    percentage: Double,
    modifier: Modifier = Modifier
) {
    val (icon, color, label) = when (trend) {
        BalanceTrend.INCREASING -> Triple(Icons.Filled.TrendingUp, AppColors.Success, "ارتفاع")
        BalanceTrend.DECREASING -> Triple(Icons.Filled.TrendingDown, AppColors.Error, "انخفاض")
        BalanceTrend.STABLE -> Triple(Icons.Filled.TrendingFlat, AppColors.Gray500, "مستقر")
    }
    
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(18.dp),
            tint = color
        )
        if (percentage > 0) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${String.format("%.1f", percentage)}%",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

// =====================================================
// METRICS GRID
// =====================================================

@Composable
fun AccountMetricsGrid(
    analytics: AccountAnalyticsState,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "إجمالي الوارد",
                value = formatCurrency(analytics.totalInflow),
                icon = Icons.Filled.ArrowDownward,
                color = AppColors.Success,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "إجمالي الصادر",
                value = formatCurrency(analytics.totalOutflow),
                icon = Icons.Filled.ArrowUpward,
                color = AppColors.Error,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "صافي التغيير",
                value = formatCurrency(analytics.netChange),
                icon = Icons.Filled.CompareArrows,
                color = if (analytics.netChange >= 0) AppColors.Success else AppColors.Error,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "متوسط المعاملة",
                value = formatCurrency(analytics.averageTransactionSize),
                icon = Icons.Filled.Calculate,
                color = AppColors.Primary,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "عدد المعاملات",
                value = analytics.transactionCount.toString(),
                icon = Icons.Filled.Receipt,
                color = AppColors.Info,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "أكبر معاملة",
                value = formatCurrency(analytics.largestTransaction),
                icon = Icons.Filled.Star,
                color = AppColors.Accent,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = color
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// =====================================================
// TIME RANGE SELECTOR
// =====================================================

@Composable
fun TimeRangeSelector(
    selectedRange: AccountTimeRange,
    onRangeSelected: (AccountTimeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        AccountTimeRange.entries.forEach { range ->
            val isSelected = range == selectedRange
            
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) AppColors.Primary else Color.Transparent,
                onClick = { onRangeSelected(range) }
            ) {
                Text(
                    text = range.labelEn,
                    modifier = Modifier.padding(vertical = 10.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) AppColors.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// =====================================================
// TRANSACTION FILTER CHIPS
// =====================================================

@Composable
fun TransactionFilterChips(
    selectedFilter: AccountTransactionFilter,
    onFilterSelected: (AccountTransactionFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AccountTransactionFilter.entries.forEach { filter ->
            val isSelected = filter == selectedFilter
            
            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(filter) },
                label = {
                    Text(
                        text = filter.labelAr,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AppColors.Primary,
                    selectedLabelColor = AppColors.White
                )
            )
        }
    }
}

// =====================================================
// TRANSACTION LIST ITEM (Expandable)
// =====================================================

@Composable
fun TransactionListItem(
    transaction: TransactionWithDetails,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onAction: (TransactionAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val (typeColor, typeIcon) = when (transaction.type) {
        "INCOME" -> AppColors.Success to Icons.Filled.ArrowDownward
        "EXPENSE" -> AppColors.Error to Icons.Filled.ArrowUpward
        "TRANSFER" -> AppColors.Primary to Icons.Filled.SwapHoriz
        else -> AppColors.Gray500 to Icons.Filled.Help
    }
    
    val stateColor = when (transaction.transactionState) {
        "PENDING" -> AppColors.Warning
        "CLEARED" -> AppColors.Success
        "VOID" -> AppColors.Gray500
        else -> AppColors.Gray500
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onToggleExpand() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (transaction.transactionState == "VOID")
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Main Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Icon + Info
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category Icon
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(typeColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = typeIcon,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = typeColor
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        // Description or Category
                        Text(
                            text = transaction.description ?: transaction.category ?: "معاملة",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        // Date
                        transaction.date?.let { date ->
                            Text(
                                text = date.substring(0, 10),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Right: Amount + State
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = when (transaction.type) {
                            "INCOME" -> "+${formatCurrency(transaction.amount ?: 0.0)}"
                            "EXPENSE" -> "-${formatCurrency(transaction.amount ?: 0.0)}"
                            else -> formatCurrency(transaction.amount ?: 0.0)
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = typeColor
                    )
                    
                    // State Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = stateColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = when (transaction.transactionState) {
                                "PENDING" -> "معلقة"
                                "CLEARED" -> "مقاصة"
                                "VOID" -> "ملغاة"
                                else -> transaction.transactionState ?: ""
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = stateColor
                        )
                    }
                }
            }
            
            // Expanded Content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Details Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        transaction.category?.let {
                            DetailItem(label = "الفئة", value = it)
                        }
                        transaction.projectName?.let {
                            DetailItem(label = "المشروع", value = it)
                        }
                        transaction.workerName?.let {
                            DetailItem(label = "العامل", value = it)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // For now, simplified without journalEntryId check
                        OutlinedButton(
                            onClick = { onAction(TransactionAction.VIEW_JOURNAL) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Book,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("القيد")
                        }
                        
                        if (transaction.transactionState == "CLEARED") {
                            OutlinedButton(
                                onClick = { onAction(TransactionAction.REVERSE) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = AppColors.Error
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Undo,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("إلغاء")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailItem(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

// =====================================================
// STICKY DATE HEADER
// =====================================================

@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.transactionListWithStickyHeaders(
    groupedTransactions: Map<String, List<TransactionWithDetails>>,
    expandedTransactionId: Int?,
    onTransactionClick: (Int) -> Unit,
    onTransactionAction: (TransactionWithDetails, TransactionAction) -> Unit
) {
    groupedTransactions.forEach { (dateHeader, transactions) ->
        stickyHeader(key = dateHeader) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.background
            ) {
                Text(
                    text = dateHeader,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Primary
                )
            }
        }
        
        items(
            items = transactions,
            key = { it.id }
        ) { transaction ->
            TransactionListItem(
                transaction = transaction,
                isExpanded = transaction.id == expandedTransactionId,
                onToggleExpand = { onTransactionClick(transaction.id) },
                onAction = { action -> onTransactionAction(transaction, action) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }
    }
}

// =====================================================
// JOURNAL ENTRY CARD
// =====================================================

@Composable
fun JournalEntryCard(
    entry: JournalEntryWithDetails,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val referenceColor = when (entry.entry.referenceType) {
        JournalReferenceType.TRANSACTION -> AppColors.Primary
        JournalReferenceType.PAYROLL -> AppColors.Success
        JournalReferenceType.ADVANCE -> AppColors.Warning
        JournalReferenceType.TRANSFER -> AppColors.Info
        JournalReferenceType.ADJUSTMENT -> AppColors.Accent
        JournalReferenceType.OPENING_BALANCE -> AppColors.Gray500
        null -> AppColors.Gray500
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (entry.entry.isReversing)
                AppColors.Error.copy(alpha = 0.05f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Date
                Text(
                    text = entry.entry.entryDate.substring(0, 10),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                // Reference Type Badge
                entry.entry.referenceType?.let { refType ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = referenceColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = when (refType) {
                                JournalReferenceType.TRANSACTION -> "معاملة"
                                JournalReferenceType.PAYROLL -> "رواتب"
                                JournalReferenceType.ADVANCE -> "سلفة"
                                JournalReferenceType.TRANSFER -> "تحويل"
                                JournalReferenceType.ADJUSTMENT -> "تسوية"
                                JournalReferenceType.OPENING_BALANCE -> "رصيد افتتاحي"
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = referenceColor
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Debit -> Credit Flow
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Debit Account
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "مدين",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.Error
                    )
                    Text(
                        text = entry.debitAccountName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Arrow
                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Credit Account
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "دائن",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.Success
                    )
                    Text(
                        text = entry.creditAccountName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Amount
            Text(
                text = formatCurrency(entry.entry.amount),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = AppColors.Primary
            )
            
            // Reversing Badge
            if (entry.entry.isReversing) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = AppColors.Error.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Undo,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = AppColors.Error
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "قيد عكسي",
                                style = MaterialTheme.typography.labelSmall,
                                color = AppColors.Error
                            )
                        }
                    }
                }
            }
            
            // Expanded Details
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Description
                    Text(
                        text = "الوصف",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = entry.entry.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    // Created At
                    entry.entry.createdAt?.let { createdAt ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "تاريخ الإنشاء: $createdAt",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// =====================================================
// FAB MENU
// =====================================================

@Composable
fun AccountFabMenu(
    expanded: Boolean,
    onToggle: () -> Unit,
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    onTransfer: () -> Unit,
    onGenerateStatement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        // Menu Items
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FabMenuItem(
                    label = "إيراد",
                    icon = Icons.Filled.Add,
                    color = AppColors.Success,
                    onClick = {
                        onToggle()
                        onAddIncome()
                    }
                )
                FabMenuItem(
                    label = "مصروف",
                    icon = Icons.Filled.Remove,
                    color = AppColors.Error,
                    onClick = {
                        onToggle()
                        onAddExpense()
                    }
                )
                FabMenuItem(
                    label = "تحويل",
                    icon = Icons.Filled.SwapHoriz,
                    color = AppColors.Primary,
                    onClick = {
                        onToggle()
                        onTransfer()
                    }
                )
                FabMenuItem(
                    label = "كشف حساب",
                    icon = Icons.Filled.Description,
                    color = AppColors.Info,
                    onClick = {
                        onToggle()
                        onGenerateStatement()
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        
        // Main FAB
        FloatingActionButton(
            onClick = onToggle,
            containerColor = if (expanded) AppColors.Error else AppColors.Primary
        ) {
            Icon(
                imageVector = if (expanded) Icons.Filled.Close else Icons.Filled.Add,
                contentDescription = if (expanded) "إغلاق" else "إجراءات"
            )
        }
    }
}

@Composable
private fun FabMenuItem(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = color.copy(alpha = 0.9f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = AppColors.White
            )
        }
    }
}

// =====================================================
// EDIT ACCOUNT BOTTOM SHEET
// =====================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAccountBottomSheet(
    account: Account,
    onDismiss: () -> Unit,
    onSave: (name: String, details: String?, bankName: String?, accountNumber: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(account.name) }
    var details by remember { mutableStateOf(account.details ?: "") }
    var bankName by remember { mutableStateOf(account.bankName ?: "") }
    var accountNumber by remember { mutableStateOf(account.accountNumber ?: "") }
    
    val isValid = name.isNotBlank()
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Text(
                text = "تعديل الحساب",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Name Field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("اسم الحساب") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = name.isBlank()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Details Field
            OutlinedTextField(
                value = details,
                onValueChange = { details = it },
                label = { Text("ملاحظات") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
            
            // Bank-specific fields
            if (account.type == AccountType.BANK) {
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = { Text("اسم البنك") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = { accountNumber = it },
                    label = { Text("رقم الحساب") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("إلغاء")
                }
                
                Button(
                    onClick = {
                        onSave(
                            name,
                            details.ifBlank { null },
                            bankName.ifBlank { null },
                            accountNumber.ifBlank { null }
                        )
                    },
                    modifier = Modifier.weight(1f),
                    enabled = isValid
                ) {
                    Text("حفظ")
                }
            }
        }
    }
}

// =====================================================
// EMPTY STATES
// =====================================================

@Composable
fun TransactionsEmptyState(
    onAddTransaction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.Receipt,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "لا توجد معاملات",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "أضف معاملة جديدة للبدء",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = onAddTransaction) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("إضافة معاملة")
        }
    }
}

@Composable
fun JournalEmptyState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.Book,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "لا توجد قيود يومية",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "ستظهر القيود هنا عند إجراء المعاملات",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}
