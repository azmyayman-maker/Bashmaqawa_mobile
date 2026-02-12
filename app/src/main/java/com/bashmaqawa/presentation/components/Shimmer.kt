package com.bashmaqawa.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Shimmer Loading Effect for Skeleton Placeholders
 * تأثير الوميض للتحميل
 */
@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    widthDp: Dp = 200.dp,
    heightDp: Dp = 20.dp,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp)
) {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 200f, translateAnim - 200f),
        end = Offset(translateAnim, translateAnim)
    )

    Box(
        modifier = modifier
            .width(widthDp)
            .height(heightDp)
            .clip(shape)
            .background(brush)
    )
}

/**
 * Card Skeleton Placeholder
 * بطاقة هيكلية للتحميل
 */
@Composable
fun CardSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ShimmerEffect(widthDp = 120.dp, heightDp = 20.dp)
            ShimmerEffect(widthDp = 60.dp, heightDp = 20.dp)
        }
        ShimmerEffect(widthDp = 180.dp, heightDp = 16.dp)
        ShimmerEffect(
            widthDp = 250.dp, 
            heightDp = 14.dp,
            modifier = Modifier.fillMaxWidth(0.8f)
        )
    }
}

/**
 * List Skeleton Placeholder
 * قائمة هيكلية للتحميل
 */
@Composable
fun ListSkeleton(
    itemCount: Int = 4,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(itemCount) {
            CardSkeleton()
        }
    }
}

/**
 * Stats Card Skeleton
 * بطاقة إحصائيات هيكلية
 */
@Composable
fun StatsCardSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ShimmerEffect(widthDp = 32.dp, heightDp = 32.dp, shape = RoundedCornerShape(8.dp))
        ShimmerEffect(widthDp = 80.dp, heightDp = 24.dp)
        ShimmerEffect(widthDp = 60.dp, heightDp = 14.dp)
    }
}

/**
 * Stats Row Skeleton (for HomeScreen)
 */
@Composable
fun StatsRowSkeleton(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(3) {
            StatsCardSkeleton(modifier = Modifier.weight(1f))
        }
    }
}
