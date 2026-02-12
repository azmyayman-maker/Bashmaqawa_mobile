package com.bashmaqawa.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bashmaqawa.R
import com.bashmaqawa.presentation.screens.home.HomeScreen
import com.bashmaqawa.presentation.components.PlaceholderScreen
import com.bashmaqawa.presentation.screens.login.LoginScreen
import com.bashmaqawa.presentation.screens.calendar.CalendarScreen
import com.bashmaqawa.presentation.screens.workforce.WorkforceScreen
import com.bashmaqawa.presentation.screens.workforce.WorkerDetailScreen
import com.bashmaqawa.presentation.screens.projects.ProjectsScreen
import com.bashmaqawa.presentation.screens.financial.FinancialScreen
import com.bashmaqawa.presentation.screens.analytics.AnalyticsScreen
import com.bashmaqawa.presentation.screens.chat.AIChatScreen
import com.bashmaqawa.presentation.screens.settings.SettingsScreen
import com.bashmaqawa.presentation.screens.projects.ProjectDetailScreen
import com.bashmaqawa.presentation.screens.reports.ReportSelectionScreen
import com.bashmaqawa.presentation.screens.bankstatement.BankStatementScreen
import com.bashmaqawa.presentation.theme.AppColors

/**
 * Bottom navigation item data
 */
data class BottomNavItem(
    val screen: Screen,
    val titleResId: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(
        screen = Screen.Home,
        titleResId = R.string.nav_home,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    BottomNavItem(
        screen = Screen.Calendar,
        titleResId = R.string.nav_calendar,
        selectedIcon = Icons.Filled.CalendarMonth,
        unselectedIcon = Icons.Outlined.CalendarMonth
    ),
    BottomNavItem(
        screen = Screen.Workforce,
        titleResId = R.string.nav_workforce,
        selectedIcon = Icons.Filled.Group,
        unselectedIcon = Icons.Outlined.Group
    ),
    BottomNavItem(
        screen = Screen.Projects,
        titleResId = R.string.nav_projects,
        selectedIcon = Icons.Filled.Business,
        unselectedIcon = Icons.Outlined.Business
    ),
    BottomNavItem(
        screen = Screen.Financial,
        titleResId = R.string.nav_financial,
        selectedIcon = Icons.Filled.AccountBalance,
        unselectedIcon = Icons.Outlined.AccountBalance
    )
)

/**
 * Main Navigation Host
 * المضيف الرئيسي للتنقل
 */
@Composable
fun BashmaqawaNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Setup Route
        composable(Screen.Setup.route) {
            LoginScreen(
                isSetupMode = true,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Setup.route) { inclusive = true }
                    }
                }
            )
        }

        // Login Route
        composable(Screen.Login.route) {
            LoginScreen(
                isSetupMode = false,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Main App Route (Home holds the MainScaffold)
        composable(Screen.Home.route) {
            MainScaffold(navController = rememberNavController())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    navController: NavHostController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // Determine if bottom bar should be shown
    val showBottomBar = currentDestination?.route in bottomNavItems.map { it.screen.route }
    
    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { 
                            it.route == item.screen.route 
                        } == true
                        
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = stringResource(item.titleResId)
                                )
                            },
                            label = {
                                Text(
                                    text = stringResource(item.titleResId),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AppColors.Primary,
                                selectedTextColor = AppColors.Primary,
                                indicatorColor = AppColors.Primary.copy(alpha = 0.1f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues),
            enterTransition = {
                fadeIn(animationSpec = tween(300)) + slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(300)) + slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(300)
                )
            }
        ) {
            // Home
            composable(Screen.Home.route) {
                HomeScreen(navController = navController)
            }
            
            // Calendar
            composable(Screen.Calendar.route) {
                CalendarScreen(navController = navController)
            }
            
            // Workforce
            composable(Screen.Workforce.route) {
                WorkforceScreen(navController = navController)
            }
            
            // Worker Detail
            composable(
                route = Screen.WorkerDetail.route,
                arguments = listOf(
                    navArgument("workerId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val workerId = backStackEntry.arguments?.getInt("workerId") ?: 0
                WorkerDetailScreen(
                    workerId = workerId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            // Projects
            composable(Screen.Projects.route) {
                ProjectsScreen(navController = navController)
            }
            
            // Project Detail
            composable(
                route = Screen.ProjectDetail.route,
                arguments = listOf(
                    navArgument("projectId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getInt("projectId") ?: 0
                ProjectDetailScreen(
                    projectId = projectId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            // Financial
            composable(Screen.Financial.route) {
                FinancialScreen(navController = navController)
            }
            
            // Account Detail
            composable(
                route = Screen.AccountDetail.route,
                arguments = listOf(
                    navArgument("accountId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val accountId = backStackEntry.arguments?.getInt("accountId") ?: 0
                com.bashmaqawa.presentation.screens.financial.AccountDetailScreen(
                    accountId = accountId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAddTransaction = { 
                        navController.navigate(Screen.AddTransaction.route) 
                    },
                    onNavigateToBankStatement = {
                        navController.navigate(Screen.BankStatement.route)
                    }
                )
            }
            
            // Analytics
            composable(Screen.Analytics.route) {
                AnalyticsScreen(navController = navController)
            }
            
            // AI Chat
            composable(Screen.AIChat.route) {
                AIChatScreen(navController = navController)
            }
            
            // Settings
            composable(Screen.Settings.route) {
                SettingsScreen(navController = navController)
            }

            // Backup
            composable(Screen.Backup.route) {
                com.bashmaqawa.presentation.screens.backup.BackupScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            // Add Transaction
            composable(
                route = Screen.AddTransaction.route,
                arguments = listOf(
                    navArgument("projectId") { 
                        type = NavType.IntType
                        defaultValue = -1 
                    },
                    navArgument("workerId") { 
                        type = NavType.IntType
                        defaultValue = -1 
                    }
                )
            ) { backStackEntry ->
                val projectId = backStackEntry.arguments?.getInt("projectId")?.takeIf { it > 0 }
                val workerId = backStackEntry.arguments?.getInt("workerId")?.takeIf { it > 0 }
            com.bashmaqawa.presentation.screens.transaction.AddTransactionScreen(
                    navController = navController,
                    preselectedProjectId = projectId,
                    preselectedWorkerId = workerId
                )
            }
            
            // Reports
            composable(Screen.Reports.route) {
                ReportSelectionScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToBankStatement = { navController.navigate(Screen.BankStatement.route) }
                )
            }
            
            // Bank Statement
            composable(Screen.BankStatement.route) {
                BankStatementScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            // Missing Routes - Placeholders
            composable(Screen.AddWorker.route) {
                com.bashmaqawa.presentation.screens.workforce.AddWorkerScreen(navController = navController)
            }
            
            composable(Screen.AddProject.route) {
                PlaceholderScreen(
                    title = "إضافة مشروع جديد",
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable(Screen.RecordAttendance.route) {
                com.bashmaqawa.presentation.screens.attendance.RecordAttendanceScreen(
                    navController = navController
                )
            }
        }
    }
}
