package com.bashmaqawa.presentation.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bashmaqawa.presentation.theme.AppColors

/**
 * Dynamic Empty State Configuration
 * تكوين حالة الفراغ الديناميكية
 */
data class EmptyStateConfig(
    val icon: ImageVector,
    val title: String,
    val message: String,
    val actionText: String? = null
)

/**
 * Enhanced Empty State Widget with Animation
 * مكون حالة الفراغ المحسن مع الرسوم المتحركة
 * 
 * This composable provides a consistent empty state UI across all screens.
 * It animates when the tab/content changes, providing smooth transitions.
 * 
 * @param config Configuration containing icon, title, message, and optional action
 * @param modifier Modifier for styling
 * @param onAction Callback when action button is clicked
 * @param tabIndex Optional tab index for animation triggering
 */
@Composable
fun DynamicEmptyState(
    config: EmptyStateConfig,
    modifier: Modifier = Modifier,
    onAction: (() -> Unit)? = null,
    tabIndex: Int = 0
) {
    // Animate when tab changes
    var animationKey by remember { mutableStateOf(tabIndex) }
    val animatedAlpha by animateFloatAsState(
        targetValue = if (animationKey == tabIndex) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "emptyStateAlpha"
    )
    
    LaunchedEffect(tabIndex) {
        animationKey = tabIndex
    }
    
    AnimatedContent(
        targetState = config,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) togetherWith
            fadeOut(animationSpec = tween(300))
        },
        modifier = modifier,
        label = "emptyStateContent"
    ) { currentConfig ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .alpha(animatedAlpha),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Icon with pulse effect
            val infiniteTransition = rememberInfiniteTransition(label = "iconPulse")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = EaseInOutCubic),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "iconScale"
            )
            
            Icon(
                imageVector = currentConfig.icon,
                contentDescription = null,
                modifier = Modifier
                    .size((64 * scale).dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = currentConfig.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = currentConfig.message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (currentConfig.actionText != null && onAction != null) {
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Primary
                    )
                ) {
                    Text(currentConfig.actionText)
                }
            }
        }
    }
}

/**
 * Get Empty State configuration for Projects based on tab index
 * الحصول على تكوين حالة الفراغ للمشاريع بناءً على فهرس التبويب
 */
fun getProjectsEmptyConfig(tabIndex: Int, icons: ProjectEmptyIcons): EmptyStateConfig {
    return when (tabIndex) {
        0 -> EmptyStateConfig( // All
            icon = icons.all,
            title = "لا توجد مشاريع",
            message = "اضغط + لإضافة مشروع جديد",
            actionText = "إضافة مشروع"
        )
        1 -> EmptyStateConfig( // Ongoing
            icon = icons.ongoing,
            title = "لا توجد مشاريع جارية",
            message = "المشاريع الجارية ستظهر هنا",
            actionText = null
        )
        2 -> EmptyStateConfig( // Completed
            icon = icons.completed,
            title = "لا توجد مشاريع مكتملة",
            message = "المشاريع المكتملة ستظهر هنا",
            actionText = null
        )
        3 -> EmptyStateConfig( // On Hold
            icon = icons.onHold,
            title = "لا توجد مشاريع متوقفة",
            message = "المشاريع المتوقفة ستظهر هنا",
            actionText = null
        )
        else -> EmptyStateConfig(
            icon = icons.all,
            title = "لا توجد بيانات",
            message = "لا توجد بيانات للعرض",
            actionText = null
        )
    }
}

/**
 * Get Empty State configuration for Workforce based on tab index
 */
fun getWorkforceEmptyConfig(tabIndex: Int, icons: WorkforceEmptyIcons): EmptyStateConfig {
    return when (tabIndex) {
        0 -> EmptyStateConfig( // Active
            icon = icons.active,
            title = "لا يوجد عمال نشطين",
            message = "اضغط + لإضافة عامل جديد",
            actionText = "إضافة عامل"
        )
        1 -> EmptyStateConfig( // Archived
            icon = icons.archived,
            title = "لا يوجد عمال مؤرشفين",
            message = "العمال المؤرشفون سيظهرون هنا",
            actionText = null
        )
        else -> EmptyStateConfig(
            icon = icons.active,
            title = "لا توجد بيانات",
            message = "لا توجد بيانات للعرض",
            actionText = null
        )
    }
}

/**
 * Get Empty State configuration for Financial based on tab index
 */
fun getFinancialEmptyConfig(tabIndex: Int, icons: FinancialEmptyIcons): EmptyStateConfig {
    return when (tabIndex) {
        0 -> EmptyStateConfig( // Accounts
            icon = icons.accounts,
            title = "لا توجد حسابات",
            message = "اضغط + لإضافة حساب جديد",
            actionText = "إضافة حساب"
        )
        1 -> EmptyStateConfig( // Transactions
            icon = icons.transactions,
            title = "لا توجد معاملات",
            message = "اضغط + لإضافة معاملة جديدة",
            actionText = "إضافة معاملة"
        )
        else -> EmptyStateConfig(
            icon = icons.accounts,
            title = "لا توجد بيانات",
            message = "لا توجد بيانات للعرض",
            actionText = null
        )
    }
}

/**
 * Icon holders for empty states
 */
data class ProjectEmptyIcons(
    val all: ImageVector,
    val ongoing: ImageVector,
    val completed: ImageVector,
    val onHold: ImageVector
)

data class WorkforceEmptyIcons(
    val active: ImageVector,
    val archived: ImageVector
)

data class FinancialEmptyIcons(
    val accounts: ImageVector,
    val transactions: ImageVector
)
