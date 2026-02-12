package com.bashmaqawa.presentation.screens.home.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bashmaqawa.presentation.screens.home.CashFlowPoint
import com.bashmaqawa.presentation.theme.AppColors
import com.bashmaqawa.presentation.theme.CustomShapes
import com.bashmaqawa.utils.CurrencyFormatter

/**
 * Cash Flow Chart - 7 Day Visualization
 * الرسم البياني للتدفق النقدي - آخر 7 أيام
 * 
 * Displays income (green) vs expense (red) bars for each day.
 */
@Composable
fun CashFlowChart(
    data: List<CashFlowPoint>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    if (data.isEmpty()) return
    
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
            Text(
                text = "📈 التدفق النقدي - آخر 7 أيام",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Chart
            CashFlowBarChart(
                data = data,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )
            
            // Legend
            CashFlowLegend(data = data)
        }
    }
}

/**
 * Bar Chart Canvas Implementation
 * تنفيذ الرسم البياني بالأعمدة
 */
@Composable
private fun CashFlowBarChart(
    data: List<CashFlowPoint>,
    modifier: Modifier = Modifier
) {
    // Animation
    var animationPlayed by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "chartAnimation"
    )
    
    LaunchedEffect(Unit) {
        animationPlayed = true
    }
    
    val maxValue = data.maxOfOrNull { maxOf(it.income, it.expense) } ?: 1.0
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(
        fontSize = 10.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    
    val incomeColor = AppColors.Success
    val expenseColor = AppColors.Error
    
    Canvas(modifier = modifier) {
        val chartWidth = size.width
        val chartHeight = size.height - 30.dp.toPx() // Leave space for labels
        val barGroupWidth = chartWidth / data.size
        val barWidth = barGroupWidth * 0.35f
        val barSpacing = barGroupWidth * 0.05f
        
        data.forEachIndexed { index, point ->
            val groupStartX = index * barGroupWidth + barGroupWidth * 0.125f
            
            // Income bar (green)
            val incomeHeight = ((point.income / maxValue) * chartHeight * animatedProgress).toFloat()
            drawRoundRect(
                color = incomeColor,
                topLeft = Offset(
                    x = groupStartX,
                    y = chartHeight - incomeHeight
                ),
                size = Size(barWidth, incomeHeight),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
            
            // Expense bar (red)
            val expenseHeight = ((point.expense / maxValue) * chartHeight * animatedProgress).toFloat()
            drawRoundRect(
                color = expenseColor,
                topLeft = Offset(
                    x = groupStartX + barWidth + barSpacing,
                    y = chartHeight - expenseHeight
                ),
                size = Size(barWidth, expenseHeight),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
            
            // Day label
            val labelText = point.dayName.take(1) // First letter only
            val textLayoutResult = textMeasurer.measure(labelText, labelStyle)
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(
                    x = groupStartX + barWidth - textLayoutResult.size.width / 2,
                    y = chartHeight + 8.dp.toPx()
                )
            )
        }
    }
}

/**
 * Chart Legend with Totals
 * مفتاح الرسم مع الإجماليات
 */
@Composable
private fun CashFlowLegend(
    data: List<CashFlowPoint>,
    modifier: Modifier = Modifier
) {
    val totalIncome = data.sumOf { it.income }
    val totalExpense = data.sumOf { it.expense }
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        LegendItem(
            color = AppColors.Success,
            label = "إيرادات",
            value = CurrencyFormatter.format(totalIncome)
        )
        
        LegendItem(
            color = AppColors.Error,
            label = "مصروفات",
            value = CurrencyFormatter.format(totalExpense)
        )
    }
}

/**
 * Legend Item
 * عنصر المفتاح
 */
@Composable
private fun LegendItem(
    color: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, shape = CustomShapes.Card)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Compact Cash Flow Summary (Alternative)
 * ملخص تدفق نقدي مضغوط
 */
@Composable
fun CompactCashFlowSummary(
    totalIncome: Double,
    totalExpense: Double,
    modifier: Modifier = Modifier
) {
    val netFlow = totalIncome - totalExpense
    val netColor = if (netFlow >= 0) AppColors.Success else AppColors.Error
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CustomShapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
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
                    text = "صافي التدفق النقدي",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = CurrencyFormatter.format(netFlow),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = netColor
                )
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "إيرادات",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.Success
                    )
                    Text(
                        text = CurrencyFormatter.format(totalIncome),
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.Success
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "مصروفات",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.Error
                    )
                    Text(
                        text = CurrencyFormatter.format(totalExpense),
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.Error
                    )
                }
            }
        }
    }
}
