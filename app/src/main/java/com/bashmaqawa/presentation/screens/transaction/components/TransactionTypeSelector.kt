package com.bashmaqawa.presentation.screens.transaction.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bashmaqawa.data.database.entities.TransactionType
import com.bashmaqawa.presentation.theme.AppColors

/**
 * Animated Transaction Type Selector
 * محدد نوع المعاملة المتحرك
 * 
 * A 3-way toggle for selecting transaction type:
 * - Expense (مصروف) - Red
 * - Income (إيراد) - Green
 * - Transfer (تحويل) - Blue
 */
@Composable
fun TransactionTypeSelector(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "نوع المعاملة",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TransactionType.entries.forEach { type ->
                TransactionTypeButton(
                    type = type,
                    isSelected = selectedType == type,
                    onClick = {
                        if (enabled && selectedType != type) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onTypeSelected(type)
                        }
                    },
                    enabled = enabled,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TransactionTypeButton(
    type: TransactionType,
    isSelected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val config = getTypeConfig(type)
    
    // Animate scale on selection
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    // Animate background color
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) config.color.copy(alpha = 0.15f) else Color.Transparent,
        label = "bgColor"
    )
    
    // Animate border color
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) config.color else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        label = "borderColor"
    )
    
    // Animate content color
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) config.color else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "contentColor"
    )
    
    Surface(
        onClick = onClick,
        modifier = modifier.scale(scale),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        ),
        enabled = enabled
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = config.icon,
                contentDescription = config.arabicName,
                tint = contentColor,
                modifier = Modifier.size(28.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = config.arabicName,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

/**
 * Configuration for each transaction type
 */
private data class TypeConfig(
    val arabicName: String,
    val englishName: String,
    val icon: ImageVector,
    val color: Color
)

private fun getTypeConfig(type: TransactionType): TypeConfig {
    return when (type) {
        TransactionType.EXPENSE -> TypeConfig(
            arabicName = "مصروف",
            englishName = "Expense",
            icon = Icons.Filled.ArrowDownward,
            color = AppColors.Error
        )
        TransactionType.INCOME -> TypeConfig(
            arabicName = "إيراد",
            englishName = "Income",
            icon = Icons.Filled.ArrowUpward,
            color = AppColors.Success
        )
        TransactionType.TRANSFER -> TypeConfig(
            arabicName = "تحويل",
            englishName = "Transfer",
            icon = Icons.Filled.SwapHoriz,
            color = AppColors.Primary
        )
    }
}
