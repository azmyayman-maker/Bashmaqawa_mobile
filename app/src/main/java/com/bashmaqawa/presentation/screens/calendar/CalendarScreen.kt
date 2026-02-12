package com.bashmaqawa.presentation.screens.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.bashmaqawa.R
import com.bashmaqawa.data.database.entities.Attendance
import com.bashmaqawa.data.database.entities.AttendanceStatus
import com.bashmaqawa.data.database.entities.ExpenseItem
import com.bashmaqawa.data.database.entities.Project
import com.bashmaqawa.data.database.entities.Worker
import com.bashmaqawa.presentation.components.EmptyState
import com.bashmaqawa.presentation.components.LoadingIndicator
import com.bashmaqawa.presentation.theme.AppColors
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * Calendar Screen with proper state management and feedback
 * شاشة التقويم مع إدارة حالة وتغذية راجعة صحيحة
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    navController: NavController,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddAttendanceSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    
    val arabicFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)
        .withLocale(Locale("ar"))
    
    // Show success/error snackbars
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
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.calendar_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = uiState.selectedDate.format(arabicFormatter),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddAttendanceSheet = true },
                containerColor = AppColors.Primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Attendance")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // View Mode Tabs
            TabRow(
                selectedTabIndex = uiState.viewMode,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = uiState.viewMode == 0,
                    onClick = { viewModel.onViewModeChange(0) },
                    text = { Text(stringResource(R.string.view_day)) }
                )
                Tab(
                    selected = uiState.viewMode == 1,
                    onClick = { viewModel.onViewModeChange(1) },
                    text = { Text(stringResource(R.string.view_week)) }
                )
                Tab(
                    selected = uiState.viewMode == 2,
                    onClick = { viewModel.onViewModeChange(2) },
                    text = { Text(stringResource(R.string.view_month)) }
                )
            }
            
            // Date Navigation Header
            DateNavigationHeader(
                selectedDate = uiState.selectedDate,
                viewMode = uiState.viewMode,
                onPreviousClick = {
                    val newDate = when (uiState.viewMode) {
                        0 -> uiState.selectedDate.minusDays(1)
                        1 -> uiState.selectedDate.minusWeeks(1)
                        else -> uiState.selectedDate.minusMonths(1)
                    }
                    viewModel.onDateSelected(newDate)
                },
                onNextClick = {
                    val newDate = when (uiState.viewMode) {
                        0 -> uiState.selectedDate.plusDays(1)
                        1 -> uiState.selectedDate.plusWeeks(1)
                        else -> uiState.selectedDate.plusMonths(1)
                    }
                    viewModel.onDateSelected(newDate)
                },
                onTodayClick = {
                    viewModel.onDateSelected(java.time.LocalDate.now())
                }
            )
            
            // Stats Summary
            if (uiState.hasRecords) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatChip(
                        label = "حاضر",
                        count = uiState.presentCount,
                        color = AppColors.Success,
                        modifier = Modifier.weight(1f)
                    )
                    StatChip(
                        label = "غائب",
                        count = uiState.absentCount,
                        color = AppColors.Error,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Content based on view mode
            if (uiState.isLoading) {
                LoadingIndicator(modifier = Modifier.fillMaxSize())
            } else {
                when (uiState.viewMode) {
                    2 -> {
                        // Month View - Calendar Grid
                        MonthCalendarGrid(
                            selectedDate = uiState.selectedDate,
                            onDateClick = { date ->
                                viewModel.onDateSelected(date)
                                viewModel.onViewModeChange(0) // Switch to day view
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        )
                    }
                    1 -> {
                        // Week View
                        WeekCalendarRow(
                            selectedDate = uiState.selectedDate,
                            onDateClick = { date ->
                                viewModel.onDateSelected(date)
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        AttendanceListContent(
                            hasRecords = uiState.hasRecords,
                            records = uiState.attendanceRecords,
                            onAddClick = { showAddAttendanceSheet = true },
                            onStatusChange = { id, status -> viewModel.updateAttendanceStatus(id, status) },
                            onDelete = { id -> viewModel.deleteAttendance(id) }
                        )
                    }
                    else -> {
                        // Day View - Attendance List
                        AttendanceListContent(
                            hasRecords = uiState.hasRecords,
                            records = uiState.attendanceRecords,
                            onAddClick = { showAddAttendanceSheet = true },
                            onStatusChange = { id, status -> viewModel.updateAttendanceStatus(id, status) },
                            onDelete = { id -> viewModel.deleteAttendance(id) }
                        )
                    }
                }
            }
        }
    }
    
    // Add Attendance Bottom Sheet
    if (showAddAttendanceSheet) {
        EnhancedAddAttendanceBottomSheet(
            isSaving = uiState.isSaving,
            selectedDate = uiState.selectedDate,
            workers = uiState.activeWorkers,
            projects = uiState.activeProjects,
            onDismiss = { showAddAttendanceSheet = false },
            onSave = { workerId, projectId, status, hours, checkIn, checkOut, overtime, expensesJson, notes ->
                viewModel.addAttendance(
                    workerId = workerId,
                    projectId = projectId,
                    status = status,
                    hoursWorked = hours,
                    checkInTime = checkIn,
                    checkOutTime = checkOut,
                    overtimeHours = overtime,
                    expensesJson = expensesJson,
                    notes = notes,
                    onSuccess = { showAddAttendanceSheet = false },
                    onError = { /* Error shown via snackbar */ }
                )
            }
        )
    }
}

/**
 * Stat Chip for attendance summary
 */
@Composable
fun StatChip(
    label: String,
    count: Int,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = color
            )
        }
    }
}

/**
 * Attendance Record Card
 */
@Composable
fun AttendanceCard(
    attendance: Attendance,
    onStatusChange: (AttendanceStatus) -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    val statusColor = when (attendance.status) {
        AttendanceStatus.PRESENT -> AppColors.Success
        AttendanceStatus.ABSENT -> AppColors.Error
        AttendanceStatus.HALF_DAY -> AppColors.Warning
        AttendanceStatus.OVERTIME -> AppColors.Primary
    }
    
    val statusIcon = when (attendance.status) {
        AttendanceStatus.PRESENT -> Icons.Filled.CheckCircle
        AttendanceStatus.ABSENT -> Icons.Filled.Cancel
        AttendanceStatus.HALF_DAY -> Icons.Filled.WatchLater
        AttendanceStatus.OVERTIME -> Icons.Filled.AddCircle
    }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = statusColor.copy(alpha = 0.2f)
            ) {
                Icon(
                    imageVector = statusIcon,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.padding(8.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "عامل #${attendance.workerId}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                if (attendance.hoursWorked != null && attendance.hoursWorked > 0) {
                    Text(
                        text = "${attendance.hoursWorked} ساعة",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "More")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    AttendanceStatus.entries.forEach { status ->
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    when (status) {
                                        AttendanceStatus.PRESENT -> "حاضر"
                                        AttendanceStatus.ABSENT -> "غائب"
                                        AttendanceStatus.HALF_DAY -> "نصف يوم"
                                        AttendanceStatus.OVERTIME -> "إضافي"
                                    }
                                )
                            },
                            onClick = {
                                onStatusChange(status)
                                showMenu = false
                            }
                        )
                    }
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("حذف", color = AppColors.Error) },
                        onClick = {
                            onDelete()
                            showMenu = false
                        },
                        leadingIcon = { Icon(Icons.Filled.Delete, null, tint = AppColors.Error) }
                    )
                }
            }
        }
    }
}

/**
 * Add Attendance Bottom Sheet with business logic fixes
 * 
 * FIX: Hours automatically set to 0 when Absent is selected
 * FIX: Hours field disabled when Absent
 * FIX: Loading indicator in save button
 * FIX: Snackbar feedback on success
 */
/**
 * Add Attendance Bottom Sheet with Dropdown and Automated Hours
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAttendanceBottomSheet(
    isSaving: Boolean,
    workers: List<com.bashmaqawa.data.database.entities.Worker>,
    onDismiss: () -> Unit,
    onSave: (workerId: Int, projectId: Int?, status: AttendanceStatus, hoursWorked: Double?) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(AttendanceStatus.PRESENT) }
    var selectedWorkerId by remember { mutableStateOf<Int?>(null) }
    var expanded by remember { mutableStateOf(false) }
    
    // Auto-calculate hours logic
    val hoursWorked = remember(selectedStatus) {
        when (selectedStatus) {
            AttendanceStatus.ABSENT -> 0.0
            AttendanceStatus.HALF_DAY -> 4.0
            AttendanceStatus.PRESENT -> 8.0
            AttendanceStatus.OVERTIME -> 10.0 // Default overtime assumption, can be adjusted
        }
    }
    
    val selectedWorkerName = workers.find { it.id == selectedWorkerId }?.name ?: ""
    
    ModalBottomSheet(
        onDismissRequest = { if (!isSaving) onDismiss() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "إضافة سجل حضور",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // Worker Dropdown Selection
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { if (!isSaving) expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedWorkerName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("اختر العامل *") },
                    leadingIcon = { Icon(Icons.Filled.Person, null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    enabled = !isSaving
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    if (workers.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("لا يوجد عمال نشطين") },
                            onClick = { expanded = false }
                        )
                    } else {
                        workers.forEach { worker ->
                            DropdownMenuItem(
                                text = { Text(worker.name) },
                                onClick = {
                                    selectedWorkerId = worker.id
                                    expanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Attendance Status Selection
            Text(
                text = "حالة الحضور",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AttendanceStatusChip(
                    label = "حاضر (8س)",
                    icon = Icons.Filled.CheckCircle,
                    isSelected = selectedStatus == AttendanceStatus.PRESENT,
                    color = AppColors.Success,
                    onClick = { selectedStatus = AttendanceStatus.PRESENT },
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving
                )
                AttendanceStatusChip(
                    label = "غائب",
                    icon = Icons.Filled.Cancel,
                    isSelected = selectedStatus == AttendanceStatus.ABSENT,
                    color = AppColors.Error,
                    onClick = { selectedStatus = AttendanceStatus.ABSENT },
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AttendanceStatusChip(
                    label = "نصف (4س)",
                    icon = Icons.Filled.WatchLater,
                    isSelected = selectedStatus == AttendanceStatus.HALF_DAY,
                    color = AppColors.Warning,
                    onClick = { selectedStatus = AttendanceStatus.HALF_DAY },
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving
                )
                AttendanceStatusChip(
                    label = "إضافي (10س)",
                    icon = Icons.Filled.AddCircle,
                    isSelected = selectedStatus == AttendanceStatus.OVERTIME,
                    color = AppColors.Primary,
                    onClick = { selectedStatus = AttendanceStatus.OVERTIME },
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Action Buttons
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
                        selectedWorkerId?.let { id ->
                            onSave(id, null, selectedStatus, hoursWorked)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                    enabled = !isSaving && selectedWorkerId != null
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
 * Enhanced Add Attendance Bottom Sheet with Premium UI
 * صفحة إضافة الحضور المحسنة مع واجهة احترافية
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedAddAttendanceBottomSheet(
    isSaving: Boolean,
    selectedDate: java.time.LocalDate,
    workers: List<Worker>,
    projects: List<Project>,
    onDismiss: () -> Unit,
    onSave: (
        workerId: Int,
        projectId: Int?,
        status: AttendanceStatus,
        hoursWorked: Double?,
        checkInTime: String?,
        checkOutTime: String?,
        overtimeHours: Double?,
        expensesJson: String?,
        notes: String?
    ) -> Unit
) {
    // State management
    var selectedStatus by remember { mutableStateOf(AttendanceStatus.PRESENT) }
    var selectedWorkerId by remember { mutableStateOf<Int?>(null) }
    var selectedProjectId by remember { mutableStateOf<Int?>(null) }
    var workerDropdownExpanded by remember { mutableStateOf(false) }
    var projectDropdownExpanded by remember { mutableStateOf(false) }
    
    // Time states
    var checkInTime by remember { mutableStateOf<LocalTime?>(LocalTime.of(8, 0)) }
    var checkOutTime by remember { mutableStateOf<LocalTime?>(LocalTime.of(16, 0)) }
    var showCheckInPicker by remember { mutableStateOf(false) }
    var showCheckOutPicker by remember { mutableStateOf(false) }
    
    // Custom hours for overtime
    var customOvertimeHours by remember { mutableStateOf(2f) }
    
    // Expenses - list of expense items
    var expenseItems by remember { mutableStateOf(listOf<ExpenseItem>()) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var newExpenseAmount by remember { mutableStateOf("") }
    var newExpenseType by remember { mutableStateOf("مواصلات") }
    var newExpenseDescription by remember { mutableStateOf("") }
    
    // Notes
    var notes by remember { mutableStateOf("") }
    
    // Calculated values
    val hoursWorked = remember(selectedStatus, customOvertimeHours) {
        when (selectedStatus) {
            AttendanceStatus.ABSENT -> 0.0
            AttendanceStatus.HALF_DAY -> 4.0
            AttendanceStatus.PRESENT -> 8.0
            AttendanceStatus.OVERTIME -> 8.0 + customOvertimeHours.toDouble()
        }
    }
    
    val selectedWorker = workers.find { it.id == selectedWorkerId }
    val selectedProject = projects.find { it.id == selectedProjectId }
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE، d MMMM", Locale("ar"))
    
    val expenseTypes = listOf(
        "مواصلات" to Icons.Filled.DirectionsCar,
        "طعام" to Icons.Filled.Restaurant,
        "مواد" to Icons.Filled.Build,
        "أخرى" to Icons.Filled.AttachMoney
    )
    
    ModalBottomSheet(
        onDismissRequest = { if (!isSaving) onDismiss() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        // Add Expense Dialog - placed outside LazyColumn
        if (showAddExpenseDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showAddExpenseDialog = false
                    newExpenseAmount = ""
                    newExpenseType = "مواصلات"
                    newExpenseDescription = ""
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Add, null, tint = AppColors.Warning)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إضافة مصروف جديد")
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Amount
                        OutlinedTextField(
                            value = newExpenseAmount,
                            onValueChange = { newExpenseAmount = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text("المبلغ *") },
                            placeholder = { Text("0.00") },
                            leadingIcon = { Text("ر.س", fontWeight = FontWeight.Bold, color = AppColors.Warning) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        // Type selection
                        Text("نوع المصروف", style = MaterialTheme.typography.labelMedium)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            expenseTypes.forEach { (type, icon) ->
                                FilterChip(
                                    selected = newExpenseType == type,
                                    onClick = { newExpenseType = type },
                                    label = { Text(type, style = MaterialTheme.typography.labelSmall) },
                                    leadingIcon = { Icon(icon, null, modifier = Modifier.size(14.dp)) }
                                )
                            }
                        }
                        
                        // Description
                        OutlinedTextField(
                            value = newExpenseDescription,
                            onValueChange = { if (it.length <= 100) newExpenseDescription = it },
                            label = { Text("الوصف (اختياري)") },
                            placeholder = { Text("فيما صُرف؟") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amount = newExpenseAmount.toDoubleOrNull()
                            if (amount != null && amount > 0) {
                                expenseItems = expenseItems + ExpenseItem(
                                    type = newExpenseType,
                                    amount = amount,
                                    description = newExpenseDescription.ifBlank { null }
                                )
                                showAddExpenseDialog = false
                                newExpenseAmount = ""
                                newExpenseType = "مواصلات"
                                newExpenseDescription = ""
                            }
                        },
                        enabled = newExpenseAmount.toDoubleOrNull()?.let { it > 0 } == true,
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Warning)
                    ) {
                        Icon(Icons.Filled.Add, null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إضافة")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        showAddExpenseDialog = false
                        newExpenseAmount = ""
                        newExpenseType = "مواصلات"
                        newExpenseDescription = ""
                    }) {
                        Text("إلغاء")
                    }
                }
            )
        }
        
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ═══════════════════════════════════════
            // PREMIUM HEADER SECTION
            // ═══════════════════════════════════════
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Icon with gradient background
                    Surface(
                        modifier = Modifier.size(72.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = AppColors.Primary.copy(alpha = 0.1f)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.EventNote,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = AppColors.Primary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "تسجيل الحضور",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = selectedDate.format(dateFormatter),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Decorative line
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(0.3f),
                        thickness = 3.dp,
                        color = AppColors.Primary
                    )
                }
            }
            
            // ═══════════════════════════════════════
            // WORKER SELECTION
            // ═══════════════════════════════════════
            item {
                Text(
                    text = "اختيار العامل",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                ExposedDropdownMenuBox(
                    expanded = workerDropdownExpanded,
                    onExpandedChange = { if (!isSaving) workerDropdownExpanded = !workerDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedWorker?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("اختر العامل...") },
                        leadingIcon = {
                            if (selectedWorker != null) {
                                Surface(
                                    modifier = Modifier.size(36.dp),
                                    shape = androidx.compose.foundation.shape.CircleShape,
                                    color = AppColors.Primary.copy(alpha = 0.2f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = selectedWorker.name.take(1),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = AppColors.Primary
                                        )
                                    }
                                }
                            } else {
                                Icon(Icons.Filled.Person, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = workerDropdownExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.Primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        enabled = !isSaving
                    )
                    
                    ExposedDropdownMenu(
                        expanded = workerDropdownExpanded,
                        onDismissRequest = { workerDropdownExpanded = false }
                    ) {
                        if (workers.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("لا يوجد عمال نشطين", color = MaterialTheme.colorScheme.error) },
                                onClick = { workerDropdownExpanded = false },
                                leadingIcon = { Icon(Icons.Filled.Warning, null, tint = MaterialTheme.colorScheme.error) }
                            )
                        } else {
                            workers.forEach { worker ->
                                DropdownMenuItem(
                                    text = { 
                                        Column {
                                            Text(worker.name, fontWeight = FontWeight.Medium)
                                            Text(
                                                worker.role ?: "عامل",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedWorkerId = worker.id
                                        workerDropdownExpanded = false
                                    },
                                    leadingIcon = {
                                        Surface(
                                            modifier = Modifier.size(32.dp),
                                            shape = androidx.compose.foundation.shape.CircleShape,
                                            color = AppColors.Primary.copy(alpha = 0.1f)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = worker.name.take(1),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AppColors.Primary
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // ═══════════════════════════════════════
            // PROJECT SELECTION (OPTIONAL)
            // ═══════════════════════════════════════
            item {
                Text(
                    text = "المشروع (اختياري)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                ExposedDropdownMenuBox(
                    expanded = projectDropdownExpanded,
                    onExpandedChange = { if (!isSaving) projectDropdownExpanded = !projectDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedProject?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("بدون مشروع محدد") },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Business,
                                null,
                                tint = if (selectedProject != null) AppColors.ProjectActive else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = projectDropdownExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.Primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        enabled = !isSaving
                    )
                    
                    ExposedDropdownMenu(
                        expanded = projectDropdownExpanded,
                        onDismissRequest = { projectDropdownExpanded = false }
                    ) {
                        // Option for no project
                        DropdownMenuItem(
                            text = { Text("بدون مشروع", fontWeight = FontWeight.Medium) },
                            onClick = {
                                selectedProjectId = null
                                projectDropdownExpanded = false
                            },
                            leadingIcon = { Icon(Icons.Filled.RemoveCircleOutline, null) }
                        )
                        
                        if (projects.isNotEmpty()) {
                            HorizontalDivider()
                            projects.forEach { project ->
                                DropdownMenuItem(
                                    text = { 
                                        Column {
                                            Text(project.name, fontWeight = FontWeight.Medium)
                                            project.clientName?.let {
                                                Text(
                                                    it,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        selectedProjectId = project.id
                                        projectDropdownExpanded = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Filled.Folder,
                                            null,
                                            tint = AppColors.ProjectActive
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // ═══════════════════════════════════════
            // ATTENDANCE STATUS SECTION
            // ═══════════════════════════════════════
            item {
                Text(
                    text = "حالة الحضور",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                // Status cards in grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PremiumStatusCard(
                        title = "حاضر",
                        subtitle = "8 ساعات",
                        icon = Icons.Filled.CheckCircle,
                        color = AppColors.AttendancePresent,
                        isSelected = selectedStatus == AttendanceStatus.PRESENT,
                        onClick = { selectedStatus = AttendanceStatus.PRESENT },
                        modifier = Modifier.weight(1f),
                        enabled = !isSaving
                    )
                    PremiumStatusCard(
                        title = "غائب",
                        subtitle = "0 ساعات",
                        icon = Icons.Filled.Cancel,
                        color = AppColors.AttendanceAbsent,
                        isSelected = selectedStatus == AttendanceStatus.ABSENT,
                        onClick = { selectedStatus = AttendanceStatus.ABSENT },
                        modifier = Modifier.weight(1f),
                        enabled = !isSaving
                    )
                }
                
                Spacer(modifier = Modifier.height(10.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PremiumStatusCard(
                        title = "نصف يوم",
                        subtitle = "4 ساعات",
                        icon = Icons.Filled.WatchLater,
                        color = AppColors.AttendanceHalfDay,
                        isSelected = selectedStatus == AttendanceStatus.HALF_DAY,
                        onClick = { selectedStatus = AttendanceStatus.HALF_DAY },
                        modifier = Modifier.weight(1f),
                        enabled = !isSaving
                    )
                    PremiumStatusCard(
                        title = "إضافي",
                        subtitle = "+${customOvertimeHours.toInt()} ساعات",
                        icon = Icons.Filled.AddCircle,
                        color = AppColors.AttendanceOvertime,
                        isSelected = selectedStatus == AttendanceStatus.OVERTIME,
                        onClick = { selectedStatus = AttendanceStatus.OVERTIME },
                        modifier = Modifier.weight(1f),
                        enabled = !isSaving
                    )
                }
            }
            
            // ═══════════════════════════════════════
            // OVERTIME HOURS SLIDER (conditional)
            // ═══════════════════════════════════════
            if (selectedStatus == AttendanceStatus.OVERTIME) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AppColors.AttendanceOvertime.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ساعات العمل الإضافي",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium
                                )
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = AppColors.AttendanceOvertime.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "${customOvertimeHours.toInt()} ساعات",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.AttendanceOvertime
                                    )
                                }
                            }
                            
                            Slider(
                                value = customOvertimeHours,
                                onValueChange = { customOvertimeHours = it },
                                valueRange = 1f..8f,
                                steps = 6,
                                colors = SliderDefaults.colors(
                                    thumbColor = AppColors.AttendanceOvertime,
                                    activeTrackColor = AppColors.AttendanceOvertime
                                ),
                                enabled = !isSaving
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("1 ساعة", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("8 ساعات", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
            
            // ═══════════════════════════════════════
            // TIME PICKERS SECTION
            // ═══════════════════════════════════════
            if (selectedStatus != AttendanceStatus.ABSENT) {
                item {
                    Text(
                        text = "أوقات العمل",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Check-in time
                        OutlinedCard(
                            onClick = { showCheckInPicker = true },
                            modifier = Modifier.weight(1f),
                            enabled = !isSaving
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Filled.Login,
                                    null,
                                    tint = AppColors.Success,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "الحضور",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = checkInTime?.format(timeFormatter) ?: "--:--",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        // Check-out time
                        OutlinedCard(
                            onClick = { showCheckOutPicker = true },
                            modifier = Modifier.weight(1f),
                            enabled = !isSaving
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Filled.Logout,
                                    null,
                                    tint = AppColors.Error,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "الانصراف",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = checkOutTime?.format(timeFormatter) ?: "--:--",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            
            // ═══════════════════════════════════════
            // EXPENSES SECTION (Dynamic List with + Button)
            // ═══════════════════════════════════════
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AppColors.Warning.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Header with + button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.Receipt,
                                    null,
                                    tint = AppColors.Warning
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "المصروفات",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (expenseItems.isNotEmpty()) {
                                        Text(
                                            text = "${expenseItems.size} مصروفات - ${expenseItems.sumOf { it.amount }} ر.س",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = AppColors.Warning
                                        )
                                    }
                                }
                            }
                            
                            // Add expense button
                            FilledTonalIconButton(
                                onClick = { showAddExpenseDialog = true },
                                enabled = !isSaving,
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = AppColors.Warning.copy(alpha = 0.2f)
                                )
                            ) {
                                Icon(Icons.Filled.Add, "إضافة مصروف", tint = AppColors.Warning)
                            }
                        }
                        
                        // Expense items list
                        if (expenseItems.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = AppColors.Warning.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            expenseItems.forEachIndexed { index, item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Surface(
                                            shape = MaterialTheme.shapes.small,
                                            color = AppColors.Warning.copy(alpha = 0.15f),
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    when (item.type) {
                                                        "مواصلات" -> Icons.Filled.DirectionsCar
                                                        "طعام" -> Icons.Filled.Restaurant
                                                        "مواد" -> Icons.Filled.Build
                                                        else -> Icons.Filled.AttachMoney
                                                    },
                                                    null,
                                                    tint = AppColors.Warning,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = item.type,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                            item.description?.let { desc ->
                                                Text(
                                                    text = desc,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                    
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${item.amount} ر.س",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = AppColors.Warning
                                        )
                                        IconButton(
                                            onClick = {
                                                expenseItems = expenseItems.filterIndexed { i, _ -> i != index }
                                            },
                                            enabled = !isSaving
                                        ) {
                                            Icon(
                                                Icons.Filled.Close,
                                                "حذف",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                                
                                if (index < expenseItems.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(start = 48.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // ═══════════════════════════════════════
            // NOTES SECTION
            // ═══════════════════════════════════════
            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { if (it.length <= 500) notes = it },
                    label = { Text("ملاحظات إضافية") },
                    placeholder = { Text("أضف أي ملاحظات هنا...") },
                    leadingIcon = { Icon(Icons.Filled.Notes, null) },
                    supportingText = { Text("${notes.length}/500") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    shape = MaterialTheme.shapes.medium,
                    enabled = !isSaving
                )
            }
            
            // ═══════════════════════════════════════
            // SUMMARY PREVIEW CARD
            // ═══════════════════════════════════════
            if (selectedWorkerId != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.Summarize,
                                    null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ملخص التسجيل",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Worker
                            SummaryRow(
                                icon = Icons.Filled.Person,
                                label = "العامل",
                                value = selectedWorker?.name ?: ""
                            )
                            
                            // Project
                            if (selectedProject != null) {
                                SummaryRow(
                                    icon = Icons.Filled.Business,
                                    label = "المشروع",
                                    value = selectedProject.name
                                )
                            }
                            
                            // Status
                            val statusText = when (selectedStatus) {
                                AttendanceStatus.PRESENT -> "حاضر"
                                AttendanceStatus.ABSENT -> "غائب"
                                AttendanceStatus.HALF_DAY -> "نصف يوم"
                                AttendanceStatus.OVERTIME -> "إضافي"
                            }
                            SummaryRow(
                                icon = Icons.Filled.EventAvailable,
                                label = "الحالة",
                                value = statusText
                            )
                            
                            // Hours
                            SummaryRow(
                                icon = Icons.Filled.Schedule,
                                label = "ساعات العمل",
                                value = "${hoursWorked.toInt()} ساعة"
                            )
                            
                            // Expenses
                            if (expenseItems.isNotEmpty()) {
                                SummaryRow(
                                    icon = Icons.Filled.Receipt,
                                    label = "المصروفات",
                                    value = "${expenseItems.size} - ${expenseItems.sumOf { it.amount }} ر.س",
                                    valueColor = AppColors.Warning
                                )
                            }
                        }
                    }
                }
            }
            
            // ═══════════════════════════════════════
            // ACTION BUTTONS
            // ═══════════════════════════════════════
            item {
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isSaving,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Icon(Icons.Filled.Close, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إلغاء")
                    }
                    
                    Button(
                        onClick = {
                            selectedWorkerId?.let { workerId ->
                                onSave(
                                    workerId,
                                    selectedProjectId,
                                    selectedStatus,
                                    hoursWorked,
                                    checkInTime?.format(timeFormatter),
                                    checkOutTime?.format(timeFormatter),
                                    if (selectedStatus == AttendanceStatus.OVERTIME) customOvertimeHours.toDouble() else null,
                                    if (expenseItems.isNotEmpty()) Json.encodeToString(expenseItems) else null,
                                    notes.ifBlank { null }
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isSaving && selectedWorkerId != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.Primary,
                            disabledContainerColor = AppColors.Primary.copy(alpha = 0.5f)
                        )
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
                            Text("حفظ التسجيل")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
    
    // Time Picker Dialogs
    if (showCheckInPicker) {
        TimePickerDialog(
            title = "وقت الحضور",
            initialTime = checkInTime ?: LocalTime.of(8, 0),
            onConfirm = { time ->
                checkInTime = time
                showCheckInPicker = false
            },
            onDismiss = { showCheckInPicker = false }
        )
    }
    
    if (showCheckOutPicker) {
        TimePickerDialog(
            title = "وقت الانصراف",
            initialTime = checkOutTime ?: LocalTime.of(16, 0),
            onConfirm = { time ->
                checkOutTime = time
                showCheckOutPicker = false
            },
            onDismiss = { showCheckOutPicker = false }
        )
    }
}

/**
 * Premium Status Card Component
 */
@Composable
fun PremiumStatusCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        label = "scale"
    )
    
    Surface(
        onClick = onClick,
        modifier = modifier.graphicsLayer(scaleX = scale, scaleY = scale),
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),
        tonalElevation = if (isSelected) 4.dp else 0.dp,
        enabled = enabled
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                color = if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) color.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Summary Row Component
 */
@Composable
fun SummaryRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

/**
 * Time Picker Dialog Component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    title: String,
    initialTime: LocalTime,
    onConfirm: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = true
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            TimePicker(state = timePickerState)
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(LocalTime.of(timePickerState.hour, timePickerState.minute))
                }
            ) {
                Text("تأكيد")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

/**
 * Attendance Status Selection Chip
 */
@Composable
fun AttendanceStatusChip(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) color else MaterialTheme.colorScheme.outline
        ),
        enabled = enabled
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Date Navigation Header with previous/next buttons
 */
@Composable
fun DateNavigationHeader(
    selectedDate: java.time.LocalDate,
    viewMode: Int,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onTodayClick: () -> Unit
) {
    val arabicFormatter = DateTimeFormatter.ofPattern("EEEE، d MMMM yyyy", Locale("ar"))
    val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("ar"))
    
    val displayText = when (viewMode) {
        2 -> selectedDate.format(monthFormatter)
        1 -> {
            val weekStart = selectedDate.minusDays(selectedDate.dayOfWeek.value.toLong() - 1)
            val weekEnd = weekStart.plusDays(6)
            "${weekStart.dayOfMonth} - ${weekEnd.dayOfMonth} ${selectedDate.format(monthFormatter)}"
        }
        else -> selectedDate.format(arabicFormatter)
    }
    
    val isToday = selectedDate == java.time.LocalDate.now()
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNextClick) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "Next")
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = displayText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (!isToday) {
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = onTodayClick,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("اليوم", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            
            IconButton(onClick = onPreviousClick) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous")
            }
        }
    }
}

/**
 * Month Calendar Grid
 */
@Composable
fun MonthCalendarGrid(
    selectedDate: java.time.LocalDate,
    onDateClick: (java.time.LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val firstDayOfMonth = selectedDate.withDayOfMonth(1)
    val lastDayOfMonth = selectedDate.withDayOfMonth(selectedDate.lengthOfMonth())
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value // 1 = Monday, 7 = Sunday
    
    val daysInMonth = selectedDate.lengthOfMonth()
    val today = java.time.LocalDate.now()
    
    // Arabic day names (Saturday first for Arabic locale)
    val dayNames = listOf("سب", "أح", "اث", "ثل", "أر", "خم", "جم")
    
    Column(modifier = modifier) {
        // Day headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            dayNames.forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Calendar grid
        var dayCounter = 1
        val adjustedFirstDay = if (firstDayOfWeek == 7) 0 else firstDayOfWeek // Adjust for Saturday start
        
        for (week in 0..5) {
            if (dayCounter > daysInMonth) break
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (dayOfWeek in 0..6) {
                    val cellIndex = week * 7 + dayOfWeek
                    
                    if (cellIndex < adjustedFirstDay || dayCounter > daysInMonth) {
                        // Empty cell
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val date = selectedDate.withDayOfMonth(dayCounter)
                        val isSelected = date == selectedDate
                        val isToday = date == today
                        
                        CalendarDayCell(
                            day = dayCounter,
                            isSelected = isSelected,
                            isToday = isToday,
                            onClick = { onDateClick(date) },
                            modifier = Modifier.weight(1f)
                        )
                        dayCounter++
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

/**
 * Calendar Day Cell
 */
@Composable
fun CalendarDayCell(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isSelected -> AppColors.Primary
        isToday -> AppColors.Primary.copy(alpha = 0.2f)
        else -> androidx.compose.ui.graphics.Color.Transparent
    }
    
    val textColor = when {
        isSelected -> androidx.compose.ui.graphics.Color.White
        isToday -> AppColors.Primary
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    Surface(
        onClick = onClick,
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp),
        shape = androidx.compose.foundation.shape.CircleShape,
        color = backgroundColor
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            )
        }
    }
}

/**
 * Week Calendar Row
 */
@Composable
fun WeekCalendarRow(
    selectedDate: java.time.LocalDate,
    onDateClick: (java.time.LocalDate) -> Unit
) {
    val weekStart = selectedDate.minusDays(selectedDate.dayOfWeek.value.toLong() - 1)
    val today = java.time.LocalDate.now()
    val dayFormatter = DateTimeFormatter.ofPattern("EEE", Locale("ar"))
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        (0..6).forEach { dayOffset ->
            val date = weekStart.plusDays(dayOffset.toLong())
            val isSelected = date == selectedDate
            val isToday = date == today
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
            ) {
                Text(
                    text = date.format(dayFormatter),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                CalendarDayCell(
                    day = date.dayOfMonth,
                    isSelected = isSelected,
                    isToday = isToday,
                    onClick = { onDateClick(date) },
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

/**
 * Attendance List Content
 */
@Composable
fun ColumnScope.AttendanceListContent(
    hasRecords: Boolean,
    records: List<Attendance>,
    onAddClick: () -> Unit,
    onStatusChange: (Int, AttendanceStatus) -> Unit,
    onDelete: (Int) -> Unit
) {
    if (!hasRecords) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            EmptyState(
                icon = Icons.Filled.CalendarMonth,
                title = "لا يوجد سجلات حضور",
                message = "اضغط + لإضافة سجل حضور جديد",
                actionText = "إضافة حضور",
                onAction = onAddClick
            )
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(records) { attendance ->
                AttendanceCard(
                    attendance = attendance,
                    onStatusChange = { status ->
                        onStatusChange(attendance.id, status)
                    },
                    onDelete = {
                        onDelete(attendance.id)
                    }
                )
            }
        }
    }
}

