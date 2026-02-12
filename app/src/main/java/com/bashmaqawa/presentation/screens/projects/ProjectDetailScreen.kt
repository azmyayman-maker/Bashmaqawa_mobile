package com.bashmaqawa.presentation.screens.projects

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bashmaqawa.data.database.entities.ProjectStatus
import com.bashmaqawa.presentation.components.*
import com.bashmaqawa.presentation.theme.AppColors
import com.bashmaqawa.utils.CurrencyFormatter

/**
 * Project Detail Screen - Premium Professional Version
 * شاشة تفاصيل المشروع - النسخة الاحترافية المميزة
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    projectId: Int,
    onNavigateBack: () -> Unit,
    viewModel: ProjectDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Show success/error messages
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
            viewModel.clearSuccessMessage()
        }
    }
    
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
            viewModel.clearError()
        }
    }
    
    // Delete Confirmation Dialog
    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteDialog() },
            icon = { 
                Icon(
                    Icons.Filled.Warning, 
                    contentDescription = null,
                    tint = AppColors.Error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = { Text("تأكيد الحذف", fontWeight = FontWeight.Bold) },
            text = { Text("هل أنت متأكد من حذف هذا المشروع؟ سيتم حذف جميع البيانات المرتبطة بما في ذلك المصاريف وسجلات الحضور.") },
            confirmButton = {
                Button(
                    onClick = { 
                        viewModel.deleteProject { onNavigateBack() }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Error)
                ) {
                    Icon(Icons.Filled.Delete, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("حذف")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { viewModel.hideDeleteDialog() }) {
                    Text("إلغاء")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("تفاصيل المشروع", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showEditSheet() }) {
                        Icon(Icons.Filled.Edit, contentDescription = "تعديل")
                    }
                    IconButton(onClick = { /* Export PDF */ }) {
                        Icon(Icons.Filled.PictureAsPdf, contentDescription = "تصدير")
                    }
                    IconButton(onClick = { viewModel.showDeleteDialog() }) {
                        Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = AppColors.Error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.showAddExpenseSheet() },
                containerColor = AppColors.Primary,
                contentColor = Color.White,
                icon = { Icon(Icons.Filled.Receipt, contentDescription = null) },
                text = { Text("إضافة مصروف") }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CardSkeleton()
                StatsRowSkeleton()
                StatsRowSkeleton()
                ListSkeleton(itemCount = 3)
            }
        } else if (uiState.project == null) {
            EmptyState(
                icon = Icons.Filled.Business,
                title = "لم يتم العثور على المشروع",
                message = "يرجى المحاولة مرة أخرى",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Premium Header Card
                item {
                    FadeInAnimated(delay = 0) {
                        PremiumProjectHeader(
                            name = uiState.project!!.name,
                            clientName = uiState.project!!.clientName,
                            location = uiState.project!!.location,
                            status = uiState.project!!.status,
                            startDate = uiState.project!!.startDate,
                            onStatusChange = { viewModel.updateProjectStatus(it) }
                        )
                    }
                }
                
                // Project Overview Stats
                item {
                    FadeInAnimated(delay = 50) {
                        ProjectOverviewStats(
                            areaSqm = uiState.project!!.areaSqm,
                            pricePerMeter = uiState.project!!.pricePerMeter,
                            totalBudget = uiState.totalBudget
                        )
                    }
                }
                
                // Budget Progress Card
                item {
                    FadeInAnimated(delay = 100) {
                        EnhancedBudgetProgressCard(
                            totalBudget = uiState.totalBudget,
                            totalExpenses = uiState.totalExpenses,
                            totalLabor = uiState.totalLabor,
                            remainingBudget = uiState.remainingBudget
                        )
                    }
                }
                
                // Quick Actions Row
                item {
                    FadeInAnimated(delay = 150) {
                        QuickActionsRow(
                            onAddExpense = { viewModel.showAddExpenseSheet() },
                            onViewReports = { /* Navigate to reports */ },
                            onShare = { /* Share project */ }
                        )
                    }
                }
                
                // Financial Breakdown Section
                item {
                    FadeInAnimated(delay = 200) {
                        SectionHeader(
                            title = "تفاصيل المصروفات",
                            icon = Icons.Filled.AccountBalance
                        )
                    }
                }
                
                item {
                    FadeInAnimated(delay = 220) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ExpenseStatCard(
                                title = "العمالة",
                                value = CurrencyFormatter.format(uiState.totalLabor),
                                icon = Icons.Filled.Group,
                                color = AppColors.Primary,
                                modifier = Modifier.weight(1f)
                            )
                            ExpenseStatCard(
                                title = "المواد",
                                value = CurrencyFormatter.format(uiState.totalMaterials),
                                icon = Icons.Filled.Build,
                                color = AppColors.Warning,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                
                // Attendance Summary Section
                item {
                    FadeInAnimated(delay = 250) {
                        EnhancedAttendanceSummary(
                            attendanceDays = uiState.attendanceDays
                        )
                    }
                }
                
                // Recent Expenses Section
                if (uiState.expenses.isNotEmpty()) {
                    item {
                        FadeInAnimated(delay = 300) {
                            SectionHeader(
                                title = "آخر المصروفات",
                                icon = Icons.Filled.Receipt,
                                action = {
                                    TextButton(onClick = { /* View all */ }) {
                                        Text("عرض الكل")
                                        Icon(
                                            Icons.Filled.ChevronLeft,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            )
                        }
                    }
                    
                    items(uiState.expenses.take(5)) { expense ->
                        FadeInAnimated(delay = 320) {
                            EnhancedExpenseListItem(expense = expense)
                        }
                    }
                }
                
                // Notes Section
                if (!uiState.project!!.notes.isNullOrBlank()) {
                    item {
                        FadeInAnimated(delay = 350) {
                            NotesCard(notes = uiState.project!!.notes!!)
                        }
                    }
                }
                
                // Bottom spacing for FAB
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
    
    // Edit Project Bottom Sheet
    if (uiState.showEditSheet) {
        EditProjectBottomSheet(
            project = uiState.project!!,
            isSaving = uiState.isSaving,
            onDismiss = { viewModel.hideEditSheet() },
            onSave = { name, clientName, location, areaSqm, pricePerMeter ->
                viewModel.updateProject(name, clientName, location, areaSqm, pricePerMeter) {}
            }
        )
    }
    
    // Add Expense Bottom Sheet
    if (uiState.showAddExpenseSheet) {
        AddProjectExpenseBottomSheet(
            isSaving = uiState.isSaving,
            onDismiss = { viewModel.hideAddExpenseSheet() },
            onSave = { category, amount, description ->
                viewModel.addExpense(category, amount, description)
            }
        )
    }
}

/**
 * Premium Project Header - Gradient Background
 */
@Composable
fun PremiumProjectHeader(
    name: String,
    clientName: String?,
    location: String?,
    status: ProjectStatus,
    startDate: String?,
    onStatusChange: (ProjectStatus) -> Unit
) {
    var showStatusMenu by remember { mutableStateOf(false) }
    
    val statusColor = when (status) {
        ProjectStatus.PENDING -> AppColors.Warning
        ProjectStatus.ACTIVE -> AppColors.Success
        ProjectStatus.COMPLETED -> AppColors.Primary
        ProjectStatus.PAUSED -> AppColors.Error
    }
    
    val statusText = when (status) {
        ProjectStatus.PENDING -> "قيد الانتظار"
        ProjectStatus.ACTIVE -> "جاري التنفيذ"
        ProjectStatus.COMPLETED -> "مكتمل"
        ProjectStatus.PAUSED -> "متوقف"
    }
    
    val gradient = when (status) {
        ProjectStatus.ACTIVE -> listOf(AppColors.Success, AppColors.Success.copy(alpha = 0.7f))
        ProjectStatus.COMPLETED -> listOf(AppColors.Primary, AppColors.PrimaryDark)
        ProjectStatus.PAUSED -> listOf(AppColors.Error, AppColors.Error.copy(alpha = 0.7f))
        else -> listOf(AppColors.Warning, AppColors.Warning.copy(alpha = 0.7f))
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(gradient))
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Left: Project Icon
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Business,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    // Right: Status Badge
                    Box {
                        Surface(
                            onClick = { showStatusMenu = true },
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.25f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Icon(
                                    Icons.Filled.ArrowDropDown,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        
                        DropdownMenu(
                            expanded = showStatusMenu,
                            onDismissRequest = { showStatusMenu = false }
                        ) {
                            ProjectStatus.entries.forEach { newStatus ->
                                val itemColor = when (newStatus) {
                                    ProjectStatus.PENDING -> AppColors.Warning
                                    ProjectStatus.ACTIVE -> AppColors.Success
                                    ProjectStatus.COMPLETED -> AppColors.Primary
                                    ProjectStatus.PAUSED -> AppColors.Error
                                }
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            when (newStatus) {
                                                ProjectStatus.PENDING -> "قيد الانتظار"
                                                ProjectStatus.ACTIVE -> "جاري التنفيذ"
                                                ProjectStatus.COMPLETED -> "مكتمل"
                                                ProjectStatus.PAUSED -> "متوقف"
                                            }
                                        )
                                    },
                                    onClick = {
                                        onStatusChange(newStatus)
                                        showStatusMenu = false
                                    },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(itemColor)
                                        )
                                    },
                                    enabled = newStatus != status
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Project Name
                Text(
                    text = name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Client & Location
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (!clientName.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Person,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = clientName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 150.dp)
                            )
                        }
                    }
                    
                    if (!location.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = location,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                
                // Start Date
                if (!startDate.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.CalendarMonth,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "بدء: $startDate",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Project Overview Stats - Area, Price, Total
 */
@Composable
fun ProjectOverviewStats(
    areaSqm: Double?,
    pricePerMeter: Double?,
    totalBudget: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Area Card
        OverviewStatCard(
            value = if (areaSqm != null && areaSqm > 0) "${areaSqm.toInt()}" else "-",
            label = "المساحة م²",
            icon = Icons.Filled.SquareFoot,
            color = AppColors.Primary,
            modifier = Modifier.weight(1f)
        )
        
        // Price Per Meter Card
        OverviewStatCard(
            value = if (pricePerMeter != null && pricePerMeter > 0) CurrencyFormatter.formatShort(pricePerMeter) else "-",
            label = "سعر المتر",
            icon = Icons.Filled.Payments,
            color = AppColors.Accent,
            modifier = Modifier.weight(1f)
        )
        
        // Total Value Card
        OverviewStatCard(
            value = if (totalBudget > 0) CurrencyFormatter.formatShort(totalBudget) else "-",
            label = "إجمالي القيمة",
            icon = Icons.Filled.MonetizationOn,
            color = AppColors.Success,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Overview Stat Card
 */
@Composable
fun OverviewStatCard(
    value: String,
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.8f),
                maxLines = 1
            )
        }
    }
}

/**
 * Enhanced Budget Progress Card
 */
@Composable
fun EnhancedBudgetProgressCard(
    totalBudget: Double,
    totalExpenses: Double,
    totalLabor: Double,
    remainingBudget: Double
) {
    val totalSpent = totalExpenses + totalLabor
    val progress = if (totalBudget > 0) (totalSpent / totalBudget).toFloat().coerceIn(0f, 1f) else 0f
    val progressPercent = (progress * 100).toInt()
    val isOverBudget = remainingBudget < 0
    
    val progressColor = when {
        isOverBudget -> AppColors.Error
        progress > 0.8f -> AppColors.Warning
        else -> AppColors.Success
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.AccountBalanceWallet,
                        contentDescription = null,
                        tint = AppColors.Primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "الميزانية",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                // Percentage Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = progressColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "$progressPercent%",
                        style = MaterialTheme.typography.labelMedium,
                        color = progressColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(progressColor, progressColor.copy(alpha = 0.7f))
                            )
                        )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BudgetStatItem(
                    label = "الميزانية",
                    value = CurrencyFormatter.format(totalBudget),
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    color = AppColors.Primary
                )
                BudgetStatItem(
                    label = "المنفق",
                    value = CurrencyFormatter.format(totalSpent),
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                    color = progressColor
                )
                BudgetStatItem(
                    label = "المتبقي",
                    value = CurrencyFormatter.format(remainingBudget),
                    icon = Icons.Filled.Savings,
                    color = if (isOverBudget) AppColors.Error else AppColors.Success
                )
            }
        }
    }
}

/**
 * Budget Stat Item
 */
@Composable
fun BudgetStatItem(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Quick Actions Row
 */
@Composable
fun QuickActionsRow(
    onAddExpense: () -> Unit,
    onViewReports: () -> Unit,
    onShare: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickActionButton(
            icon = Icons.Filled.Receipt,
            label = "مصروف",
            color = AppColors.Error,
            onClick = onAddExpense,
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            icon = Icons.Filled.Assessment,
            label = "تقارير",
            color = AppColors.Primary,
            onClick = onViewReports,
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            icon = Icons.Filled.Share,
            label = "مشاركة",
            color = AppColors.Success,
            onClick = onShare,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Quick Action Button
 */
@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Section Header
 */
@Composable
fun SectionHeader(
    title: String,
    icon: ImageVector,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppColors.Primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        action?.invoke()
    }
}

/**
 * Expense Stat Card
 */
@Composable
fun ExpenseStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Enhanced Attendance Summary
 */
@Composable
fun EnhancedAttendanceSummary(attendanceDays: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
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
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppColors.Primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    tint = AppColors.Primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "سجل الحضور",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$attendanceDays يوم عمل مسجل (آخر 30 يوم)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Filled.ChevronLeft,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Enhanced Expense List Item
 */
@Composable
fun EnhancedExpenseListItem(expense: ProjectExpense) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AppColors.Error.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Receipt,
                    contentDescription = null,
                    tint = AppColors.Error,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.category,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                if (!expense.description.isNullOrBlank()) {
                    Text(
                        text = expense.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = expense.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyFormatter.format(expense.amount),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Error
                )
            }
        }
    }
}

/**
 * Notes Card
 */
@Composable
fun NotesCard(notes: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.InfoLight
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Icon(
                Icons.Filled.Notes,
                contentDescription = null,
                tint = AppColors.Info,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "ملاحظات",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.Info
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * Edit Project Bottom Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProjectBottomSheet(
    project: com.bashmaqawa.data.database.entities.Project,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, clientName: String?, location: String?, areaSqm: Double?, pricePerMeter: Double?) -> Unit
) {
    var name by remember { mutableStateOf(project.name) }
    var clientName by remember { mutableStateOf(project.clientName ?: "") }
    var location by remember { mutableStateOf(project.location ?: "") }
    var areaSqm by remember { mutableStateOf(project.areaSqm?.toString() ?: "") }
    var pricePerMeter by remember { mutableStateOf(project.pricePerMeter?.toString() ?: "") }
    var nameError by remember { mutableStateOf(false) }
    
    ModalBottomSheet(
        onDismissRequest = { if (!isSaving) onDismiss() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppColors.Primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = null,
                        tint = AppColors.Primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "تعديل بيانات المشروع",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "قم بتحديث معلومات المشروع",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedTextField(
                value = name,
                onValueChange = { 
                    name = it
                    nameError = false
                },
                label = { Text("اسم المشروع *") },
                leadingIcon = { Icon(Icons.Filled.Business, null) },
                isError = nameError,
                supportingText = if (nameError) {{ Text("اسم المشروع مطلوب") }} else null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSaving,
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = clientName,
                onValueChange = { clientName = it },
                label = { Text("اسم العميل") },
                leadingIcon = { Icon(Icons.Filled.Person, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSaving,
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("الموقع") },
                leadingIcon = { Icon(Icons.Filled.LocationOn, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSaving,
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = areaSqm,
                    onValueChange = { areaSqm = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("المساحة") },
                    suffix = { Text("م²") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = !isSaving,
                    shape = RoundedCornerShape(12.dp)
                )
                
                OutlinedTextField(
                    value = pricePerMeter,
                    onValueChange = { pricePerMeter = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("سعر المتر") },
                    suffix = { Text("ج.م") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    enabled = !isSaving,
                    shape = RoundedCornerShape(12.dp)
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
                    enabled = !isSaving,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("إلغاء")
                }
                Button(
                    onClick = {
                        if (name.isBlank()) {
                            nameError = true
                        } else {
                            onSave(
                                name,
                                clientName.takeIf { it.isNotBlank() },
                                location.takeIf { it.isNotBlank() },
                                areaSqm.toDoubleOrNull(),
                                pricePerMeter.toDoubleOrNull()
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                    enabled = !isSaving && name.isNotBlank(),
                    shape = RoundedCornerShape(12.dp)
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
        }
    }
}

/**
 * Add Project Expense Bottom Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProjectExpenseBottomSheet(
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (category: String, amount: Double, description: String?) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("مواد") }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf(false) }
    
    val categories = listOf("مواد", "معدات", "نقل", "خدمات", "أخرى")
    
    ModalBottomSheet(
        onDismissRequest = { if (!isSaving) onDismiss() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppColors.Error.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Receipt,
                        contentDescription = null,
                        tint = AppColors.Error,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "إضافة مصروف للمشروع",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "سجل مصروف جديد",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Category Selection
            Text(
                text = "التصنيف",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories.size) { index ->
                    FilterChip(
                        selected = selectedCategory == categories[index],
                        onClick = { selectedCategory = categories[index] },
                        label = { Text(categories[index]) },
                        enabled = !isSaving,
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = amount,
                onValueChange = { 
                    amount = it.filter { c -> c.isDigit() || c == '.' }
                    amountError = false
                },
                label = { Text("المبلغ *") },
                leadingIcon = { Icon(Icons.Filled.Payments, null) },
                suffix = { Text("ج.م") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = amountError,
                supportingText = if (amountError) {{ Text("المبلغ مطلوب") }} else null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSaving,
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("الوصف") },
                leadingIcon = { Icon(Icons.Filled.Description, null) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2,
                enabled = !isSaving,
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("إلغاء")
                }
                Button(
                    onClick = {
                        val parsedAmount = amount.toDoubleOrNull()
                        if (parsedAmount == null || parsedAmount <= 0) {
                            amountError = true
                        } else {
                            onSave(selectedCategory, parsedAmount, description.takeIf { it.isNotBlank() })
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                    enabled = !isSaving && amount.isNotBlank(),
                    shape = RoundedCornerShape(12.dp)
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
        }
    }
}
