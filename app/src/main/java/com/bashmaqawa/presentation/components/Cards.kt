package com.bashmaqawa.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bashmaqawa.presentation.theme.AppColors
import com.bashmaqawa.presentation.theme.CustomShapes

/**
 * Standardized Card Defaults for consistent UI across all screens
 * الإعدادات الافتراضية الموحدة للبطاقات
 * 
 * Use these defaults for all cards to ensure visual consistency:
 * - Elevation: 2.dp
 * - Shape: 16.dp rounded corners
 * - Colors: Surface-based for theme compatibility
 */
object AppCardDefaults {
    val Elevation = 2.dp
    val Shape = CustomShapes.Card  // 16.dp border-radius
    val RoundedShape = CustomShapes.Card
    
    @Composable
    fun colors() = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
    )
    
    @Composable
    fun elevation() = CardDefaults.cardElevation(defaultElevation = Elevation)
}

/**
 * Gradient Info Card Component
 * بطاقة معلومات بتدرج لوني
 */
@Composable
fun InfoCard(
    title: String,
    value: String,
    icon: ImageVector,
    gradient: List<Color> = AppColors.PrimaryGradient,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val cardModifier = if (onClick != null) {
        modifier
            .clip(CustomShapes.Card)
            .background(Brush.linearGradient(gradient))
    } else {
        modifier
            .clip(CustomShapes.Card)
            .background(Brush.linearGradient(gradient))
    }
    
    Surface(
        modifier = modifier,
        shape = CustomShapes.Card,
        color = Color.Transparent,
        onClick = onClick ?: {}
    ) {
        Box(
            modifier = Modifier
                .background(Brush.linearGradient(gradient))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

/**
 * Stats Card with Icon
 * بطاقة إحصائيات مع أيقونة
 */
@Composable
fun StatsCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconTint: Color = AppColors.Primary,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier,
        shape = CustomShapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick ?: {}
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CustomShapes.Card)
                    .background(iconTint.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Quick Action Card
 * بطاقة إجراء سريع
 */
@Composable
fun QuickActionCard(
    title: String,
    icon: ImageVector,
    iconTint: Color = AppColors.Primary,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        shape = CustomShapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CustomShapes.Card)
                    .background(iconTint.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
