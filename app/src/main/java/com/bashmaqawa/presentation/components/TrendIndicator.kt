package com.bashmaqawa.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bashmaqawa.presentation.theme.AppColors

/**
 * Trend Indicator Component
 * مؤشر الاتجاه (صعود/هبوط)
 * 
 * Shows percentage change with color-coded arrow indicator.
 * Green for positive, red for negative, gray for neutral.
 */
@Composable
fun TrendIndicator(
    percentage: Double,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true,
    label: String = "عن الشهر الماضي",
    animated: Boolean = true
) {
    val trendColor = when {
        percentage > 0 -> AppColors.Success
        percentage < 0 -> AppColors.Error
        else -> AppColors.Gray500
    }
    
    val trendIcon = when {
        percentage > 0 -> Icons.Filled.TrendingUp
        percentage < 0 -> Icons.Filled.TrendingDown
        else -> Icons.Filled.TrendingFlat
    }
    
    // Animation for icon entrance
    var visible by remember { mutableStateOf(!animated) }
    LaunchedEffect(Unit) {
        visible = true
    }
    
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "trendScale"
    )
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = trendIcon,
            contentDescription = null,
            tint = trendColor,
            modifier = Modifier
                .size(16.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
        )
        
        Text(
            text = formatPercentage(percentage),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = trendColor
        )
        
        if (showLabel) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Trend Badge - Compact version for cards
 * شارة الاتجاه - نسخة مضغوطة للبطاقات
 */
@Composable
fun TrendBadge(
    percentage: Double,
    modifier: Modifier = Modifier
) {
    val trendColor = when {
        percentage > 0 -> AppColors.Success
        percentage < 0 -> AppColors.Error
        else -> AppColors.Gray500
    }
    
    val backgroundColor = trendColor.copy(alpha = 0.1f)
    
    val trendIcon = when {
        percentage > 0 -> Icons.Filled.TrendingUp
        percentage < 0 -> Icons.Filled.TrendingDown
        else -> Icons.Filled.TrendingFlat
    }
    
    Row(
        modifier = modifier
            .wrapContentSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            imageVector = trendIcon,
            contentDescription = null,
            tint = trendColor,
            modifier = Modifier.size(14.dp)
        )
        
        Text(
            text = formatPercentage(percentage),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = trendColor
        )
    }
}

/**
 * Large Trend Display for Hero Cards
 * عرض كبير للاتجاه في البطاقات الرئيسية
 */
@Composable
fun HeroTrendIndicator(
    percentage: Double,
    label: String = "عن الشهر الماضي",
    modifier: Modifier = Modifier,
    textColor: Color = Color.White.copy(alpha = 0.9f)
) {
    val trendIcon = when {
        percentage > 0 -> Icons.Filled.TrendingUp
        percentage < 0 -> Icons.Filled.TrendingDown
        else -> Icons.Filled.TrendingFlat
    }
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = trendIcon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(18.dp)
        )
        
        Text(
            text = "${formatPercentage(percentage)} $label",
            style = MaterialTheme.typography.labelMedium,
            color = textColor
        )
    }
}

// تنسيق النسبة المئوية - Format percentage
private fun formatPercentage(value: Double): String {
    val prefix = if (value > 0) "+" else ""
    return "$prefix%.1f%%".format(kotlin.math.abs(value))
}
