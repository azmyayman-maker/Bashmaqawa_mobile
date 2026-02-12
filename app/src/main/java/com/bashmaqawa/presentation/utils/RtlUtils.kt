package com.bashmaqawa.presentation.utils

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * RTL-aware utility functions for consistent directional logic
 * أدوات مساعدة للتوجيه من اليمين لليسار
 * 
 * These utilities help ensure consistent RTL behavior across all screens,
 * particularly important for Arabic localization.
 */
object RtlUtils {
    
    /**
     * Check if current layout direction is RTL
     * التحقق من اتجاه التخطيط للعربية
     */
    @Composable
    fun isRtl(): Boolean = LocalLayoutDirection.current == LayoutDirection.Rtl
    
    /**
     * Get the correct start padding based on layout direction
     * For RTL: start = right side
     * For LTR: start = left side
     */
    @Composable
    fun getStartPadding(value: Dp): Dp = value
    
    /**
     * Get the correct end padding based on layout direction
     */
    @Composable
    fun getEndPadding(value: Dp): Dp = value
}

/**
 * Extension function for RTL-aware padding
 * Compose automatically handles start/end mirroring in RTL layouts
 * This explicit extension serves as documentation and ensures intentional usage
 * 
 * @param start Start padding (right in RTL, left in LTR)
 * @param top Top padding
 * @param end End padding (left in RTL, right in LTR)
 * @param bottom Bottom padding
 */
fun Modifier.rtlAwarePadding(
    start: Dp = 0.dp,
    top: Dp = 0.dp,
    end: Dp = 0.dp,
    bottom: Dp = 0.dp
): Modifier = this.padding(
    start = start,  // Compose auto-mirrors in RTL context
    top = top,
    end = end,
    bottom = bottom
)

/**
 * Extension function for symmetric horizontal RTL-aware padding
 */
fun Modifier.rtlAwareHorizontalPadding(horizontal: Dp): Modifier = this.padding(
    start = horizontal,
    end = horizontal
)
