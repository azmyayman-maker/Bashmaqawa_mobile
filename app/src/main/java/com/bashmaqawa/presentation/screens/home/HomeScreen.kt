package com.bashmaqawa.presentation.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.bashmaqawa.R
import com.bashmaqawa.presentation.components.FadeInAnimated
import com.bashmaqawa.presentation.components.StaggeredAnimatedItem
import com.bashmaqawa.presentation.navigation.Screen
import com.bashmaqawa.presentation.screens.home.components.*
import com.bashmaqawa.presentation.theme.AppColors

/**
 * Redesigned Home Dashboard Screen
 * الصفحة الرئيسية المعاد تصميمها - لوحة التحكم
 * 
 * Premium enterprise dashboard with:
 * - Glassmorphic Net Worth Hero Card
 * - 2x2 Financial Overview Grid
 * - 7-day Cash Flow Chart
 * - Quick Actions Hub
 * - Active Projects Carousel
 * - Today's Attendance Overview
 * - Recent Transactions
 * - AI Assistant Quick Access
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val pullRefreshState = rememberPullToRefreshState()
    
    Scaffold(
        floatingActionButton = {
            AiFloatingButton(
                onClick = { navController.navigate(Screen.AIChat.route) }
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            state = pullRefreshState
        ) {
            if (uiState.isLoading && !uiState.isRefreshing) {
                HomeLoadingState()
            } else if (uiState.error != null) {
                HomeErrorState(
                    error = uiState.error!!,
                    onRetry = { viewModel.refresh() }
                )
            } else {
                HomeContent(
                    uiState = uiState,
                    navController = navController
                )
            }
        }
    }
}

/**
 * Main Dashboard Content
 * محتوى لوحة التحكم الرئيسية
 */
@Composable
private fun HomeContent(
    uiState: HomeUiState,
    navController: NavController
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 80.dp // Space for FAB
        ),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Header Section with Net Worth Hero
        item(key = "header") {
            StaggeredAnimatedItem(index = 0) {
                HeaderSection(
                    headerState = uiState.header,
                    financialState = uiState.financial,
                    onNotificationsClick = { /* TODO: Navigate to notifications */ },
                    onSettingsClick = { navController.navigate(Screen.Settings.route) }
                )
            }
        }
        
        // 2. Financial Overview Cards (2x2 Grid)
        item(key = "financial") {
            StaggeredAnimatedItem(index = 1) {
                FinancialCards(
                    financialState = uiState.financial,
                    onAssetsClick = { navController.navigate(Screen.Financial.route) },
                    onLiabilitiesClick = { navController.navigate(Screen.Financial.route) },
                    onCashClick = { navController.navigate(Screen.Financial.route) },
                    onBankClick = { navController.navigate(Screen.Financial.route) }
                )
            }
        }
        
        // 3. Today's Income/Expense Summary Row
        item(key = "today_summary") {
            StaggeredAnimatedItem(index = 2) {
                TodayFinancialRow(
                    todayIncome = uiState.financial.todayIncome,
                    todayExpenses = uiState.financial.todayExpenses
                )
            }
        }
        
        // 4. Cash Flow Chart (7 days)
        item(key = "cashflow") {
            if (uiState.financial.cashFlowData.isNotEmpty()) {
                StaggeredAnimatedItem(index = 3) {
                    CashFlowChart(
                        data = uiState.financial.cashFlowData,
                        onClick = { navController.navigate(Screen.Financial.route) }
                    )
                }
            }
        }
        
        // 5. Quick Actions Hub
        item(key = "quick_actions") {
            StaggeredAnimatedItem(index = 4) {
                QuickActionsRow(
                    onRecordAttendance = { navController.navigate(Screen.RecordAttendance.route) },
                    onAddExpense = { navController.navigate(Screen.AddTransaction.route) },
                    onAddWorker = { navController.navigate(Screen.AddWorker.route) },
                    onNewProject = { navController.navigate(Screen.AddProject.route) },
                    onAddIncome = { navController.navigate(Screen.AddTransaction.route) },
                    onTransfer = { navController.navigate(Screen.AddTransaction.route) }
                )
            }
        }
        
        // 6. Active Projects Carousel
        item(key = "projects") {
            StaggeredAnimatedItem(index = 5) {
                ProjectsCarousel(
                    state = uiState.projects,
                    onProjectClick = { projectId ->
                        navController.navigate(Screen.ProjectDetail.createRoute(projectId))
                    },
                    onViewAllClick = { navController.navigate(Screen.Projects.route) },
                    onAddProjectClick = { navController.navigate(Screen.AddProject.route) }
                )
            }
        }
        
        // 7. Today's Attendance Overview
        item(key = "attendance") {
            StaggeredAnimatedItem(index = 6) {
                AttendanceOverview(
                    state = uiState.attendance,
                    onClick = { navController.navigate(Screen.RecordAttendance.route) }
                )
            }
        }
        
        // 8. Quick Attendance Record Button (if pending)
        if (uiState.attendance.notRecordedCount > 0) {
            item(key = "attendance_button") {
                StaggeredAnimatedItem(index = 7) {
                    AttendanceQuickRecordButton(
                        pendingCount = uiState.attendance.notRecordedCount,
                        onClick = { navController.navigate(Screen.RecordAttendance.route) }
                    )
                }
            }
        }
        
        // 9. Recent Transactions
        item(key = "transactions") {
            StaggeredAnimatedItem(index = 8) {
                RecentTransactions(
                    state = uiState.transactions,
                    onTransactionClick = { transactionId ->
                        navController.navigate(Screen.Financial.route)
                    },
                    onViewAllClick = { navController.navigate(Screen.Financial.route) }
                )
            }
        }
        
        // 10. AI Assistant Card
        item(key = "ai_assistant") {
            StaggeredAnimatedItem(index = 9) {
                AiAssistantCard(
                    onClick = { navController.navigate(Screen.AIChat.route) }
                )
            }
        }
    }
}

/**
 * Loading State with Shimmer
 * حالة التحميل مع تأثير الشيمر
 */
@Composable
private fun HomeLoadingState() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header skeleton
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ShimmerBox(modifier = Modifier.fillMaxWidth(0.5f).height(24.dp))
                ShimmerBox(modifier = Modifier.fillMaxWidth(0.7f).height(16.dp))
            }
        }
        
        // Hero card skeleton
        item {
            ShimmerBox(modifier = Modifier.fillMaxWidth().height(120.dp))
        }
        
        // Financial cards skeleton (2x2)
        item {
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.4f).height(20.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ShimmerBox(modifier = Modifier.weight(1f).height(100.dp))
                ShimmerBox(modifier = Modifier.weight(1f).height(100.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ShimmerBox(modifier = Modifier.weight(1f).height(80.dp))
                ShimmerBox(modifier = Modifier.weight(1f).height(80.dp))
            }
        }
        
        // Quick actions skeleton
        item {
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.4f).height(20.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(3) {
                    ShimmerBox(modifier = Modifier.weight(1f).height(80.dp))
                }
            }
        }
        
        // Projects skeleton
        item {
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.5f).height(20.dp))
            Spacer(modifier = Modifier.height(8.dp))
            ShimmerBox(modifier = Modifier.fillMaxWidth().height(140.dp))
        }
    }
}

/**
 * Simple Shimmer Box
 * صندوق شيمر بسيط
 */
@Composable
private fun ShimmerBox(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = MaterialTheme.shapes.medium
    ) {
        Box(modifier = Modifier.fillMaxSize())
    }
}

/**
 * Error State
 * حالة الخطأ
 */
@Composable
private fun HomeErrorState(
    error: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.ErrorOutline,
            contentDescription = null,
            tint = AppColors.Error,
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = stringResource(R.string.error_loading_data),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = error,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.Primary
            )
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.retry))
        }
    }
}
