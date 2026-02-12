package com.bashmaqawa.presentation.screens.bankstatement

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bashmaqawa.data.database.entities.Account
import com.bashmaqawa.data.database.entities.AccountType
import com.bashmaqawa.pdf.models.PdfGenerationProgress
import com.bashmaqawa.presentation.theme.AppColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Bank Statement Screen
 * شاشة كشف الحساب البنكي
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankStatementScreen(
    onNavigateBack: () -> Unit,
    viewModel: BankStatementViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is BankStatementEffect.NavigateBack -> onNavigateBack()
                is BankStatementEffect.ShowSnackbar -> { /* Snackbar handling */ }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("كشف الحساب البنكي", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.LightBackground,
                    titleContentColor = AppColors.Gray900
                )
            )
        },
        containerColor = AppColors.LightBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Account Selection Card
                item {
                    AccountSelectionCard(
                        selectedAccount = state.selectedAccount,
                        isLoading = state.isLoadingAccounts,
                        onClick = { viewModel.onEvent(BankStatementEvent.ShowAccountSelector) }
                    )
                }
                
                // Date Range Card
                item {
                    DateRangeCard(
                        startDate = state.startDate,
                        endDate = state.endDate,
                        isValid = state.isDateRangeValid,
                        onStartDateClick = { viewModel.onEvent(BankStatementEvent.ShowStartDatePicker) },
                        onEndDateClick = { viewModel.onEvent(BankStatementEvent.ShowEndDatePicker) },
                        onPresetClick = { preset ->
                            viewModel.onEvent(
                                when (preset) {
                                    DatePreset.THIS_MONTH -> BankStatementEvent.SetThisMonth
                                    DatePreset.LAST_MONTH -> BankStatementEvent.SetLastMonth
                                    DatePreset.LAST_3_MONTHS -> BankStatementEvent.SetLast3Months
                                    DatePreset.THIS_YEAR -> BankStatementEvent.SetThisYear
                                }
                            )
                        }
                    )
                }
                
                // Options Card
                item {
                    OptionsCard(
                        includeAnalytics = state.includeAnalytics,
                        onIncludeAnalyticsChange = { 
                            viewModel.onEvent(BankStatementEvent.SetIncludeAnalytics(it)) 
                        }
                    )
                }
                
                // Preview Section
                if (state.previewData != null) {
                    item {
                        StatementPreviewCard(
                            transactionCount = state.previewData!!.transactionCount,
                            totalDebits = state.previewData!!.totalDebits,
                            totalCredits = state.previewData!!.totalCredits,
                            openingBalance = state.previewData!!.openingBalance.amount,
                            closingBalance = state.previewData!!.closingBalance.amount
                        )
                    }
                }
                
                // Generate Button
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (state.isGenerating) {
                        GenerationProgressCard(
                            progress = state.generationProgress,
                            progressPercentage = state.progressPercentage,
                            onCancel = { viewModel.onEvent(BankStatementEvent.CancelGeneration) }
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Preview Button
                            if (state.previewData == null && state.canGenerate) {
                                OutlinedButton(
                                    onClick = { viewModel.onEvent(BankStatementEvent.LoadPreview) },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !state.isLoadingPreview,
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = AppColors.Primary
                                    )
                                ) {
                                    if (state.isLoadingPreview) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(Icons.Rounded.Visibility, contentDescription = null)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("معاينة الكشف")
                                }
                            }
                            
                            // Generate Button
                            GenerateButton(
                                enabled = state.canGenerate,
                                onClick = { viewModel.onEvent(BankStatementEvent.GenerateStatement) }
                            )
                        }
                    }
                }
                
                // Bottom spacing
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
            
            // Error Snackbar
            state.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.onEvent(BankStatementEvent.DismissError) }) {
                            Text("إغلاق", color = Color.White)
                        }
                    },
                    containerColor = AppColors.Error
                ) {
                    Text(error, color = Color.White)
                }
            }
        }
    }
    
    // Account Selector Bottom Sheet
    if (state.showAccountSelectorSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.onEvent(BankStatementEvent.HideAccountSelector) },
            containerColor = AppColors.LightSurface
        ) {
            AccountSelectorContent(
                accounts = state.availableAccounts,
                selectedAccount = state.selectedAccount,
                onSelect = { viewModel.onEvent(BankStatementEvent.SelectAccount(it)) }
            )
        }
    }
    
    // Date Pickers
    if (state.showStartDatePicker) {
        DatePickerDialog(
            title = "تاريخ البداية",
            initialDate = state.startDate,
            onDateSelected = { viewModel.onEvent(BankStatementEvent.SetStartDate(it)) },
            onDismiss = { viewModel.onEvent(BankStatementEvent.HideDatePickers) }
        )
    }
    
    if (state.showEndDatePicker) {
        DatePickerDialog(
            title = "تاريخ النهاية",
            initialDate = state.endDate,
            onDateSelected = { viewModel.onEvent(BankStatementEvent.SetEndDate(it)) },
            onDismiss = { viewModel.onEvent(BankStatementEvent.HideDatePickers) }
        )
    }
    
    // Success Dialog
    if (state.showSuccessDialog) {
        SuccessDialog(
            onDismiss = { viewModel.onEvent(BankStatementEvent.DismissSuccessDialog) },
            onOpen = { 
                viewModel.onEvent(BankStatementEvent.OpenPdf)
                viewModel.onEvent(BankStatementEvent.DismissSuccessDialog)
            },
            onShare = { 
                viewModel.onEvent(BankStatementEvent.SharePdf)
                viewModel.onEvent(BankStatementEvent.DismissSuccessDialog)
            }
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// ACCOUNT SELECTION COMPONENTS
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun AccountSelectionCard(
    selectedAccount: Account?,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    GlassmorphicCard(
        onClick = onClick,
        enabled = !isLoading
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Account Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(AppColors.Primary, AppColors.PrimaryDark)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        selectedAccount?.type?.let { getAccountIcon(it) } ?: Icons.Rounded.AccountBalance,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Account Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (selectedAccount != null) selectedAccount.name else "اختر حساباً",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.Gray900
                )
                if (selectedAccount != null) {
                    Text(
                        getAccountTypeArabic(selectedAccount.type),
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.Gray600
                    )
                }
            }
            
            Icon(
                Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                tint = AppColors.Gray600
            )
        }
    }
}

@Composable
private fun AccountSelectorContent(
    accounts: List<Account>,
    selectedAccount: Account?,
    onSelect: (Account) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            "اختر الحساب",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = AppColors.Gray900
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (accounts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "لا توجد حسابات متاحة",
                    color = AppColors.Gray600
                )
            }
        } else {
            accounts.forEach { account ->
                AccountItem(
                    account = account,
                    isSelected = account.id == selectedAccount?.id,
                    onClick = { onSelect(account) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun AccountItem(
    account: Account,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) AppColors.Primary.copy(alpha = 0.1f) else AppColors.LightSurface
        ),
        border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(AppColors.Primary, AppColors.PrimaryDark))
        ) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) AppColors.Primary else AppColors.Gray200
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    getAccountIcon(account.type),
                    contentDescription = null,
                    tint = if (isSelected) Color.White else AppColors.Gray600,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    account.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.Gray900
                )
                Text(
                    getAccountTypeArabic(account.type),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.Gray600
                )
            }
            
            Text(
                formatCurrency(account.balance),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (account.balance >= 0) AppColors.Success else AppColors.Error
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// DATE RANGE COMPONENTS
// ═══════════════════════════════════════════════════════════════════════════

enum class DatePreset {
    THIS_MONTH, LAST_MONTH, LAST_3_MONTHS, THIS_YEAR
}

@Composable
private fun DateRangeCard(
    startDate: LocalDate,
    endDate: LocalDate,
    isValid: Boolean,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit,
    onPresetClick: (DatePreset) -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    
    GlassmorphicCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "الفترة الزمنية",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.Gray900
            )
            
            // Date Inputs Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DateInputButton(
                    label = "من",
                    date = startDate.format(dateFormatter),
                    onClick = onStartDateClick,
                    modifier = Modifier.weight(1f),
                    isError = !isValid
                )
                
                DateInputButton(
                    label = "إلى",
                    date = endDate.format(dateFormatter),
                    onClick = onEndDateClick,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Error message
            if (!isValid) {
                Text(
                    "تاريخ البداية يجب أن يكون قبل تاريخ النهاية",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.Error
                )
            }
            
            // Quick Presets
            Text(
                "اختيار سريع",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.Gray600
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PresetChip("هذا الشهر", { onPresetClick(DatePreset.THIS_MONTH) }, Modifier.weight(1f))
                PresetChip("الشهر الماضي", { onPresetClick(DatePreset.LAST_MONTH) }, Modifier.weight(1f))
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PresetChip("آخر 3 أشهر", { onPresetClick(DatePreset.LAST_3_MONTHS) }, Modifier.weight(1f))
                PresetChip("هذا العام", { onPresetClick(DatePreset.THIS_YEAR) }, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun DateInputButton(
    label: String,
    date: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    Column(modifier = modifier) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.Gray600
        )
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = AppColors.Gray100,
            border = if (isError) 
                androidx.compose.foundation.BorderStroke(1.dp, AppColors.Error)
            else null
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.CalendarMonth,
                    contentDescription = null,
                    tint = if (isError) AppColors.Error else AppColors.Primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    date,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.Gray900
                )
            }
        }
    }
}

@Composable
private fun PresetChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = AppColors.Primary.copy(alpha = 0.1f)
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.Primary,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    title: String,
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.toEpochDay() * 24 * 60 * 60 * 1000
    )
    
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    val date = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                    onDateSelected(date)
                }
            }) {
                Text("تأكيد")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            title = { Text(title, modifier = Modifier.padding(16.dp)) }
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// OPTIONS & PREVIEW COMPONENTS
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun OptionsCard(
    includeAnalytics: Boolean,
    onIncludeAnalyticsChange: (Boolean) -> Unit
) {
    GlassmorphicCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                "خيارات التقرير",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.Gray900
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "تضمين التحليلات",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.Gray900
                    )
                    Text(
                        "متوسط الرصيد، أعلى/أقل رصيد، إلخ",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.Gray600
                    )
                }
                
                Switch(
                    checked = includeAnalytics,
                    onCheckedChange = onIncludeAnalyticsChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = AppColors.Primary
                    )
                )
            }
        }
    }
}

@Composable
private fun StatementPreviewCard(
    transactionCount: Int,
    totalDebits: Double,
    totalCredits: Double,
    openingBalance: Double,
    closingBalance: Double
) {
    GlassmorphicCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.Preview,
                    contentDescription = null,
                    tint = AppColors.Primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "معاينة الكشف",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.Gray900
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PreviewStatItem("المعاملات", transactionCount.toString(), AppColors.Primary)
                PreviewStatItem("المدين", formatCurrencyShort(totalDebits), AppColors.Error)
                PreviewStatItem("الدائن", formatCurrencyShort(totalCredits), AppColors.Success)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            HorizontalDivider(color = AppColors.Gray300)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("الرصيد الافتتاحي", style = MaterialTheme.typography.bodySmall, color = AppColors.Gray600)
                    Text(formatCurrency(openingBalance), fontWeight = FontWeight.SemiBold, color = AppColors.Gray900)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("الرصيد الختامي", style = MaterialTheme.typography.bodySmall, color = AppColors.Gray600)
                    Text(
                        formatCurrency(closingBalance), 
                        fontWeight = FontWeight.Bold,
                        color = if (closingBalance >= 0) AppColors.Success else AppColors.Error
                    )
                }
            }
        }
    }
}

@Composable
private fun PreviewStatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.Gray600
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// GENERATION & PROGRESS COMPONENTS
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun GenerateButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.Primary,
            disabledContainerColor = AppColors.Primary.copy(alpha = 0.3f)
        )
    ) {
        Icon(Icons.Rounded.PictureAsPdf, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "إنشاء كشف الحساب",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun GenerationProgressCard(
    progress: PdfGenerationProgress,
    progressPercentage: Float,
    onCancel: () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progressPercentage / 100f,
        animationSpec = tween(300),
        label = "progress"
    )
    
    GlassmorphicCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated progress circle
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxSize(),
                    color = AppColors.Primary,
                    strokeWidth = 6.dp,
                    trackColor = AppColors.Primary.copy(alpha = 0.2f)
                )
                Text(
                    "${progressPercentage.toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                progress.getStatusMessage(),
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray900
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = onCancel,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AppColors.Error
                )
            ) {
                Text("إلغاء")
            }
        }
    }
}

@Composable
private fun SuccessDialog(
    onDismiss: () -> Unit,
    onOpen: () -> Unit,
    onShare: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(AppColors.Success.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = AppColors.Success,
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        title = {
            Text(
                "تم إنشاء الكشف بنجاح!",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                "تم إنشاء كشف الحساب البنكي بنجاح.\nيمكنك الآن فتحه أو مشاركته.",
                textAlign = TextAlign.Center,
                color = AppColors.Gray600
            )
        },
        confirmButton = {
            Button(
                onClick = onOpen,
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)
            ) {
                Icon(Icons.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("فتح")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onShare) {
                Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("مشاركة")
            }
        },
        containerColor = AppColors.LightSurface
    )
}

// ═══════════════════════════════════════════════════════════════════════════
// SHARED COMPONENTS
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val cardModifier = modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .background(AppColors.LightSurface)
        .border(
            width = 1.dp,
            color = AppColors.Gray200,
            shape = RoundedCornerShape(16.dp)
        )
    
    if (onClick != null) {
        Box(
            modifier = cardModifier.clickable(enabled = enabled, onClick = onClick)
        ) {
            content()
        }
    } else {
        Box(modifier = cardModifier) {
            content()
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// UTILITY FUNCTIONS
// ═══════════════════════════════════════════════════════════════════════════

private fun getAccountIcon(type: AccountType?): ImageVector = when (type) {
    AccountType.CASH_BOX -> Icons.Rounded.Savings
    AccountType.BANK -> Icons.Rounded.AccountBalance
    AccountType.WALLET -> Icons.Rounded.Wallet
    else -> Icons.Rounded.AccountBalance
}

private fun getAccountTypeArabic(type: AccountType?): String = when (type) {
    AccountType.CASH_BOX -> "صندوق"
    AccountType.BANK -> "بنك"
    AccountType.WALLET -> "محفظة"
    AccountType.RECEIVABLE -> "ذمم مدينة"
    AccountType.PAYABLE -> "ذمم دائنة"
    null -> "غير محدد"
}

private fun formatCurrency(amount: Double): String {
    val formatted = String.format("%.2f", kotlin.math.abs(amount))
    val sign = if (amount < 0) "-" else ""
    return "$sign$formatted ج.م"
}

private fun formatCurrencyShort(amount: Double): String {
    return when {
        amount >= 1_000_000 -> String.format("%.1fM", amount / 1_000_000)
        amount >= 1_000 -> String.format("%.1fK", amount / 1_000)
        else -> String.format("%.0f", amount)
    }
}
