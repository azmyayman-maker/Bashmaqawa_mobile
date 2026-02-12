package com.bashmaqawa.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.bashmaqawa.utils.CurrencyFormatter
import java.text.NumberFormat
import java.util.Locale

/**
 * Animated Counter Component
 * عداد متحرك للأرقام
 * 
 * Animates number changes with smooth spring animation.
 * Supports currency formatting and RTL display.
 */
@Composable
fun AnimatedCounter(
    targetValue: Double,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineMedium,
    fontWeight: FontWeight = FontWeight.Bold,
    color: Color = MaterialTheme.colorScheme.onSurface,
    isCurrency: Boolean = true,
    animationSpec: AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
) {
    // Animate the value
    val animatedValue by animateFloatAsState(
        targetValue = targetValue.toFloat(),
        animationSpec = animationSpec,
        label = "counterAnimation"
    )
    
    val displayText = if (isCurrency) {
        CurrencyFormatter.format(animatedValue.toDouble())
    } else {
        formatNumber(animatedValue.toDouble())
    }
    
    Text(
        text = displayText,
        style = style,
        fontWeight = fontWeight,
        color = color,
        modifier = modifier
    )
}

/**
 * Animated Integer Counter
 * عداد متحرك للأعداد الصحيحة
 */
@Composable
fun AnimatedIntCounter(
    targetValue: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineMedium,
    fontWeight: FontWeight = FontWeight.Bold,
    color: Color = MaterialTheme.colorScheme.onSurface,
    suffix: String = "",
    animationSpec: AnimationSpec<Int> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
) {
    val animatedValue by animateIntAsState(
        targetValue = targetValue,
        animationSpec = animationSpec,
        label = "intCounterAnimation"
    )
    
    Text(
        text = "${formatInteger(animatedValue)}$suffix",
        style = style,
        fontWeight = fontWeight,
        color = color,
        modifier = modifier
    )
}

/**
 * Percentage Counter with Animation
 * عداد نسبة مئوية متحرك
 */
@Composable
fun AnimatedPercentage(
    targetValue: Float,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.labelMedium,
    fontWeight: FontWeight = FontWeight.Medium,
    color: Color = MaterialTheme.colorScheme.onSurface,
    showPlusSign: Boolean = true
) {
    val animatedValue by animateFloatAsState(
        targetValue = targetValue,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "percentageAnimation"
    )
    
    val prefix = when {
        showPlusSign && animatedValue > 0 -> "+"
        else -> ""
    }
    
    Text(
        text = "$prefix%.1f%%".format(animatedValue),
        style = style,
        fontWeight = fontWeight,
        color = color,
        modifier = modifier
    )
}

// تنسيق الأرقام بالفواصل - Format numbers with separators
private fun formatNumber(value: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("ar", "EG"))
    formatter.maximumFractionDigits = 2
    formatter.minimumFractionDigits = 0
    return formatter.format(value)
}

private fun formatInteger(value: Int): String {
    val formatter = NumberFormat.getNumberInstance(Locale("ar", "EG"))
    return formatter.format(value)
}
