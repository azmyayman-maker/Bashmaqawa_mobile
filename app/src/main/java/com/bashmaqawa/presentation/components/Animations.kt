package com.bashmaqawa.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Animation Utilities for Professional UX
 * أدوات الحركة لتجربة مستخدم احترافية
 */

/**
 * Fade-in animation wrapper
 * غلاف تأثير الظهور التدريجي
 */
@Composable
fun FadeInAnimated(
    modifier: Modifier = Modifier,
    delay: Int = 0,
    duration: Int = 300,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        visible = true
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = duration,
                delayMillis = delay,
                easing = EaseOutCubic
            )
        ) + slideInVertically(
            animationSpec = tween(
                durationMillis = duration,
                delayMillis = delay,
                easing = EaseOutCubic
            ),
            initialOffsetY = { it / 4 }
        ),
        modifier = modifier
    ) {
        content()
    }
}

/**
 * Staggered animation for list items
 * تأثير متتالي لعناصر القائمة
 */
@Composable
fun StaggeredAnimatedItem(
    index: Int,
    modifier: Modifier = Modifier,
    baseDelay: Int = 50,
    maxDelay: Int = 300,
    content: @Composable () -> Unit
) {
    val delay = (index * baseDelay).coerceAtMost(maxDelay)
    FadeInAnimated(
        delay = delay,
        modifier = modifier
    ) {
        content()
    }
}

/**
 * Scale animation on press
 * تأثير التكبير عند الضغط
 */
@Composable
fun ScaleOnPress(
    pressed: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scalePress"
    )
    
    Box(
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    ) {
        content()
    }
}

/**
 * Pulse animation for highlights
 * تأثير النبض للإبرازات
 */
@Composable
fun PulseAnimation(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    
    Box(
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    ) {
        content()
    }
}

/**
 * Bounce animation for success states
 * تأثير الارتداد لحالات النجاح
 */
@Composable
fun BounceAnimation(
    trigger: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (trigger) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bounce"
    )
    
    Box(
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    ) {
        content()
    }
}

/**
 * Slide animation from side
 * تأثير الانزلاق من الجانب
 */
@Composable
fun SlideInAnimated(
    visible: Boolean,
    modifier: Modifier = Modifier,
    fromRight: Boolean = true,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200)) + slideInHorizontally(
            animationSpec = tween(300, easing = EaseOutCubic),
            initialOffsetX = { if (fromRight) it else -it }
        ),
        exit = fadeOut(tween(200)) + slideOutHorizontally(
            animationSpec = tween(300, easing = EaseInCubic),
            targetOffsetX = { if (fromRight) it else -it }
        ),
        modifier = modifier
    ) {
        content()
    }
}

// Custom easing curves
private val EaseOutCubic = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)
private val EaseInCubic = CubicBezierEasing(0.32f, 0f, 0.67f, 0f)
