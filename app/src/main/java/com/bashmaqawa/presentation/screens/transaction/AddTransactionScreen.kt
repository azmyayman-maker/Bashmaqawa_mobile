package com.bashmaqawa.presentation.screens.transaction

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.bashmaqawa.data.database.entities.Account
import com.bashmaqawa.data.database.entities.Project
import com.bashmaqawa.data.database.entities.Worker
import com.bashmaqawa.presentation.screens.transaction.components.AccountSelectorCard
import com.bashmaqawa.presentation.screens.transaction.components.AmountInputField
import com.bashmaqawa.presentation.screens.transaction.components.BalancePreviewCard
import com.bashmaqawa.presentation.screens.transaction.components.CategoryChipFlow
import com.bashmaqawa.presentation.screens.transaction.components.DateSelectorCard
import com.bashmaqawa.presentation.screens.transaction.components.PaymentMethodCard
import com.bashmaqawa.presentation.screens.transaction.components.ProjectSelectorCard
import com.bashmaqawa.presentation.screens.transaction.components.ReceiptAttachmentCard
import com.bashmaqawa.presentation.screens.transaction.components.ReferenceNumberCard
import com.bashmaqawa.presentation.screens.transaction.components.TransactionTypeSelector
import com.bashmaqawa.presentation.screens.transaction.components.WorkerSelectorCard
import com.bashmaqawa.presentation.theme.AppColors
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Add Transaction Screen
 * شاشة إضافة المعاملة
 * 
 * Full-screen modal for creating new transactions with:
 * - Transaction type selection (Expense/Income/Transfer)
 * - Amount input with live formatting
 * - Account selection with balance preview
 * - Category selection
 * - Optional project/worker linking
 * - Date picker
 * - Receipt attachment
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    navController: NavController,
    preselectedProjectId: Int? = null,
    preselectedWorkerId: Int? = null,
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    // Camera/Gallery launchers
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        // Handle camera result - in real app, save bitmap and get URI
        // For now, just show a toast
        Toast.makeText(context, "Camera capture not fully implemented", Toast.LENGTH_SHORT).show()
    }
    
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.onEvent(TransactionEvent.ReceiptAttached(it.toString()))
        }
    }
    
    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is TransactionEffect.NavigateBack -> {
                    navController.popBackStack()
                }
                is TransactionEffect.ShowSuccess -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is TransactionEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
                is TransactionEffect.LaunchCamera -> {
                    cameraLauncher.launch(null)
                }
                is TransactionEffect.LaunchGallery -> {
                    galleryLauncher.launch("image/*")
                }
                is TransactionEffect.ScrollToField -> {
                    // Scroll to error field
                    // In real implementation, calculate offset based on field
                    scope.launch {
                        scrollState.animateScrollTo(0)
                    }
                }
            }
        }
    }
    
    // RTL layout for Arabic
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Header
                TransactionScreenHeader(
                    onClose = { viewModel.onEvent(TransactionEvent.NavigateBack) },
                    onSave = { viewModel.onEvent(TransactionEvent.SubmitTransaction) },
                    isSaving = state.isSaving,
                    isFormValid = state.isFormValid || state.validationErrors.isEmpty()
                )
                
                // Content
                if (state.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AppColors.Primary)
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 100.dp)
                            .imePadding()
                    ) {
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Transaction Type Selector
                        TransactionTypeSelector(
                            selectedType = state.transactionType,
                            onTypeSelected = { viewModel.onEvent(TransactionEvent.TypeChanged(it)) },
                            enabled = !state.isSaving
                        )
                        
                        Spacer(modifier = Modifier.height(28.dp))
                        
                        // Amount Input
                        AmountInputField(
                            value = state.amount,
                            onValueChange = { viewModel.onEvent(TransactionEvent.AmountChanged(it)) },
                            hasError = state.hasError(FormField.AMOUNT),
                            errorMessage = state.getError(FormField.AMOUNT),
                            enabled = !state.isSaving
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Source Account
                        AccountSelectorCard(
                            selectedAccount = state.selectedSourceAccount,
                            label = if (state.transactionType == com.bashmaqawa.data.database.entities.TransactionType.INCOME) 
                                "الحساب المستلم" else "الحساب المصدر",
                            placeholder = "اختر الحساب",
                            onClick = { viewModel.onEvent(TransactionEvent.ShowSourceAccountPicker) },
                            hasError = state.hasError(FormField.SOURCE_ACCOUNT),
                            errorMessage = state.getError(FormField.SOURCE_ACCOUNT),
                            enabled = !state.isSaving
                        )
                        
                        // Destination Account (for transfers only)
                        AnimatedVisibility(
                            visible = state.isDestinationAccountVisible,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column {
                                Spacer(modifier = Modifier.height(16.dp))
                                AccountSelectorCard(
                                    selectedAccount = state.selectedDestinationAccount,
                                    label = "حساب الوجهة",
                                    placeholder = "اختر حساب الوجهة",
                                    onClick = { viewModel.onEvent(TransactionEvent.ShowDestinationAccountPicker) },
                                    hasError = state.hasError(FormField.DESTINATION_ACCOUNT),
                                    errorMessage = state.getError(FormField.DESTINATION_ACCOUNT),
                                    enabled = !state.isSaving
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Balance Preview
                        BalancePreviewCard(
                            currentBalance = state.sourceAccountBalance,
                            transactionAmount = state.amountDouble,
                            transactionType = state.transactionType,
                            accountName = state.selectedSourceAccount?.name,
                            isInsufficientBalance = state.hasInsufficientBalance
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Category (not for transfers)
                        AnimatedVisibility(
                            visible = state.isCategoryVisible,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column {
                                CategoryChipFlow(
                                    categories = state.availableCategories,
                                    selectedCategory = state.selectedCategory,
                                    onCategorySelected = { viewModel.onEvent(TransactionEvent.CategorySelected(it)) },
                                    transactionType = state.transactionType,
                                    hasError = state.hasError(FormField.CATEGORY),
                                    errorMessage = state.getError(FormField.CATEGORY),
                                    enabled = !state.isSaving
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }
                        
                        // Divider
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Optional Fields Section
                        Text(
                            text = "حقول إضافية",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Date
                        DateSelectorCard(
                            selectedDate = state.date,
                            onClick = { viewModel.onEvent(TransactionEvent.ShowDatePicker) },
                            enabled = !state.isSaving
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Project
                        ProjectSelectorCard(
                            selectedProject = state.selectedProject,
                            onClick = { viewModel.onEvent(TransactionEvent.ShowProjectPicker) },
                            enabled = !state.isSaving
                        )
                        
                        // Worker (visible for certain categories)
                        AnimatedVisibility(
                            visible = state.isWorkerVisible,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column {
                                Spacer(modifier = Modifier.height(12.dp))
                                WorkerSelectorCard(
                                    selectedWorker = state.selectedWorker,
                                    onClick = { viewModel.onEvent(TransactionEvent.ShowWorkerPicker) },
                                    enabled = !state.isSaving
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Payment Method
                        PaymentMethodCard(
                            selectedMethod = state.paymentMethod,
                            onClick = { viewModel.onEvent(TransactionEvent.ShowPaymentMethodPicker) },
                            enabled = !state.isSaving
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Description
                        DescriptionField(
                            value = state.description,
                            onValueChange = { viewModel.onEvent(TransactionEvent.DescriptionChanged(it)) },
                            enabled = !state.isSaving
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Reference Number
                        ReferenceNumberCard(
                            value = state.referenceNumber,
                            onValueChange = { viewModel.onEvent(TransactionEvent.ReferenceNumberChanged(it)) }
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Receipt Attachment
                        ReceiptAttachmentCard(
                            receiptUri = state.receiptImageUri,
                            onCameraClick = { viewModel.onEvent(TransactionEvent.LaunchCamera) },
                            onGalleryClick = { viewModel.onEvent(TransactionEvent.LaunchGallery) },
                            onRemoveClick = { viewModel.onEvent(TransactionEvent.RemoveReceipt) }
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
            
            // Bottom Save Button
            SaveTransactionButton(
                onClick = { viewModel.onEvent(TransactionEvent.SubmitTransaction) },
                isSaving = state.isSaving,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(20.dp)
            )
        }
        
        // Bottom Sheets and Dialogs
        
        // Source Account Picker
        if (state.showSourceAccountPicker) {
            AccountPickerBottomSheet(
                accounts = state.availableAccounts,
                selectedAccount = state.selectedSourceAccount,
                excludeAccount = if (state.isDestinationAccountVisible) state.selectedDestinationAccount else null,
                onSelect = { viewModel.onEvent(TransactionEvent.SourceAccountSelected(it)) },
                onDismiss = { viewModel.onEvent(TransactionEvent.DismissAccountPicker) }
            )
        }
        
        // Destination Account Picker
        if (state.showDestinationAccountPicker) {
            AccountPickerBottomSheet(
                accounts = state.availableAccounts,
                selectedAccount = state.selectedDestinationAccount,
                excludeAccount = state.selectedSourceAccount,
                onSelect = { viewModel.onEvent(TransactionEvent.DestinationAccountSelected(it)) },
                onDismiss = { viewModel.onEvent(TransactionEvent.DismissAccountPicker) }
            )
        }
        
        // Project Picker
        if (state.showProjectPicker) {
            ProjectPickerBottomSheet(
                projects = state.availableProjects,
                selectedProject = state.selectedProject,
                onSelect = { viewModel.onEvent(TransactionEvent.ProjectSelected(it)) },
                onDismiss = { viewModel.onEvent(TransactionEvent.DismissProjectPicker) }
            )
        }
        
        // Worker Picker
        if (state.showWorkerPicker) {
            WorkerPickerBottomSheet(
                workers = state.availableWorkers,
                selectedWorker = state.selectedWorker,
                onSelect = { viewModel.onEvent(TransactionEvent.WorkerSelected(it)) },
                onDismiss = { viewModel.onEvent(TransactionEvent.DismissWorkerPicker) }
            )
        }
        
        // Payment Method Picker
        if (state.showPaymentMethodPicker) {
            PaymentMethodPickerBottomSheet(
                selectedMethod = state.paymentMethod,
                onSelect = { viewModel.onEvent(TransactionEvent.PaymentMethodSelected(it)) },
                onDismiss = { viewModel.onEvent(TransactionEvent.DismissPaymentMethodPicker) }
            )
        }
        
        // Date Picker Dialog
        if (state.showDatePicker) {
            DatePickerDialogWrapper(
                initialDate = state.date,
                onDateSelected = { viewModel.onEvent(TransactionEvent.DateSelected(it)) },
                onDismiss = { viewModel.onEvent(TransactionEvent.DismissDatePicker) }
            )
        }
    }
}

/**
 * Screen Header
 */
@Composable
private fun TransactionScreenHeader(
    onClose: () -> Unit,
    onSave: () -> Unit,
    isSaving: Boolean,
    isFormValid: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Close button
            IconButton(onClick = onClose, enabled = !isSaving) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "إغلاق",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Title
            Text(
                text = "إضافة معاملة جديدة",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Save button
            IconButton(
                onClick = onSave,
                enabled = !isSaving && isFormValid
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = AppColors.Primary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Save,
                        contentDescription = "حفظ",
                        tint = if (isFormValid) AppColors.Primary 
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

/**
 * Description Field
 */
@Composable
private fun DescriptionField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Description,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "الوصف (اختياري)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("أدخل وصف المعاملة...") },
            minLines = 2,
            maxLines = 4,
            enabled = enabled,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

/**
 * Bottom Save Button
 */
@Composable
private fun SaveTransactionButton(
    onClick: () -> Unit,
    isSaving: Boolean,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = !isSaving,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.Primary,
            disabledContainerColor = AppColors.Primary.copy(alpha = 0.5f)
        )
    ) {
        if (isSaving) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = AppColors.White
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "جاري الحفظ...",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.White
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Save,
                contentDescription = null,
                tint = AppColors.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "حفظ المعاملة",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.White
            )
        }
    }
}

// =====================================================
// PICKER BOTTOM SHEETS
// =====================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountPickerBottomSheet(
    accounts: List<Account>,
    selectedAccount: Account?,
    excludeAccount: Account?,
    onSelect: (Account) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "اختر الحساب",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            accounts.filter { it != excludeAccount }.forEach { account ->
                val isSelected = account == selectedAccount
                Surface(
                    onClick = { onSelect(account) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) AppColors.Primary.copy(alpha = 0.1f) 
                        else MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = account.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            Text(
                                text = "الرصيد: ${String.format("%.2f", account.balance)} ج.م",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(AppColors.Primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✓", color = AppColors.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProjectPickerBottomSheet(
    projects: List<Project>,
    selectedProject: Project?,
    onSelect: (Project?) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "اختر المشروع",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Clear option
            Surface(
                onClick = { onSelect(null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (selectedProject == null) AppColors.Primary.copy(alpha = 0.1f) 
                    else MaterialTheme.colorScheme.surface
            ) {
                Text(
                    text = "بدون مشروع",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            projects.forEach { project ->
                val isSelected = project == selectedProject
                Surface(
                    onClick = { onSelect(project) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) AppColors.Primary.copy(alpha = 0.1f) 
                        else MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = project.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(AppColors.Primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✓", color = AppColors.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkerPickerBottomSheet(
    workers: List<Worker>,
    selectedWorker: Worker?,
    onSelect: (Worker?) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "اختر العامل",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Clear option
            Surface(
                onClick = { onSelect(null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (selectedWorker == null) AppColors.Primary.copy(alpha = 0.1f) 
                    else MaterialTheme.colorScheme.surface
            ) {
                Text(
                    text = "بدون عامل",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            workers.forEach { worker ->
                val isSelected = worker == selectedWorker
                Surface(
                    onClick = { onSelect(worker) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) AppColors.Primary.copy(alpha = 0.1f) 
                        else MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = worker.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(AppColors.Primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✓", color = AppColors.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentMethodPickerBottomSheet(
    selectedMethod: PaymentMethod?,
    onSelect: (PaymentMethod) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "اختر طريقة الدفع",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            PaymentMethod.entries.forEach { method ->
                val isSelected = method == selectedMethod
                Surface(
                    onClick = { onSelect(method) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) AppColors.Primary.copy(alpha = 0.1f) 
                        else MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = method.arabicName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(AppColors.Primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✓", color = AppColors.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialogWrapper(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val initialMillis = initialDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
    
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(selectedDate)
                    }
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
    ) {
        DatePicker(state = datePickerState)
    }
}
