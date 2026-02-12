package com.bashmaqawa.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bashmaqawa.presentation.theme.CustomShapes

/**
 * Glassmorphism Card Component
 * بطاقة بتأثير الزجاج المضبب
 * 
 * Creates a frosted glass effect with semi-transparent background,
 * subtle blur, and light border for a premium modern look.
 */
@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White.copy(alpha = 0.15f),
    borderColor: Color = Color.White.copy(alpha = 0.30f),
    borderWidth: Dp = 1.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(CustomShapes.Card)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor,
                        backgroundColor.copy(alpha = backgroundColor.alpha * 0.7f)
                    )
                )
            )
            .border(
                width = borderWidth,
                color = borderColor,
                shape = CustomShapes.Card
            )
    ) {
        content()
    }
}

/**
 * Glassmorphic Card with gradient background
 * بطاقة زجاجية مع تدرج لوني في الخلفية
 */
@Composable
fun GlassmorphicGradientCard(
    modifier: Modifier = Modifier,
    gradientColors: List<Color>,
    overlayAlpha: Float = 0.20f,
    borderColor: Color = Color.White.copy(alpha = 0.25f),
    borderWidth: Dp = 1.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(CustomShapes.Card)
            .background(
                brush = Brush.linearGradient(colors = gradientColors)
            )
    ) {
        // Glassmorphic overlay
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = overlayAlpha),
                            Color.Transparent
                        )
                    )
                )
                .border(
                    width = borderWidth,
                    color = borderColor,
                    shape = CustomShapes.Card
                )
        )
        content()
    }
}

/**
 * Dark mode compatible glassmorphic card
 * بطاقة زجاجية متوافقة مع الوضع الداكن
 */
@Composable
fun AdaptiveGlassmorphicCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    
    GlassmorphicCard(
        modifier = modifier,
        backgroundColor = backgroundColor,
        borderColor = borderColor,
        content = content
    )
}
