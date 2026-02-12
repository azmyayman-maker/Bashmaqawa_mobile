package com.bashmaqawa.presentation.screens.financial

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bashmaqawa.R
import com.bashmaqawa.data.database.entities.TransactionType
import com.bashmaqawa.presentation.components.LoadingIndicator
import com.bashmaqawa.presentation.theme.AppColors
import kotlinx.coroutines.launch

/**
 * Account Detail Screen
 * شاشة تفاصيل الحساب
 * 
 * Enterprise-grade account detail view with:
 * - Glassmorphic balance card
 * - Tab navigation (Transactions, Journal, Analytics, Related)
 * - Expandable transaction/journal items
 * - Quick actions FAB menu
 * - Full RTL support
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AccountDetailScreen(
    accountId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToBankStatement: () -> Unit = {},
    viewModel: AccountDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Tab state
    val tabs = listOf(
        AccountDetailTab.TRANSACTIONS to "المعاملات",
        AccountDetailTab.JOURNAL to "القيود",
        AccountDetailTab.ANALYTICS to "التحليلات",
        AccountDetailTab.RELATED to "المرتبطة"
    )
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    
    // Sync tab selection with pager
    LaunchedEffect(pagerState.currentPage) {
        viewModel.selectTab(tabs[pagerState.currentPage].first)
    }
    
    // Handle success/error messages
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccessMessage()
        }
    }
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }
    
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = uiState.accountState.account?.name ?: "تفاصيل الحساب",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        uiState.accountState.account?.accountCode?.let { code ->
                            Text(
                                text = "رمز: $code",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع"
                        )
                    }
                },
                actions = {
                    // Dropdown Menu
                    var showMenu by remember { mutableStateOf(false) }
                    
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "خيارات"
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            // Edit Account
                            DropdownMenuItem(
                                text = { 
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Edit,
                                            contentDescription = null,
                                            tint = AppColors.Primary
                                        )
                                        Text("تعديل الحساب")
                                    }
                                },
                                onClick = {
                                    showMenu = false
                                    viewModel.toggleEditSheet(true)
                                }
                            )
                            
                            Divider()
                            
                            // Generate Statement
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Description,
                                            contentDescription = null,
                                            tint = AppColors.Info
                                        )
                                        Text("تصدير كشف الحساب")
                                    }
                                },
                                onClick = {
                                    showMenu = false
                                    onNavigateToBankStatement()
                                }
                            )
                            
                            // Delete Account (only if not system account)
                            if (uiState.accountState.account?.isSystemAccount == false) {
                                Divider()
                                
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Delete,
                                                contentDescription = null,
                                                tint = AppColors.Error
                                            )
                                            Text(
                                                text = "حذف الحساب",
                                                color = AppColors.Error
                                            )
                                        }
                                    },
                                    onClick = {
                                        showMenu = false
                                        viewModel.toggleDeleteConfirmation(true)
                                    }
                                )
                            }
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            AccountFabMenu(
                expanded = uiState.showFabMenu,
                onToggle = { viewModel.toggleFabMenu(!uiState.showFabMenu) },
                onAddIncome = onNavigateToAddTransaction,
                onAddExpense = onNavigateToAddTransaction,
                onTransfer = onNavigateToAddTransaction,
                onGenerateStatement = onNavigateToBankStatement
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Balance Card
                uiState.accountState.account?.let { account ->
                    AccountBalanceCard(
                        account = account,
                        trend = uiState.accountState.balanceTrend,
                        trendPercentage = uiState.accountState.trendPercentage,
                        pendingBalance = uiState.accountState.pendingBalance,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                
                // Tab Row
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = AppColors.Primary,
                    divider = { }
                ) {
                    tabs.forEachIndexed { index, (tab, title) ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (pagerState.currentPage == index) 
                                        FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
                
                // Content Pager
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (tabs[page].first) {
                        AccountDetailTab.TRANSACTIONS -> TransactionsTabContent(
                            state = uiState.transactionState,
                            onFilterChange = viewModel::updateFilter,
                            onSearchChange = viewModel::updateSearchQuery,
                            onTransactionClick = viewModel::toggleTransactionExpansion,
                            onTransactionAction = { tx, action ->
                                when (action) {
                                    TransactionAction.VIEW_DETAILS -> {
                                        // Already expanded
                                    }
                                    TransactionAction.VIEW_JOURNAL -> {
                                        // Navigate to journal entry
                                    }
                                    TransactionAction.REVERSE -> {
                                        // TODO: Implement reversal
                                        scope.launch {
                                            snackbarHostState.showSnackbar("سيتم تنفيذ الإلغاء قريباً")
                                        }
                                    }
                                    TransactionAction.EDIT -> {
                                        // TODO: Navigate to edit
                                    }
                                }
                            },
                            onAddTransaction = onNavigateToAddTransaction,
                            onRefresh = viewModel::refresh
                        )
                        
                        AccountDetailTab.JOURNAL -> JournalTabContent(
                            state = uiState.journalState,
                            onEntryClick = viewModel::toggleJournalExpansion,
                            onRefresh = viewModel::refresh
                        )
                        
                        AccountDetailTab.ANALYTICS -> AnalyticsTabContent(
                            state = uiState.analyticsState,
                            onTimeRangeChange = viewModel::updateTimeRange
                        )
                        
                        AccountDetailTab.RELATED -> RelatedTabContent(
                            state = uiState.relatedState
                        )
                    }
                }
            }
        }
    }
    
    // Edit Bottom Sheet
    if (uiState.showEditSheet) {
        uiState.accountState.account?.let { account ->
            EditAccountBottomSheet(
                account = account,
                onDismiss = { viewModel.toggleEditSheet(false) },
                onSave = { name, details, bankName, accountNumber ->
                    viewModel.updateAccount(
                        name = name,
                        details = details,
                        bankName = bankName,
                        accountNumber = accountNumber,
                        onSuccess = { viewModel.toggleEditSheet(false) },
                        onError = { error ->
                            scope.launch {
                                snackbarHostState.showSnackbar(error)
                            }
                        }
                    )
                }
            )
        }
    }
    
    // Delete Confirmation Dialog
    if (uiState.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleDeleteConfirmation(false) },
            title = { Text("حذف الحساب") },
            text = { Text("هل أنت متأكد من حذف هذا الحساب؟ لا يمكن التراجع عن هذا الإجراء.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAccount(
                            onSuccess = onNavigateBack,
                            onError = { error ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(error)
                                }
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Error
                    )
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.toggleDeleteConfirmation(false) }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

// =====================================================
// TAB CONTENTS
// =====================================================

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun TransactionsTabContent(
    state: AccountTransactionState,
    onFilterChange: (AccountTransactionFilter) -> Unit,
    onSearchChange: (String) -> Unit,
    onTransactionClick: (Int) -> Unit,
    onTransactionAction: (com.bashmaqawa.data.database.dao.TransactionWithDetails, TransactionAction) -> Unit,
    onAddTransaction: () -> Unit,
    onRefresh: () -> Unit
) {
    var isRefreshing by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            onRefresh()
            isRefreshing = false
        }
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Search Bar
            item {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("بحث في المعاملات...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "مسح"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )
            }
            
            // Filter Chips
            item {
                TransactionFilterChips(
                    selectedFilter = state.filter,
                    onFilterSelected = onFilterChange,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            // Empty State or Transactions
            if (state.groupedTransactions.isEmpty()) {
                item {
                    TransactionsEmptyState(
                        onAddTransaction = onAddTransaction 
                    )
                }
            } else {
                transactionListWithStickyHeaders(
                    groupedTransactions = state.groupedTransactions,
                    expandedTransactionId = state.expandedTransactionId,
                    onTransactionClick = onTransactionClick,
                    onTransactionAction = onTransactionAction
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JournalTabContent(
    state: AccountJournalState,
    onEntryClick: (Int) -> Unit,
    onRefresh: () -> Unit
) {
    var isRefreshing by remember { mutableStateOf(false) }
    
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            onRefresh()
            isRefreshing = false
        }
    ) {
        if (state.journalEntries.isEmpty()) {
            JournalEmptyState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = state.journalEntries,
                    key = { it.entry.id }
                ) { entryWithDetails ->
                    JournalEntryCard(
                        entry = entryWithDetails,
                        isExpanded = entryWithDetails.entry.id == state.expandedEntryId,
                        onToggle = { onEntryClick(entryWithDetails.entry.id) }
                    )
                }
                
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun AnalyticsTabContent(
    state: AccountAnalyticsState,
    onTimeRangeChange: (AccountTimeRange) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Time Range Selector
        item {
            TimeRangeSelector(
                selectedRange = state.selectedTimeRange,
                onRangeSelected = onTimeRangeChange
            )
        }
        
        // Metrics Grid
        item {
            AccountMetricsGrid(analytics = state)
        }
        
        // Chart Placeholder
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.isLoadingChart) {
                        CircularProgressIndicator()
                    } else if (state.chartData.isEmpty()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ShowChart,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "لا توجد بيانات كافية للمخطط",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        // Simple text representation of chart data
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ShowChart,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = AppColors.Primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "اتجاه الرصيد",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${state.chartData.size} نقطة بيانات",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun RelatedTabContent(
    state: AccountRelatedState
) {
    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (state.linkedWorkers.isEmpty() && state.linkedProjects.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Link,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "لا توجد كيانات مرتبطة",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "ستظهر العمال والمشاريع المرتبطة هنا",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Linked Workers Section
            if (state.linkedWorkers.isNotEmpty()) {
                item {
                    Text(
                        text = "العمال المرتبطون",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Primary
                    )
                }
                
                items(state.linkedWorkers) { worker ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = null,
                                    tint = AppColors.Primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = worker.workerName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    worker.lastTransactionDate?.let {
                                        Text(
                                            text = "آخر معاملة: ${it.substring(0, 10)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            
                            if (worker.outstandingAdvance > 0) {
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = AppColors.Warning.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = formatCurrency(worker.outstandingAdvance),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = AppColors.Warning
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Linked Projects Section
            if (state.linkedProjects.isNotEmpty()) {
                item {
                    if (state.linkedWorkers.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Text(
                        text = "المشاريع المرتبطة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Primary
                    )
                }
                
                items(state.linkedProjects) { project ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Business,
                                    contentDescription = null,
                                    tint = AppColors.Success
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = project.projectName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${project.transactionCount} معاملة",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            
                            Text(
                                text = formatCurrency(project.totalTransactions),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.Primary
                            )
                        }
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}
