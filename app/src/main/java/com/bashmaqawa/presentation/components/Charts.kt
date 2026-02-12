package com.bashmaqawa.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bashmaqawa.data.database.dao.CategorySummary
import com.bashmaqawa.utils.CurrencyFormatter

@Composable
fun DonutChart(
    data: List<CategorySummary>,
    totalAmount: Double,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 30.dp,
    animDuration: Int = 1000
) {
    val nonZeroData = data.filter { it.total > 0 }
    val total = if (totalAmount > 0) totalAmount else 1.0
    
    val proportions = nonZeroData.map { (it.total / total).toFloat() }
    val angles = proportions.map { 360f * it }
    
    val animationProgress = remember { Animatable(0f) }
    
    LaunchedEffect(nonZeroData) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = animDuration,
                easing = FastOutSlowInEasing
            )
        )
    }
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(strokeWidth / 2)
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = (size.minDimension - strokeWidth.toPx()) / 2
            
            var startAngle = -90f
            
            nonZeroData.forEachIndexed { index, _ ->
                val sweepAngle = angles[index] * animationProgress.value
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
        if (totalAmount > 0) {
            Text(
                text = CurrencyFormatter.format(totalAmount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        } else {
             Text(
                text = "لا توجد بيانات",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
