package com.bashmaqawa.presentation.screens.workforce

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bashmaqawa.presentation.components.*
import com.bashmaqawa.presentation.theme.AppColors
import com.bashmaqawa.utils.CurrencyFormatter

/**
 * Worker Detail Screen (Time Capsule) - Professional Version
 * شاشة تفاصيل العامل - الكبسولة الزمنية - النسخة الاحترافية
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerDetailScreen(
    workerId: Int,
    onNavigateBack: () -> Unit,
    viewModel: WorkerDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
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
            title = { Text("تأكيد الحذف") },
            text = { Text("هل أنت متأكد من حذف هذا العامل؟ لا يمكن التراجع عن هذا الإجراء.") },
            confirmButton = {
                TextButton(
                    onClick = { 
                        viewModel.deleteWorker { onNavigateBack() }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = AppColors.Error)
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDeleteDialog() }) {
                    Text("إلغاء")
                }
            }
        )
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("تفاصيل العامل") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showEditSheet() }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { /* Export PDF */ }) {
                        Icon(Icons.Filled.PictureAsPdf, contentDescription = "Export")
                    }
                    IconButton(onClick = { viewModel.showDeleteDialog() }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = AppColors.Error)
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingIndicator(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else if (uiState.worker == null) {
            EmptyState(
                icon = Icons.Filled.PersonOff,
                title = "لم يتم العثور على العامل",
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
                // Worker Info Card
                item {
                    WorkerInfoCard(
                        name = uiState.worker!!.name,
                        category = uiState.categoryName,
                        phone = uiState.worker!!.phone,
                        dailyRate = uiState.worker!!.dailyRate
                    )
                }
                
                // Contact Actions
                item {
                    ContactActionsRow(
                        phone = uiState.worker!!.phone,
                        onCall = {
                            uiState.worker!!.phone?.let { phone ->
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:$phone")
                                }
                                context.startActivity(intent)
                            }
                        },
                        onWhatsApp = {
                            uiState.worker!!.phone?.let { phone ->
                                val cleanPhone = phone.replace(Regex("[^0-9+]"), "")
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse("https://wa.me/$cleanPhone")
                                }
                                context.startActivity(intent)
                            }
                        }
                    )
                }
                
                // Financial Summary
                item {
                    Text(
                        text = "الملخص المالي (آخر 30 يوم)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FinancialStatCard(
                            title = "المستحقات",
                            value = CurrencyFormatter.format(uiState.totalEarned),
                            icon = Icons.Filled.TrendingUp,
                            color = AppColors.Success,
                            modifier = Modifier.weight(1f)
                        )
                        FinancialStatCard(
                            title = "السلف",
                            value = CurrencyFormatter.format(uiState.totalAdvances),
                            icon = Icons.Filled.Money,
                            color = AppColors.Warning,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FinancialStatCard(
                            title = "الخصومات",
                            value = CurrencyFormatter.format(uiState.totalDeductions),
                            icon = Icons.Filled.TrendingDown,
                            color = AppColors.Error,
                            modifier = Modifier.weight(1f)
                        )
                        FinancialStatCard(
                            title = "الصافي",
                            value = CurrencyFormatter.format(uiState.netBalance),
                            icon = Icons.Filled.AccountBalance,
                            color = if (uiState.netBalance >= 0) AppColors.Primary else AppColors.Error,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // Quick Actions
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SecondaryButton(
                            text = "إضافة سلفة",
                            onClick = { viewModel.showAddAdvanceSheet() },
                            icon = Icons.Filled.Add,
                            modifier = Modifier.weight(1f)
                        )
                        SecondaryButton(
                            text = "إضافة خصم",
                            onClick = { viewModel.showAddDeductionSheet() },
                            icon = Icons.Filled.Remove,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // Attendance Summary
                item {
                    Text(
                        text = "ملخص الحضور (آخر 30 يوم)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                item {
                    AttendanceSummaryCard(
                        presentDays = uiState.presentDays,
                        absentDays = uiState.absentDays,
                        halfDays = uiState.halfDays,
                        overtimeDays = uiState.overtimeDays
                    )
                }
                
                // Transaction History
                if (uiState.transactions.isNotEmpty()) {
                    item {
                        Text(
                            text = "سجل المعاملات",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    items(uiState.transactions) { transaction ->
                        TransactionHistoryItem(transaction = transaction)
                    }
                }
            }
        }
    }
    
    // Edit Worker Bottom Sheet
    if (uiState.showEditSheet) {
        EditWorkerBottomSheet(
            worker = uiState.worker!!,
            isSaving = uiState.isSaving,
            onDismiss = { viewModel.hideEditSheet() },
            onSave = { name, phone, dailyRate ->
                viewModel.updateWorker(name, phone, dailyRate) {}
            }
        )
    }
    
    // Add Advance Bottom Sheet
    if (uiState.showAddAdvanceSheet) {
        AddAmountBottomSheet(
            title = "إضافة سلفة",
            isSaving = uiState.isSaving,
            onDismiss = { viewModel.hideAddAdvanceSheet() },
            onSave = { amount, description ->
                viewModel.addAdvance(amount, description)
            }
        )
    }
    
    // Add Deduction Bottom Sheet
    if (uiState.showAddDeductionSheet) {
        AddAmountBottomSheet(
            title = "إضافة خصم",
            isSaving = uiState.isSaving,
            onDismiss = { viewModel.hideAddDeductionSheet() },
            onSave = { amount, description ->
                viewModel.addDeduction(amount, description)
            }
        )
    }
}

/**
 * Worker Info Card with Avatar
 */
@Composable
fun WorkerInfoCard(
    name: String,
    category: String?,
    phone: String?,
    dailyRate: Double?
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(AppColors.PrimaryGradient)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.firstOrNull()?.toString() ?: "؟",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Work,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = category ?: "غير محدد",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Daily Rate Badge
            if (dailyRate != null && dailyRate > 0) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = AppColors.Success.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Payments,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = AppColors.Success
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${CurrencyFormatter.format(dailyRate)} / يوم",
                            style = MaterialTheme.typography.labelLarge,
                            color = AppColors.Success,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            
            // Phone
            if (!phone.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = phone,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Contact Actions Row
 */
@Composable
fun ContactActionsRow(
    phone: String?,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onCall,
            modifier = Modifier.weight(1f),
            enabled = !phone.isNullOrBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)
        ) {
            Icon(Icons.Filled.Phone, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("اتصال")
        }
        
        Button(
            onClick = onWhatsApp,
            modifier = Modifier.weight(1f),
            enabled = !phone.isNullOrBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Success)
        ) {
            Icon(Icons.Filled.Chat, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("واتساب")
        }
    }
}

/**
 * Financial Stat Card
 */
@Composable
fun FinancialStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
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
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

/**
 * Attendance Summary Card
 */
@Composable
fun AttendanceSummaryCard(
    presentDays: Int,
    absentDays: Int,
    halfDays: Int,
    overtimeDays: Int
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            AttendanceStatItem("حاضر", presentDays, AppColors.Success)
            AttendanceStatItem("غائب", absentDays, AppColors.Error)
            AttendanceStatItem("نصف يوم", halfDays, AppColors.Warning)
            AttendanceStatItem("إضافي", overtimeDays, AppColors.Primary)
        }
    }
}

@Composable
fun AttendanceStatItem(
    label: String,
    count: Int,
    color: androidx.compose.ui.graphics.Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Transaction History Item
 */
@Composable
fun TransactionHistoryItem(transaction: WorkerTransaction) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (transaction.isDeduction) 
                    Icons.Filled.ArrowDownward 
                else 
                    Icons.Filled.ArrowUpward,
                contentDescription = null,
                tint = if (transaction.isDeduction) AppColors.Error else AppColors.Success
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.type,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                if (!transaction.description.isNullOrBlank()) {
                    Text(
                        text = transaction.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = transaction.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = CurrencyFormatter.format(transaction.amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (transaction.isDeduction) AppColors.Error else AppColors.Success
            )
        }
    }
}

/**
 * Edit Worker Bottom Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditWorkerBottomSheet(
    worker: com.bashmaqawa.data.database.entities.Worker,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String?, dailyRate: Double?) -> Unit
) {
    var name by remember { mutableStateOf(worker.name) }
    var phone by remember { mutableStateOf(worker.phone ?: "") }
    var dailyRate by remember { mutableStateOf(worker.dailyRate?.toString() ?: "") }
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
                text = "تعديل بيانات العامل",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            OutlinedTextField(
                value = name,
                onValueChange = { 
                    name = it
                    nameError = false
                },
                label = { Text("اسم العامل *") },
                leadingIcon = { Icon(Icons.Filled.Person, null) },
                isError = nameError,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSaving
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("رقم الهاتف") },
                leadingIcon = { Icon(Icons.Filled.Phone, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isSaving
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = dailyRate,
                onValueChange = { dailyRate = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("الأجر اليومي") },
                leadingIcon = { Icon(Icons.Filled.Payments, null) },
                suffix = { Text("ج.م") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
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
                        if (name.isBlank()) {
                            nameError = true
                        } else {
                            onSave(
                                name,
                                phone.takeIf { it.isNotBlank() },
                                dailyRate.toDoubleOrNull()
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                    enabled = !isSaving && name.isNotBlank()
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
 * Add Amount Bottom Sheet (for Advances and Deductions)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAmountBottomSheet(
    title: String,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (amount: Double, description: String?) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf(false) }
    
    ModalBottomSheet(
        onDismissRequest = { if (!isSaving) onDismiss() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
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
                enabled = !isSaving
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
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
                            onSave(parsedAmount, description.takeIf { it.isNotBlank() })
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
