package com.bashmaqawa.presentation.screens.financial

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.bashmaqawa.R
import com.bashmaqawa.data.database.entities.Account
import com.bashmaqawa.presentation.components.DynamicEmptyState
import com.bashmaqawa.presentation.components.FinancialEmptyIcons
import com.bashmaqawa.presentation.components.LoadingIndicator
import com.bashmaqawa.presentation.components.getFinancialEmptyConfig
import com.bashmaqawa.presentation.theme.AppColors
import com.bashmaqawa.presentation.navigation.Screen
import com.bashmaqawa.utils.CurrencyFormatter

/**
 * Premium Financial Dashboard Screen
 * شاشة لوحة المعلومات المالية المتميزة
 * 
 * Features:
 * - Collapsing toolbar with animated dashboard header
 * - Smart card pager (Net Worth, Cash Flow)
 * - Quick actions row with haptic feedback
 * - Interactive charts
 * - Sticky-header transaction list with filters
 * - Full RTL support
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FinancialScreen(
    navController: NavController,
    viewModel: FinancialViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dashboardState = uiState.dashboardState
    val transactionListState = uiState.transactionListState
    
    var showAddAccountSheet by remember { mutableStateOf(false) }
    var showAddTransactionSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val haptic = LocalHapticFeedback.current
    
    // Pull to refresh state
    var isRefreshing by remember { mutableStateOf(false) }
    
    // Success/Error snackbar handling
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearSuccessMessage()
        }
    }
    
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }
    
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.financial_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    when (uiState.selectedTab) {
                        0 -> showAddAccountSheet = true
                        1 -> navController.navigate(Screen.AddTransaction.route)
                    }
                },
                containerColor = AppColors.Primary
            ) {
                Icon(
                    imageVector = when (uiState.selectedTab) {
                        0 -> Icons.Filled.AccountBalance
                        1 -> Icons.Filled.Receipt
                        else -> Icons.Filled.Add
                    },
                    contentDescription = when (uiState.selectedTab) {
                        0 -> "Add Account"
                        1 -> "Add Transaction"
                        else -> "Add"
                    }
                )
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                viewModel.refresh()
                isRefreshing = false
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp) // Space for FAB
            ) {
                // ===== DASHBOARD HEADER =====
                item {
                    DashboardHeader(
                        dashboardState = dashboardState,
                        onNewExpense = { navController.navigate(Screen.AddTransaction.route) },
                        onTransfer = { navController.navigate(Screen.AddTransaction.route) },
                        onNewIncome = { navController.navigate(Screen.AddTransaction.route) },
                        onReport = { /* TODO: Navigate to report */ }
                    )
                }
                
                // ===== TAB ROW =====
                stickyHeader {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Column {
                            TabRow(
                                selectedTabIndex = uiState.selectedTab,
                                containerColor = MaterialTheme.colorScheme.background
                            ) {
                                Tab(
                                    selected = uiState.selectedTab == 0,
                                    onClick = { 
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.onTabSelected(0) 
                                    },
                                    text = { Text(stringResource(R.string.accounts)) }
                                )
                                Tab(
                                    selected = uiState.selectedTab == 1,
                                    onClick = { 
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.onTabSelected(1) 
                                    },
                                    text = { Text(stringResource(R.string.transactions)) }
                                )
                            }
                            
                            // Show filters only on transactions tab
                            if (uiState.selectedTab == 1) {
                                Spacer(modifier = Modifier.height(8.dp))
                                TransactionFilterChipRow(
                                    selectedFilter = transactionListState.selectedFilter,
                                    onFilterSelected = { viewModel.updateTransactionFilter(it) }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
                
                // ===== CONTENT =====
                when (uiState.selectedTab) {
                    0 -> {
                        // Accounts Tab
                        if (uiState.hasNoAccounts) {
                            item {
                                DynamicEmptyState(
                                    config = getFinancialEmptyConfig(
                                        tabIndex = 0,
                                        icons = FinancialEmptyIcons(
                                            accounts = Icons.Filled.AccountBalance,
                                            transactions = Icons.Filled.Receipt
                                        )
                                    ),
                                    onAction = { showAddAccountSheet = true },
                                    tabIndex = 0,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(300.dp)
                                )
                            }
                        } else {
                            itemsIndexed(
                                items = uiState.accounts,
                                key = { _, account -> account.id }
                            ) { index, account ->
                                AnimatedAccountCard(
                                    account = account,
                                    animationDelay = index * 50,
                                    onClick = {
                                        navController.navigate(Screen.AccountDetail.createRoute(account.id))
                                    }
                                )
                            }
                        }
                    }
                    1 -> {
                        // Transactions Tab with Sticky Headers
                        if (uiState.hasNoTransactions) {
                            item {
                                DynamicEmptyState(
                                    config = getFinancialEmptyConfig(
                                        tabIndex = 1,
                                        icons = FinancialEmptyIcons(
                                            accounts = Icons.Filled.AccountBalance,
                                            transactions = Icons.Filled.Receipt
                                        )
                                    ),
                                    onAction = { showAddTransactionSheet = true },
                                    tabIndex = 1,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(300.dp)
                                )
                            }
                        } else {
                            // Grouped transactions with sticky headers
                            transactionListState.groupedTransactions.forEach { (dateLabel, transactions) ->
                                stickyHeader {
                                    TransactionDateHeader(dateLabel = dateLabel)
                                }
                                
                                itemsIndexed(
                                    items = transactions,
                                    key = { _, tx -> tx.id ?: System.nanoTime() }
                                ) { index, transaction ->
                                    PremiumTransactionItem(
                                        transaction = transaction,
                                        animationDelay = index * 30,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Bottom Sheets
    if (showAddAccountSheet) {
        AddAccountBottomSheet(
            isSaving = uiState.isSaving,
            onDismiss = { showAddAccountSheet = false },
            onSave = { name, type, balance, accountNumber, bankName ->
                viewModel.addAccount(
                    name = name,
                    type = type,
                    balance = balance,
                    accountNumber = accountNumber,
                    bankName = bankName,
                    onSuccess = { showAddAccountSheet = false },
                    onError = { /* Error shown via snackbar */ }
                )
            }
        )
    }
    
    if (showAddTransactionSheet) {
        AddTransactionBottomSheet(
            isSaving = uiState.isSaving,
            onDismiss = { showAddTransactionSheet = false },
            onSave = { type, amount, category, description ->
                viewModel.addTransaction(
                    type = type,
                    amount = amount,
                    category = category,
                    description = description,
                    accountId = null,
                    projectId = null,
                    onSuccess = { showAddTransactionSheet = false },
                    onError = { /* Error shown via snackbar */ }
                )
            }
        )
    }
}

// =====================================================
// DASHBOARD HEADER COMPONENTS
// =====================================================

/**
 * Dashboard Header with smart cards and quick actions
 * رأس لوحة المعلومات مع البطاقات الذكية والإجراءات السريعة
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DashboardHeader(
    dashboardState: DashboardState,
    onNewExpense: () -> Unit,
    onTransfer: () -> Unit,
    onNewIncome: () -> Unit,
    onReport: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    )
                )
            )
    ) {
        // Main Balance Card
        AnimatedVisibility(
            visible = !dashboardState.isLoading,
            enter = fadeIn() + expandVertically()
        ) {
            FinancialOverviewCard(
                totalBalance = dashboardState.totalBalance,
                trendPercentage = dashboardState.trendPercentage,
                isPositiveTrend = dashboardState.isPositiveTrend,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        
        // Smart Card Pager (Net Worth, Cash Flow)
        val pagerState = rememberPagerState(pageCount = { 2 })
        
        if (!dashboardState.isLoading) {
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 16.dp),
                pageSpacing = 12.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) { page ->
                when (page) {
                    0 -> NetWorthCard(
                        totalAssets = dashboardState.totalAssets,
                        totalLiabilities = dashboardState.totalLiabilities,
                        modifier = Modifier.fillMaxWidth()
                    )
                    1 -> CashFlowCard(
                        income = dashboardState.monthlyIncome,
                        expenses = dashboardState.monthlyExpenses,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            // Pager Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(2) { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (pagerState.currentPage == index) 8.dp else 6.dp)
                            .background(
                                color = if (pagerState.currentPage == index) 
                                    AppColors.Primary 
                                else 
                                    AppColors.Gray400,
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                    )
                }
            }
        } else {
            // Loading placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        // Quick Actions
        Spacer(modifier = Modifier.height(8.dp))
        QuickActionsRow(
            onNewExpense = onNewExpense,
            onTransfer = onTransfer,
            onNewIncome = onNewIncome,
            onReport = onReport
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        // Charts Section (Analytics Preview)
        if (!dashboardState.isLoading && 
            (dashboardState.incomeChartData.isNotEmpty() || dashboardState.expenseChartData.isNotEmpty())) {
            AnalyticsPreviewSection(dashboardState = dashboardState)
        }
    }
}

/**
 * Analytics Preview Section with trend chart
 * قسم معاينة التحليلات مع مخطط الاتجاه
 */
@Composable
private fun AnalyticsPreviewSection(dashboardState: DashboardState) {
    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.financial_analytics),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.Filled.ShowChart,
                    contentDescription = null,
                    tint = AppColors.Primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Trend Line Chart
            TrendLineChart(
                incomeData = dashboardState.incomeChartData,
                expenseData = dashboardState.expenseChartData,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(color = AppColors.Success, label = stringResource(R.string.income))
                Spacer(modifier = Modifier.width(24.dp))
                LegendItem(color = AppColors.Error, label = stringResource(R.string.expense))
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, shape = androidx.compose.foundation.shape.CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// =====================================================
// ANIMATED ACCOUNT CARD
// =====================================================

@Composable
private fun AnimatedAccountCard(
    account: Account,
    animationDelay: Int = 0,
    onClick: () -> Unit = {}
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(animationDelay.toLong())
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
            initialOffsetY = { 50 },
            animationSpec = tween(300)
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            onClick = onClick
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Account Icon based on type
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = AppColors.Primary.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when (account.type) {
                                com.bashmaqawa.data.database.entities.AccountType.CASH_BOX -> Icons.Filled.Money
                                com.bashmaqawa.data.database.entities.AccountType.BANK -> Icons.Filled.AccountBalance
                                com.bashmaqawa.data.database.entities.AccountType.WALLET -> Icons.Filled.Wallet
                                com.bashmaqawa.data.database.entities.AccountType.RECEIVABLE -> Icons.Filled.PersonAdd
                                com.bashmaqawa.data.database.entities.AccountType.PAYABLE -> Icons.Filled.Receipt
                                null -> Icons.Filled.AccountBalance
                            },
                            contentDescription = null,
                            tint = AppColors.Primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = account.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = when (account.type) {
                            com.bashmaqawa.data.database.entities.AccountType.CASH_BOX -> stringResource(R.string.cash_box)
                            com.bashmaqawa.data.database.entities.AccountType.BANK -> stringResource(R.string.bank_account)
                            com.bashmaqawa.data.database.entities.AccountType.WALLET -> stringResource(R.string.e_wallet)
                            else -> account.type?.name ?: ""
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    text = CurrencyFormatter.format(account.balance),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (account.balance >= 0) AppColors.Success else AppColors.Error
                )
            }
        }
    }
}

// =====================================================
// BOTTOM SHEETS (Preserved from original)
// =====================================================

/**
 * Add Account Bottom Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountBottomSheet(
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, type: com.bashmaqawa.data.database.entities.AccountType, balance: Double, accountNumber: String?, bankName: String?) -> Unit
) {
    var accountName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(com.bashmaqawa.data.database.entities.AccountType.CASH_BOX) }
    var initialBalance by remember { mutableStateOf("0") }
    var accountNumber by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    
    ModalBottomSheet(
        onDismissRequest = { if (!isSaving) onDismiss() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "إضافة حساب جديد",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // Account name (required)
            OutlinedTextField(
                value = accountName,
                onValueChange = { 
                    accountName = it
                    nameError = false
                },
                label = { Text("اسم الحساب *") },
                leadingIcon = { Icon(Icons.Filled.AccountBalance, null) },
                isError = nameError,
                supportingText = if (nameError) {{ Text("اسم الحساب مطلوب") }} else null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSaving
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Account type selector
            Text(
                text = "نوع الحساب",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                com.bashmaqawa.data.database.entities.AccountType.entries.take(3).forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { 
                            Text(
                                when (type) {
                                    com.bashmaqawa.data.database.entities.AccountType.CASH_BOX -> "خزنة"
                                    com.bashmaqawa.data.database.entities.AccountType.BANK -> "بنك"
                                    com.bashmaqawa.data.database.entities.AccountType.WALLET -> "محفظة"
                                    else -> type.name
                                }
                            )
                        },
                        leadingIcon = {
                            Icon(
                                when (type) {
                                    com.bashmaqawa.data.database.entities.AccountType.CASH_BOX -> Icons.Filled.Money
                                    com.bashmaqawa.data.database.entities.AccountType.BANK -> Icons.Filled.AccountBalance
                                    com.bashmaqawa.data.database.entities.AccountType.WALLET -> Icons.Filled.Wallet
                                    else -> Icons.Filled.AccountBalance
                                },
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        enabled = !isSaving,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Initial balance
            OutlinedTextField(
                value = initialBalance,
                onValueChange = { initialBalance = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("الرصيد الافتتاحي") },
                leadingIcon = { Icon(Icons.Filled.Payments, null) },
                suffix = { Text("ج.م") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSaving
            )
            
            // Show bank-specific fields
            if (selectedType == com.bashmaqawa.data.database.entities.AccountType.BANK) {
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = { Text("اسم البنك") },
                    leadingIcon = { Icon(Icons.Filled.Business, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isSaving
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = { accountNumber = it },
                    label = { Text("رقم الحساب") },
                    leadingIcon = { Icon(Icons.Filled.Numbers, null) },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isSaving
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving
                ) {
                    Text("إلغاء")
                }
                Button(
                    onClick = {
                        if (accountName.isBlank()) {
                            nameError = true
                        } else {
                            onSave(
                                accountName,
                                selectedType,
                                initialBalance.toDoubleOrNull() ?: 0.0,
                                accountNumber.takeIf { it.isNotBlank() },
                                bankName.takeIf { it.isNotBlank() }
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                    enabled = !isSaving && accountName.isNotBlank()
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Filled.Save, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("حفظ")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Add Transaction Bottom Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionBottomSheet(
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (type: com.bashmaqawa.data.database.entities.TransactionType, amount: Double, category: String, description: String?) -> Unit
) {
    var selectedType by remember { mutableStateOf(com.bashmaqawa.data.database.entities.TransactionType.EXPENSE) }
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf(false) }
    
    // Get categories based on type
    val categories = if (selectedType == com.bashmaqawa.data.database.entities.TransactionType.EXPENSE) {
        com.bashmaqawa.data.database.entities.TransactionCategories.expenseCategories
    } else {
        com.bashmaqawa.data.database.entities.TransactionCategories.incomeCategories
    }
    
    // Reset category when type changes
    LaunchedEffect(selectedType) {
        selectedCategory = categories.firstOrNull()?.first ?: ""
    }
    
    ModalBottomSheet(
        onDismissRequest = { if (!isSaving) onDismiss() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "إضافة معاملة جديدة",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // Transaction Type Toggle
            Text(
                text = "نوع المعاملة",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Expense button
                Surface(
                    onClick = { selectedType = com.bashmaqawa.data.database.entities.TransactionType.EXPENSE },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    color = if (selectedType == com.bashmaqawa.data.database.entities.TransactionType.EXPENSE)
                        AppColors.Error.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (selectedType == com.bashmaqawa.data.database.entities.TransactionType.EXPENSE) 2.dp else 1.dp,
                        color = if (selectedType == com.bashmaqawa.data.database.entities.TransactionType.EXPENSE) 
                            AppColors.Error else MaterialTheme.colorScheme.outline
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Filled.ArrowDownward,
                            contentDescription = null,
                            tint = if (selectedType == com.bashmaqawa.data.database.entities.TransactionType.EXPENSE)
                                AppColors.Error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "مصروف",
                            color = if (selectedType == com.bashmaqawa.data.database.entities.TransactionType.EXPENSE)
                                AppColors.Error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Income button
                Surface(
                    onClick = { selectedType = com.bashmaqawa.data.database.entities.TransactionType.INCOME },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    color = if (selectedType == com.bashmaqawa.data.database.entities.TransactionType.INCOME)
                        AppColors.Success.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (selectedType == com.bashmaqawa.data.database.entities.TransactionType.INCOME) 2.dp else 1.dp,
                        color = if (selectedType == com.bashmaqawa.data.database.entities.TransactionType.INCOME) 
                            AppColors.Success else MaterialTheme.colorScheme.outline
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Filled.ArrowUpward,
                            contentDescription = null,
                            tint = if (selectedType == com.bashmaqawa.data.database.entities.TransactionType.INCOME)
                                AppColors.Success else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "إيراد",
                            color = if (selectedType == com.bashmaqawa.data.database.entities.TransactionType.INCOME)
                                AppColors.Success else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Amount
            OutlinedTextField(
                value = amount,
                onValueChange = { 
                    amount = it.filter { c -> c.isDigit() || c == '.' }
                    amountError = false
                },
                label = { Text("المبلغ *") },
                leadingIcon = { Icon(Icons.Filled.Payments, null) },
                suffix = { Text("ج.م") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                ),
                isError = amountError,
                supportingText = if (amountError) {{ Text("المبلغ مطلوب") }} else null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSaving
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Category selector
            Text(
                text = "التصنيف",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories.size) { index ->
                    val (arabicName, _) = categories[index]
                    FilterChip(
                        selected = selectedCategory == arabicName,
                        onClick = { selectedCategory = arabicName },
                        label = { Text(arabicName) },
                        enabled = !isSaving
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("الوصف") },
                leadingIcon = { Icon(Icons.Filled.Description, null) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2,
                enabled = !isSaving
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving
                ) {
                    Text("إلغاء")
                }
                Button(
                    onClick = {
                        val parsedAmount = amount.toDoubleOrNull()
                        if (parsedAmount == null || parsedAmount <= 0) {
                            amountError = true
                        } else {
                            onSave(
                                selectedType,
                                parsedAmount,
                                selectedCategory,
                                description.takeIf { it.isNotBlank() }
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                    enabled = !isSaving && amount.isNotBlank()
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Filled.Save, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("حفظ")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
