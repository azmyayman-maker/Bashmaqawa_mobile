package com.bashmaqawa.presentation.screens.reports

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bashmaqawa.core.DateRange
import com.bashmaqawa.data.database.entities.Account
import com.bashmaqawa.data.database.entities.Worker
import com.bashmaqawa.pdf.models.PdfGenerationProgress
import com.bashmaqawa.pdf.models.ReportType
import com.bashmaqawa.presentation.theme.AppColors
import java.io.File

/**
 * Report Selection Screen
 * شاشة اختيار التقارير
 * 
 * Premium UI for selecting and generating PDF reports with real-time progress tracking.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportSelectionScreen(
    viewModel: ReportViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToBankStatement: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val context = LocalContext.current
    
    // Show toast when report is generated
    LaunchedEffect(uiState.generatedFile) {
        uiState.generatedFile?.let { file ->
            Toast.makeText(
                context,
                "تم إنشاء التقرير بنجاح: ${uiState.pageCount} صفحات",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    
    // Show error toast
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "التقارير المالية",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "العودة")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.LightSurface,
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Section
                item {
                    ReportHeaderSection()
                }
                
                // Quick Date Range Selection
                item {
                    DateRangeQuickSelect(
                        selectedRange = uiState.dateRange,
                        onThisMonth = { viewModel.setThisMonth() },
                        onThisWeek = { viewModel.setThisWeek() },
                        onLast30Days = { viewModel.setLast30Days() }
                    )
                }
                
                // Report Type Cards
                item {
                    Text(
                        "اختر نوع التقرير",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Gray900,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                items(getReportTypeCards()) { reportCard ->
                    ReportTypeCard(
                        reportCard = reportCard,
                        isSelected = uiState.selectedReportType == reportCard.type,
                        onSelect = { 
                            // Bank Statement has its own dedicated screen
                            if (reportCard.type == ReportType.BANK_STATEMENT) {
                                onNavigateToBankStatement()
                            } else {
                                viewModel.selectReportType(reportCard.type)
                            }
                        }
                    )
                }
                
                // Entity Selection (if required)
                item {
                    AnimatedVisibility(
                        visible = uiState.requiresAccountSelection,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        AccountSelectionCard(
                            accounts = uiState.availableAccounts,
                            selectedAccount = uiState.selectedAccount,
                            onSelectAccount = { viewModel.selectAccount(it) }
                        )
                    }
                }
                
                item {
                    AnimatedVisibility(
                        visible = uiState.requiresWorkerSelection,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        WorkerSelectionCard(
                            workers = uiState.availableWorkers,
                            selectedWorker = uiState.selectedWorker,
                            onSelectWorker = { viewModel.selectWorker(it) }
                        )
                    }
                }
                
                // Generate Button
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    GenerateReportButton(
                        isEnabled = uiState.canGenerate && !uiState.isGenerating,
                        isLoading = uiState.isGenerating,
                        progress = progress,
                        onClick = { viewModel.generateReport() }
                    )
                }
                
                // Result Section
                item {
                    AnimatedVisibility(
                        visible = uiState.hasResult,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        uiState.generatedFile?.let { file ->
                            ReportResultCard(
                                file = file,
                                pageCount = uiState.pageCount,
                                generationTimeMs = uiState.generationTimeMs,
                                onShare = { viewModel.shareReport() },
                                onOpen = { viewModel.openReport() },
                                onClear = { viewModel.clearResult() }
                            )
                        }
                    }
                }
                
                // Bottom spacing
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
            
            // Loading Overlay
            if (uiState.isGenerating) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    GenerationProgressCard(progress = progress)
                }
            }
        }
    }
}

/**
 * Report Header Section with gradient background
 */
@Composable
private fun ReportHeaderSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            AppColors.Primary,
                            AppColors.PrimaryDark
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Description,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "مولد التقارير المالية",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "إنشاء تقارير PDF احترافية بجودة عالية",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

/**
 * Quick date range selection buttons
 */
@Composable
private fun DateRangeQuickSelect(
    selectedRange: DateRange,
    onThisMonth: () -> Unit,
    onThisWeek: () -> Unit,
    onLast30Days: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.LightSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "الفترة الزمنية",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = AppColors.Gray900
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DateRangeChip(
                    label = "هذا الشهر",
                    isSelected = true,
                    onClick = onThisMonth,
                    modifier = Modifier.weight(1f)
                )
                DateRangeChip(
                    label = "هذا الأسبوع",
                    isSelected = false,
                    onClick = onThisWeek,
                    modifier = Modifier.weight(1f)
                )
                DateRangeChip(
                    label = "آخر 30 يوم",
                    isSelected = false,
                    onClick = onLast30Days,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "من ${selectedRange.startDateString} إلى ${selectedRange.endDateString}",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.Gray600,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun DateRangeChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) AppColors.Primary else AppColors.LightSurface,
        border = if (!isSelected) {
            androidx.compose.foundation.BorderStroke(1.dp, AppColors.Gray400)
        } else null
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) Color.White else AppColors.Gray900,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Report Type Card Data
 */
data class ReportCardData(
    val type: ReportType,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconColor: Color
)

private fun getReportTypeCards(): List<ReportCardData> = listOf(
    ReportCardData(
        type = ReportType.BANK_STATEMENT,
        title = "كشف حساب بنكي",
        description = "كشف حساب بنكي احترافي مع الرصيد المتحرك",
        icon = Icons.Outlined.CreditCard,
        iconColor = Color(0xFF00BCD4)
    ),
    ReportCardData(
        type = ReportType.ACCOUNT_STATEMENT,
        title = "كشف حساب",
        description = "تقرير تفصيلي لحركات حساب معين",
        icon = Icons.Outlined.AccountBalance,
        iconColor = Color(0xFF2196F3)
    ),
    ReportCardData(
        type = ReportType.PROFIT_LOSS,
        title = "الأرباح والخسائر",
        description = "ملخص الإيرادات والمصروفات",
        icon = Icons.Outlined.TrendingUp,
        iconColor = Color(0xFF4CAF50)
    ),
    ReportCardData(
        type = ReportType.TRANSACTION_LEDGER,
        title = "دفتر المعاملات",
        description = "قائمة كاملة بجميع المعاملات",
        icon = Icons.Outlined.Receipt,
        iconColor = Color(0xFF9C27B0)
    ),
    ReportCardData(
        type = ReportType.PAYROLL_SUMMARY,
        title = "كشف الرواتب",
        description = "ملخص أجور العمال",
        icon = Icons.Outlined.Payments,
        iconColor = Color(0xFFFF9800)
    ),
    ReportCardData(
        type = ReportType.JOURNAL_ENTRIES,
        title = "قيود اليومية",
        description = "سجل المراجعة المحاسبي",
        icon = Icons.Outlined.Book,
        iconColor = Color(0xFF607D8B)
    ),
    ReportCardData(
        type = ReportType.ANALYTICS_SUMMARY,
        title = "ملخص تحليلي",
        description = "نظرة شاملة على الأداء المالي",
        icon = Icons.Outlined.Analytics,
        iconColor = Color(0xFFE91E63)
    )
)

@Composable
private fun ReportTypeCard(
    reportCard: ReportCardData,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = AppColors.Primary,
                        shape = RoundedCornerShape(12.dp)
                    )
                } else Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                AppColors.Primary.copy(alpha = 0.1f)
            } else {
                AppColors.LightSurface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = reportCard.iconColor.copy(alpha = 0.15f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = reportCard.icon,
                    contentDescription = null,
                    tint = reportCard.iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reportCard.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Gray900
                )
                Text(
                    text = reportCard.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.Gray600
                )
            }
            
            // Selection indicator
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "محدد",
                    tint = AppColors.Primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Account Selection Card
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountSelectionCard(
    accounts: List<Account>,
    selectedAccount: Account?,
    onSelectAccount: (Account?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.LightSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "اختر الحساب",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = AppColors.Gray900
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = selectedAccount?.name ?: "اختر حساب...",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.Primary,
                        unfocusedBorderColor = AppColors.Gray400
                    )
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    accounts.forEach { account ->
                        DropdownMenuItem(
                            text = { Text(account.name) },
                            onClick = {
                                onSelectAccount(account)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Worker Selection Card
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkerSelectionCard(
    workers: List<Worker>,
    selectedWorker: Worker?,
    onSelectWorker: (Worker?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.LightSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "اختر العامل",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = AppColors.Gray900
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = selectedWorker?.name ?: "اختر عامل...",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.Primary,
                        unfocusedBorderColor = AppColors.Gray400
                    )
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    workers.forEach { worker ->
                        DropdownMenuItem(
                            text = { Text(worker.name) },
                            onClick = {
                                onSelectWorker(worker)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Generate Report Button with loading state
 */
@Composable
private fun GenerateReportButton(
    isEnabled: Boolean,
    isLoading: Boolean,
    progress: PdfGenerationProgress,
    onClick: () -> Unit
) {
    val progressPercent = (progress.getProgressPercent() * 100).toInt()
    
    Button(
        onClick = onClick,
        enabled = isEnabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.Primary,
            disabledContainerColor = AppColors.Primary.copy(alpha = 0.5f)
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "جاري الإنشاء... $progressPercent%",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
        } else {
            Icon(
                imageVector = Icons.Default.PictureAsPdf,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "إنشاء التقرير",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Generation Progress Card (overlay)
 */
@Composable
private fun GenerationProgressCard(progress: PdfGenerationProgress) {
    val progressPercent = (progress.getProgressPercent() * 100).toInt()
    val statusMessage = progress.getStatusMessage()
    val currentPage = when (progress) {
        is PdfGenerationProgress.RenderingPage -> progress.current
        else -> 0
    }
    val totalPages = when (progress) {
        is PdfGenerationProgress.RenderingPage -> progress.total
        else -> 0
    }
    
    Card(
        modifier = Modifier
            .width(280.dp)
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.LightSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                progress = { progress.getProgressPercent() },
                modifier = Modifier.size(64.dp),
                strokeWidth = 6.dp,
                color = AppColors.Primary,
                trackColor = AppColors.Gray300
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = statusMessage,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.Gray900,
                textAlign = TextAlign.Center
            )
            
            if (currentPage > 0) {
                Text(
                    text = "صفحة $currentPage من $totalPages",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.Gray600
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = { progress.getProgressPercent() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = AppColors.Primary,
                trackColor = AppColors.Gray300
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "$progressPercent%",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = AppColors.Primary
            )
        }
    }
}

/**
 * Report Result Card with actions
 */
@Composable
private fun ReportResultCard(
    file: File,
    pageCount: Int,
    generationTimeMs: Long,
    onShare: () -> Unit,
    onOpen: () -> Unit,
    onClear: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.SuccessLight
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = AppColors.Success,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "تم إنشاء التقرير بنجاح!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Success
                    )
                    Text(
                        "$pageCount صفحات • ${generationTimeMs}ms",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.Gray600
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = file.name,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray900
            )
            
            Text(
                text = "الحجم: ${file.length() / 1024} KB",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.Gray600
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpen,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.OpenInNew, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("فتح")
                }
                
                Button(
                    onClick = onShare,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)
                ) {
                    Icon(Icons.Default.Share, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("مشاركة")
                }
                
                IconButton(onClick = onClear) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = AppColors.Gray600
                    )
                }
            }
        }
    }
}
