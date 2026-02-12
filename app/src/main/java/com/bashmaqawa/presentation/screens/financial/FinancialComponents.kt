package com.bashmaqawa.presentation.screens.financial

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bashmaqawa.R
import com.bashmaqawa.data.database.dao.TransactionWithDetails
import com.bashmaqawa.presentation.theme.AppColors
import com.bashmaqawa.utils.CurrencyFormatter

// =====================================================
// GLASSMORPHIC DESIGN SYSTEM
// =====================================================

/**
 * Base Glassmorphic Card with blur effect, translucent background, and subtle border
 * بطاقة زجاجية مع تأثير الضبابية والخلفية الشفافة
 */
@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
    borderColor: Color = Color.White.copy(alpha = 0.2f),
    borderWidth: Dp = 1.dp,
    cornerRadius: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(cornerRadius)
            ),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(content = content)
    }
}

/**
 * Premium gradient card for financial metrics
 * بطاقة متدرجة للمقاييس المالية
 */
@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    gradientColors: List<Color> = listOf(AppColors.Primary, AppColors.PrimaryDark),
    cornerRadius: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .background(Brush.linearGradient(gradientColors))
        ) {
            Column(content = content)
        }
    }
}

// =====================================================
// FINANCIAL DASHBOARD CARDS
// =====================================================

/**
 * Main Financial Overview Card with total balance and trend indicator
 * بطاقة النظرة العامة المالية مع الرصيد والمؤشر
 */
@Composable
fun FinancialOverviewCard(
    totalBalance: Double,
    trendPercentage: Float,
    isPositiveTrend: Boolean,
    modifier: Modifier = Modifier
) {
    GradientCard(
        modifier = modifier.fillMaxWidth(),
        gradientColors = if (isPositiveTrend) 
            listOf(AppColors.Success, Color(0xFF2E7D32))
        else 
            listOf(AppColors.Primary, AppColors.PrimaryDark)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.total),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = CurrencyFormatter.format(totalBalance),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Trend Indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isPositiveTrend) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                    contentDescription = null,
                    tint = if (isPositiveTrend) Color.White else Color(0xFFFFCDD2),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${if (isPositiveTrend) "+" else ""}${String.format("%.1f", trendPercentage)}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = " مقارنة بالشهر السابق",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * Net Worth Card showing Assets vs Liabilities
 * بطاقة صافي القيمة - الأصول مقابل الخصوم
 */
@Composable
fun NetWorthCard(
    totalAssets: Double,
    totalLiabilities: Double,
    modifier: Modifier = Modifier
) {
    val netWorth = totalAssets - totalLiabilities
    
    GlassmorphicCard(
        modifier = modifier,
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.financial_net_worth),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.Outlined.AccountBalanceWallet,
                    contentDescription = null,
                    tint = AppColors.Primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Assets Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.financial_assets),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = CurrencyFormatter.format(totalAssets),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.Success
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Liabilities Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.financial_liabilities),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = CurrencyFormatter.format(totalLiabilities),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.Error
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            HorizontalDivider()
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Net Worth
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.net),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = CurrencyFormatter.format(netWorth),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (netWorth >= 0) AppColors.Success else AppColors.Error
                )
            }
        }
    }
}

/**
 * Cash Flow Card with Income vs Expense progress visualization
 * بطاقة التدفق النقدي مع تصور الإيرادات والمصروفات
 */
@Composable
fun CashFlowCard(
    income: Double,
    expenses: Double,
    modifier: Modifier = Modifier
) {
    val total = income + expenses
    val incomeRatio = if (total > 0) (income / total).toFloat() else 0.5f
    
    GlassmorphicCard(
        modifier = modifier,
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.financial_cash_flow),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.financial_this_month),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress Bar
            CashFlowProgressBar(
                incomeRatio = incomeRatio,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Income
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(AppColors.Success)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.income),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = CurrencyFormatter.format(income),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.Success
                        )
                    }
                }
                
                // Expenses
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(AppColors.Error)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = stringResource(R.string.expense),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = CurrencyFormatter.format(expenses),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.Error
                        )
                    }
                }
            }
        }
    }
}

/**
 * Animated progress bar for cash flow visualization
 */
@Composable
private fun CashFlowProgressBar(
    incomeRatio: Float,
    modifier: Modifier = Modifier
) {
    val animatedRatio by animateFloatAsState(
        targetValue = incomeRatio,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "cashflow_animation"
    )
    
    Box(
        modifier = modifier
            .height(12.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(AppColors.Error.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedRatio)
                .clip(RoundedCornerShape(6.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(AppColors.Success, AppColors.Success.copy(alpha = 0.8f))
                    )
                )
        )
    }
}

// =====================================================
// QUICK ACTIONS
// =====================================================

/**
 * Quick Actions Row with circular buttons
 * صف الإجراءات السريعة
 */
@Composable
fun QuickActionsRow(
    onNewExpense: () -> Unit,
    onTransfer: () -> Unit,
    onNewIncome: () -> Unit,
    onReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        QuickActionButton(
            icon = Icons.Filled.RemoveCircleOutline,
            label = stringResource(R.string.financial_new_expense),
            color = AppColors.Error,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onNewExpense()
            }
        )
        QuickActionButton(
            icon = Icons.Filled.SwapHoriz,
            label = stringResource(R.string.financial_transfer),
            color = AppColors.Primary,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onTransfer()
            }
        )
        QuickActionButton(
            icon = Icons.Filled.AddCircleOutline,
            label = stringResource(R.string.financial_new_income),
            color = AppColors.Success,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onNewIncome()
            }
        )
        QuickActionButton(
            icon = Icons.Filled.Assessment,
            label = stringResource(R.string.financial_report),
            color = AppColors.Info,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onReport()
            }
        )
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            color = color.copy(alpha = 0.15f),
            border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// =====================================================
// CHARTS
// =====================================================

/**
 * Animated Line Chart for trend visualization
 * مخطط خطي متحرك لتصور الاتجاهات
 */
@Composable
fun TrendLineChart(
    incomeData: List<Float>,
    expenseData: List<Float>,
    modifier: Modifier = Modifier
) {
    val animationProgress = remember { Animatable(0f) }
    
    LaunchedEffect(incomeData, expenseData) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
        )
    }
    
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
    ) {
        val width = size.width
        val height = size.height
        val padding = 16.dp.toPx()
        
        if (incomeData.isEmpty() || expenseData.isEmpty()) return@Canvas
        
        val maxValue = maxOf(incomeData.maxOrNull() ?: 0f, expenseData.maxOrNull() ?: 0f)
        if (maxValue == 0f) return@Canvas
        
        val stepX = (width - padding * 2) / (incomeData.size - 1).coerceAtLeast(1)
        
        // Draw Income Line (Green)
        val incomePath = Path()
        incomeData.forEachIndexed { index, value ->
            val x = padding + index * stepX
            val y = height - padding - (value / maxValue) * (height - padding * 2)
            val animatedX = padding + (x - padding) * animationProgress.value
            
            if (index == 0) {
                incomePath.moveTo(animatedX, y)
            } else {
                incomePath.lineTo(animatedX, y)
            }
        }
        drawPath(
            path = incomePath,
            color = AppColors.Success,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
        
        // Draw Expense Line (Red)
        val expensePath = Path()
        expenseData.forEachIndexed { index, value ->
            val x = padding + index * stepX
            val y = height - padding - (value / maxValue) * (height - padding * 2)
            val animatedX = padding + (x - padding) * animationProgress.value
            
            if (index == 0) {
                expensePath.moveTo(animatedX, y)
            } else {
                expensePath.lineTo(animatedX, y)
            }
        }
        drawPath(
            path = expensePath,
            color = AppColors.Error,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

/**
 * Enhanced Donut Chart with animation and center label
 * مخطط دائري محسن مع الرسوم المتحركة
 */
@Composable
fun EnhancedDonutChart(
    data: List<Pair<String, Double>>,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 30.dp
) {
    val total = data.sumOf { it.second }
    val animationProgress = remember { Animatable(0f) }
    
    LaunchedEffect(data) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }
    
    Box(
        modifier = modifier.aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(strokeWidth / 2)
        ) {
            val radius = (size.minDimension - strokeWidth.toPx()) / 2
            val center = Offset(size.width / 2, size.height / 2)
            
            var startAngle = -90f
            
            data.forEachIndexed { index, (_, value) ->
                val sweepAngle = (value / total * 360f).toFloat() * animationProgress.value
                val color = colors.getOrElse(index) { Color.Gray }
                
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                )
                
                startAngle += sweepAngle
            }
        }
        
        // Center Text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = CurrencyFormatter.format(total),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.total),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// =====================================================
// TRANSACTION LIST COMPONENTS
// =====================================================

/**
 * Time Range Filter Chips
 * فلاتر النطاق الزمني
 */
@Composable
fun TimeRangeFilterRow(
    selectedRange: TimeRange,
    onRangeSelected: (TimeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TimeRange.entries.forEach { range ->
            FilterChip(
                selected = selectedRange == range,
                onClick = { onRangeSelected(range) },
                label = {
                    Text(
                        when (range) {
                            TimeRange.WEEK -> stringResource(R.string.view_week)
                            TimeRange.MONTH -> stringResource(R.string.view_month)
                            TimeRange.YEAR -> stringResource(R.string.financial_year)
                        }
                    )
                }
            )
        }
    }
}

/**
 * Transaction Filter Chips
 * فلاتر المعاملات
 */
@Composable
fun TransactionFilterChipRow(
    selectedFilter: TransactionFilter,
    onFilterSelected: (TransactionFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(TransactionFilter.entries) { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onFilterSelected(filter) 
                },
                label = {
                    Text(
                        when (filter) {
                            TransactionFilter.ALL -> stringResource(R.string.financial_filter_all)
                            TransactionFilter.INCOME -> stringResource(R.string.income)
                            TransactionFilter.EXPENSE -> stringResource(R.string.expense)
                            TransactionFilter.BY_PROJECT -> stringResource(R.string.financial_by_project)
                            TransactionFilter.BY_WORKER -> stringResource(R.string.financial_by_worker)
                        }
                    )
                },
                leadingIcon = if (selectedFilter == filter) {
                    { Icon(Icons.Filled.Check, null, modifier = Modifier.size(18.dp)) }
                } else null
            )
        }
    }
}

/**
 * Sticky Date Header for transaction groups
 * رأس تاريخ ثابت لمجموعات المعاملات
 */
@Composable
fun TransactionDateHeader(
    dateLabel: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    ) {
        Text(
            text = dateLabel,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

/**
 * Premium Transaction Item with category icon and rich details
 * عنصر معاملة متميز مع أيقونة الفئة والتفاصيل
 */
@Composable
fun PremiumTransactionItem(
    transaction: TransactionWithDetails,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    animationDelay: Int = 0
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(animationDelay.toLong())
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
            initialOffsetY = { 50 },
            animationSpec = tween(300)
        )
    ) {
        val isIncome = transaction.type == "INCOME"
        val categoryIcon = getCategoryIcon(transaction.category ?: "")
        val categoryColor = if (isIncome) AppColors.Success else AppColors.Error
        
        Card(
            modifier = modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Icon
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = categoryColor.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = null,
                            tint = categoryColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.description ?: transaction.category ?: stringResource(R.string.transactions),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Project or Worker info
                        transaction.projectName?.let { projectName ->
                            Icon(
                                imageVector = Icons.Outlined.Business,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = projectName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        transaction.workerName?.let { workerName ->
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = workerName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                
                // Amount
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${if (isIncome) "+" else "-"} ${CurrencyFormatter.format(transaction.amount ?: 0.0)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = categoryColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = transaction.date ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Get appropriate icon for transaction category
 * الحصول على الأيقونة المناسبة لفئة المعاملة
 */
fun getCategoryIcon(category: String): ImageVector {
    return when (category.lowercase()) {
        "material", "مواد" -> Icons.Filled.Inventory2
        "wages", "أجور" -> Icons.Filled.People
        "transport", "نقل" -> Icons.Filled.LocalShipping
        "equipment", "معدات" -> Icons.Filled.Construction
        "rent", "إيجارات" -> Icons.Filled.Home
        "services", "خدمات" -> Icons.Filled.Handyman
        "maintenance", "صيانة" -> Icons.Filled.Build
        "advance", "سلف" -> Icons.Filled.MoneyOff
        "payment", "دفعة" -> Icons.Filled.Payments
        "deposit", "إيداع" -> Icons.Filled.AccountBalance
        "invoice", "مستخلص" -> Icons.Filled.Receipt
        else -> Icons.Filled.AttachMoney
    }
}

// =====================================================
// ENUMS
// =====================================================

enum class TimeRange {
    WEEK, MONTH, YEAR
}

enum class TransactionFilter {
    ALL, INCOME, EXPENSE, BY_PROJECT, BY_WORKER
}
