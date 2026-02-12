package com.bashmaqawa.presentation.screens.analytics

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.bashmaqawa.R
import com.bashmaqawa.presentation.components.*
import com.bashmaqawa.presentation.theme.AppColors
import com.bashmaqawa.utils.CurrencyFormatter
import com.bashmaqawa.utils.PdfExporter

/**
 * Analytics Screen with ViewModel Integration
 * شاشة التحليلات مع تكامل الـ ViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    navController: NavController,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var isExporting by remember { mutableStateOf(false) }
    
    // Export PDF function
    fun exportPdf() {
        isExporting = true
        val periodTitle = when (uiState.selectedPeriod) {
            0 -> "هذا الشهر"
            1 -> "الشهر الماضي"
            2 -> "هذا العام"
            else -> "هذا الشهر"
        }
        
        PdfExporter.exportAnalyticsReport(
            context = context,
            totalIncome = uiState.totalIncome,
            totalExpense = uiState.totalExpense,
            netProfit = uiState.netProfit,
            activeProjects = uiState.activeProjectsCount,
            totalWorkers = uiState.totalWorkersCount,
            periodTitle = periodTitle,
            onSuccess = { file ->
                isExporting = false
                PdfExporter.sharePdf(context, file)
            },
            onError = { e ->
                isExporting = false
                Toast.makeText(context, "فشل تصدير PDF: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.analytics_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(
                        onClick = { exportPdf() },
                        enabled = !isExporting && !uiState.isLoading
                    ) {
                        if (isExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Filled.PictureAsPdf, contentDescription = "Export PDF")
                        }
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
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Period Selector
                item {
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("هذا الشهر", "الشهر الماضي", "هذا العام").forEachIndexed { index, label ->
                            SegmentedButton(
                                selected = uiState.selectedPeriod == index,
                                onClick = { viewModel.onPeriodSelected(index) },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = 3
                                )
                            ) {
                                Text(label)
                            }
                        }
                    }
                }
                
                // Summary Cards
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        InfoCard(
                            title = "إجمالي الإيرادات",
                            value = CurrencyFormatter.format(uiState.totalIncome),
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            gradient = AppColors.SuccessGradient,
                            modifier = Modifier.weight(1f)
                        )
                        InfoCard(
                            title = "إجمالي المصروفات",
                            value = CurrencyFormatter.format(uiState.totalExpense),
                            icon = Icons.AutoMirrored.Filled.TrendingDown,
                            gradient = listOf(AppColors.Error, AppColors.Error.copy(alpha = 0.8f)),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // Net Profit Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (uiState.netProfit >= 0) 
                                AppColors.Success.copy(alpha = 0.1f)
                            else AppColors.Error.copy(alpha = 0.1f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.profit_loss),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = CurrencyFormatter.format(uiState.netProfit),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.netProfit >= 0) AppColors.Success else AppColors.Error
                            )
                            if (uiState.totalIncome > 0) {
                                Text(
                                    text = "هامش الربح: %.1f%%".format(uiState.profitMargin),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                // Quick Stats
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatsCard(
                            title = "المشاريع النشطة",
                            value = uiState.activeProjectsCount.toString(),
                            icon = Icons.Filled.Business,
                            iconTint = AppColors.Primary,
                            modifier = Modifier.weight(1f)
                        )
                        StatsCard(
                            title = "إجمالي العمال",
                            value = uiState.totalWorkersCount.toString(),
                            icon = Icons.Filled.Group,
                            iconTint = AppColors.Accent,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // Expense Categories
                if (uiState.expenseCategories.isNotEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = stringResource(R.string.expenses_by_category),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Donut Chart
                                val expenseColors = listOf(
                                    AppColors.Error, 
                                    AppColors.Error.copy(alpha = 0.7f),
                                    AppColors.Error.copy(alpha = 0.5f),
                                    AppColors.Warning,
                                    AppColors.Warning.copy(alpha = 0.7f)
                                )
                                
                                Box(modifier = Modifier.height(200.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    DonutChart(
                                        data = uiState.expenseCategories,
                                        totalAmount = uiState.totalExpense,
                                        colors = expenseColors
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                uiState.expenseCategories.forEachIndexed { index, category ->
                                    CategoryRow(
                                        name = category.category,
                                        amount = category.total,
                                        count = category.count,
                                        totalAmount = uiState.totalExpense,
                                        color = expenseColors.getOrElse(index) { Color.Gray }
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }
                        }
                    }
                }
                
                // Income Categories
                if (uiState.incomeCategories.isNotEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "الإيرادات حسب الفئة",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                val incomeColors = listOf(
                                     AppColors.Success,
                                     AppColors.Success.copy(alpha = 0.7f),
                                     AppColors.Success.copy(alpha = 0.5f),
                                     AppColors.Primary,
                                     AppColors.Primary.copy(alpha = 0.7f)
                                )
                                
                                Box(modifier = Modifier.height(200.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                     DonutChart(
                                        data = uiState.incomeCategories,
                                        totalAmount = uiState.totalIncome,
                                        colors = incomeColors
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                uiState.incomeCategories.forEachIndexed { index, category ->
                                    CategoryRow(
                                        name = category.category,
                                        amount = category.total,
                                        count = category.count,
                                        totalAmount = uiState.totalIncome,
                                        isIncome = true,
                                        color = incomeColors.getOrElse(index) { Color.Gray }
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Category breakdown row with progress bar
 */
@Composable
fun CategoryRow(
    name: String,
    amount: Double,
    count: Int,
    totalAmount: Double,
    isIncome: Boolean = false,
    color: Color
) {
    val percentage = if (totalAmount > 0) (amount / totalAmount).toFloat() else 0f
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Color Indicator
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(color, shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = CurrencyFormatter.format(amount),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface 
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { percentage },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(MaterialTheme.shapes.small),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Text(
            text = "$count معاملة • %.1f%%".format(percentage * 100),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
