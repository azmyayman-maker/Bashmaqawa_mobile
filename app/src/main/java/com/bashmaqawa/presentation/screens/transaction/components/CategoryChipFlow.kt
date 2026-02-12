package com.bashmaqawa.presentation.screens.transaction.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bashmaqawa.data.database.entities.TransactionType
import com.bashmaqawa.presentation.screens.transaction.TransactionCategory
import com.bashmaqawa.presentation.theme.AppColors

/**
 * Category Chip Flow
 * تدفق شرائح التصنيف
 * 
 * Displays categories in a flow layout with
 * animated selection states and emoji icons.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryChipFlow(
    categories: List<TransactionCategory>,
    selectedCategory: TransactionCategory?,
    onCategorySelected: (TransactionCategory) -> Unit,
    transactionType: TransactionType,
    hasError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "التصنيف",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        if (categories.isEmpty()) {
            // No categories for transfers
            Text(
                text = "لا يلزم تصنيف للتحويلات",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        } else {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    CategoryChip(
                        category = category,
                        isSelected = selectedCategory == category,
                        transactionType = transactionType,
                        onClick = {
                            if (enabled && selectedCategory != category) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onCategorySelected(category)
                            }
                        },
                        enabled = enabled
                    )
                }
            }
        }
        
        // Error message
        if (hasError && !errorMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.Error
            )
        }
    }
}

@Composable
private fun CategoryChip(
    category: TransactionCategory,
    isSelected: Boolean,
    transactionType: TransactionType,
    onClick: () -> Unit,
    enabled: Boolean
) {
    // Animate scale on selection
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "chipScale"
    )
    
    // Get color based on transaction type
    val typeColor = when (transactionType) {
        TransactionType.EXPENSE -> AppColors.Error
        TransactionType.INCOME -> AppColors.Success
        TransactionType.TRANSFER -> AppColors.Primary
    }
    
    // Animate colors
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) typeColor.copy(alpha = 0.15f) else 
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        animationSpec = tween(200),
        label = "containerColor"
    )
    
    val labelColor by animateColorAsState(
        targetValue = if (isSelected) typeColor else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(200),
        label = "labelColor"
    )
    
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = "${category.icon} ${category.arabicName}",
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = labelColor
            )
        },
        modifier = Modifier.scale(scale),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        border = FilterChipDefaults.filterChipBorder(
            enabled = enabled,
            selected = isSelected,
            borderColor = if (isSelected) typeColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            selectedBorderColor = typeColor,
            borderWidth = if (isSelected) 2.dp else 1.dp,
            selectedBorderWidth = 2.dp
        ),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = containerColor,
            selectedContainerColor = containerColor,
            labelColor = labelColor,
            selectedLabelColor = labelColor
        )
    )
}
