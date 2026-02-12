package com.bashmaqawa.presentation.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bashmaqawa.R
import com.bashmaqawa.presentation.components.AnimatedCounter
import com.bashmaqawa.presentation.components.GlassmorphicGradientCard
import com.bashmaqawa.presentation.components.HeroTrendIndicator
import com.bashmaqawa.presentation.screens.home.FinancialState
import com.bashmaqawa.presentation.screens.home.HeaderState
import com.bashmaqawa.presentation.theme.AppColors
import com.bashmaqawa.presentation.theme.CustomShapes

/**
 * Premium Header Section with Net Worth Hero Card
 * قسم الرأس الفاخر مع بطاقة صافي القيمة
 */
@Composable
fun HeaderSection(
    headerState: HeaderState,
    financialState: FinancialState,
    onNotificationsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top row with greeting and actions
        HeaderTopRow(
            headerState = headerState,
            onNotificationsClick = onNotificationsClick,
            onSettingsClick = onSettingsClick
        )
        
        // Net Worth Hero Card
        NetWorthHeroCard(
            netWorth = financialState.netWorth,
            trend = financialState.netWorthTrend
        )
    }
}

/**
 * Top Row with Greeting, Date, and Action Buttons
 * صف علوي مع التحية والتاريخ وأزرار الإجراءات
 */
@Composable
private fun HeaderTopRow(
    headerState: HeaderState,
    onNotificationsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        // Greeting and Date
        Column {
            Text(
                text = headerState.greeting,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = headerState.currentDate,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Action buttons
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            // Notifications with badge
            IconButton(onClick = onNotificationsClick) {
                BadgedBox(
                    badge = {
                        if (headerState.notificationCount > 0) {
                            Badge {
                                Text(
                                    text = headerState.notificationCount.toString(),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (headerState.notificationCount > 0) 
                            Icons.Filled.Notifications 
                        else 
                            Icons.Outlined.Notifications,
                        contentDescription = "الإشعارات",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = stringResource(R.string.nav_settings),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Net Worth Hero Card with Glassmorphism Effect
 * بطاقة صافي القيمة الرئيسية مع تأثير الزجاج
 */
@Composable
fun NetWorthHeroCard(
    netWorth: Double,
    trend: Double,
    modifier: Modifier = Modifier
) {
    GlassmorphicGradientCard(
        modifier = modifier.fillMaxWidth(),
        gradientColors = AppColors.PrimaryGradient,
        overlayAlpha = 0.15f
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Label
            Text(
                text = "💰 صافي القيمة الإجمالية",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
            
            // Value with animation
            AnimatedCounter(
                targetValue = netWorth,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                isCurrency = true
            )
            
            // Trend indicator
            if (trend != 0.0) {
                HeroTrendIndicator(
                    percentage = trend,
                    label = "عن الشهر الماضي",
                    textColor = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

/**
 * Compact Net Worth Card (Alternative Layout)
 * بطاقة صافي القيمة المضغوطة
 */
@Composable
fun CompactNetWorthCard(
    netWorth: Double,
    trend: Double,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CustomShapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(AppColors.PrimaryGradient)
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "صافي القيمة",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    AnimatedCounter(
                        targetValue = netWorth,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        isCurrency = true
                    )
                }
                
                if (trend != 0.0) {
                    HeroTrendIndicator(
                        percentage = trend,
                        label = "",
                        textColor = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}
