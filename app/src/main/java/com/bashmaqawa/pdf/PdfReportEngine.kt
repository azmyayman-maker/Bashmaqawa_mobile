package com.bashmaqawa.pdf

import android.content.Context
import android.content.Intent
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.core.content.FileProvider
import com.bashmaqawa.pdf.core.PdfPageRenderer
import com.bashmaqawa.pdf.core.PdfTableRenderer
import com.bashmaqawa.pdf.models.*
import com.bashmaqawa.pdf.templates.*
import com.bashmaqawa.utils.CurrencyFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PDF Report Engine
 * محرك إنشاء تقارير PDF
 * 
 * Enterprise-grade PDF generation engine for the Bashmaqawa application.
 * Features:
 * - Multi-page support with automatic pagination
 * - Consistent headers and footers
 * - Progress tracking
 * - Cancellation support
 * - Memory-efficient streaming
 */
@Singleton
class PdfReportEngine @Inject constructor(
    private val context: Context
) {
    private val pageRenderer = PdfPageRenderer()
    private val tableRenderer = PdfTableRenderer()
    
    private val _progress = MutableStateFlow<PdfGenerationProgress>(PdfGenerationProgress.Initializing)
    val progress: StateFlow<PdfGenerationProgress> = _progress.asStateFlow()
    
    @Volatile
    private var isCancelled = false
    
    /**
     * Cancel ongoing PDF generation
     */
    fun cancelGeneration() {
        isCancelled = true
    }
    
    /**
     * Reset cancellation flag
     */
    private fun resetCancellation() {
        isCancelled = false
    }
    
    // ══════════════════════════════════════════════════════════════════════
    // PUBLIC GENERATION METHODS
    // ══════════════════════════════════════════════════════════════════════
    
    /**
     * Generate Account Statement Report
     */
    suspend fun generateAccountStatement(
        data: AccountStatementData,
        config: ReportConfig
    ): PdfGenerationResult = withContext(Dispatchers.IO) {
        resetCancellation()
        val startTime = System.currentTimeMillis()
        
        try {
            _progress.value = PdfGenerationProgress.Initializing
            
            if (data.transactions.isEmpty() && data.openingBalance == 0.0) {
                return@withContext PdfGenerationResult.Failure.NoData
            }
            
            _progress.value = PdfGenerationProgress.LoadingData(0.5f)
            
            val template = AccountStatementTemplate(pageRenderer, tableRenderer)
            val pdfDocument = PdfDocument()
            
            _progress.value = PdfGenerationProgress.LoadingData(1f)
            
            val pageCount = template.render(pdfDocument, data, config)
            
            if (isCancelled) {
                pdfDocument.close()
                return@withContext PdfGenerationResult.Failure.Cancelled
            }
            
            _progress.value = PdfGenerationProgress.Saving(0.5f)
            
            val file = saveDocument(pdfDocument, ReportType.ACCOUNT_STATEMENT, config)
            pdfDocument.close()
            
            _progress.value = PdfGenerationProgress.Complete
            
            PdfGenerationResult.Success(
                file = file,
                pageCount = pageCount,
                generationTimeMs = System.currentTimeMillis() - startTime
            )
        } catch (e: Exception) {
            _progress.value = PdfGenerationProgress.Error(e.message ?: "Unknown error")
            PdfGenerationResult.Failure.RenderError(0, e)
        }
    }
    
    /**
     * Generate Profit & Loss Report
     */
    suspend fun generateProfitLossReport(
        data: ProfitLossData,
        config: ReportConfig
    ): PdfGenerationResult = withContext(Dispatchers.IO) {
        resetCancellation()
        val startTime = System.currentTimeMillis()
        
        try {
            _progress.value = PdfGenerationProgress.Initializing
            
            if (data.totalIncome == 0.0 && data.totalExpenses == 0.0) {
                return@withContext PdfGenerationResult.Failure.NoData
            }
            
            _progress.value = PdfGenerationProgress.LoadingData(1f)
            
            val template = ProfitLossReportTemplate(pageRenderer, tableRenderer)
            val pdfDocument = PdfDocument()
            
            val pageCount = template.render(pdfDocument, data, config)
            
            if (isCancelled) {
                pdfDocument.close()
                return@withContext PdfGenerationResult.Failure.Cancelled
            }
            
            _progress.value = PdfGenerationProgress.Saving(0.5f)
            
            val file = saveDocument(pdfDocument, ReportType.PROFIT_LOSS, config)
            pdfDocument.close()
            
            _progress.value = PdfGenerationProgress.Complete
            
            PdfGenerationResult.Success(
                file = file,
                pageCount = pageCount,
                generationTimeMs = System.currentTimeMillis() - startTime
            )
        } catch (e: Exception) {
            _progress.value = PdfGenerationProgress.Error(e.message ?: "Unknown error")
            PdfGenerationResult.Failure.RenderError(0, e)
        }
    }
    
    /**
     * Generate Transaction Ledger Report
     */
    suspend fun generateTransactionLedger(
        transactions: List<TransactionRow>,
        periodStart: String,
        periodEnd: String,
        config: ReportConfig
    ): PdfGenerationResult = withContext(Dispatchers.IO) {
        resetCancellation()
        val startTime = System.currentTimeMillis()
        
        try {
            _progress.value = PdfGenerationProgress.Initializing
            
            if (transactions.isEmpty()) {
                return@withContext PdfGenerationResult.Failure.NoData
            }
            
            _progress.value = PdfGenerationProgress.LoadingData(1f)
            
            val template = TransactionLedgerTemplate(pageRenderer, tableRenderer)
            val pdfDocument = PdfDocument()
            
            val pageCount = template.render(pdfDocument, transactions, periodStart, periodEnd, config)
            
            if (isCancelled) {
                pdfDocument.close()
                return@withContext PdfGenerationResult.Failure.Cancelled
            }
            
            _progress.value = PdfGenerationProgress.Saving(0.5f)
            
            val file = saveDocument(pdfDocument, ReportType.TRANSACTION_LEDGER, config)
            pdfDocument.close()
            
            _progress.value = PdfGenerationProgress.Complete
            
            PdfGenerationResult.Success(
                file = file,
                pageCount = pageCount,
                generationTimeMs = System.currentTimeMillis() - startTime
            )
        } catch (e: Exception) {
            _progress.value = PdfGenerationProgress.Error(e.message ?: "Unknown error")
            PdfGenerationResult.Failure.RenderError(0, e)
        }
    }
    
    /**
     * Generate Payroll Summary Report
     */
    suspend fun generatePayrollReport(
        data: PayrollReportData,
        config: ReportConfig
    ): PdfGenerationResult = withContext(Dispatchers.IO) {
        resetCancellation()
        val startTime = System.currentTimeMillis()
        
        try {
            _progress.value = PdfGenerationProgress.Initializing
            
            if (data.entries.isEmpty()) {
                return@withContext PdfGenerationResult.Failure.NoData
            }
            
            _progress.value = PdfGenerationProgress.LoadingData(1f)
            
            val template = PayrollReportTemplate(pageRenderer, tableRenderer)
            val pdfDocument = PdfDocument()
            
            val pageCount = template.render(pdfDocument, data, config)
            
            if (isCancelled) {
                pdfDocument.close()
                return@withContext PdfGenerationResult.Failure.Cancelled
            }
            
            _progress.value = PdfGenerationProgress.Saving(0.5f)
            
            val file = saveDocument(pdfDocument, ReportType.PAYROLL_SUMMARY, config)
            pdfDocument.close()
            
            _progress.value = PdfGenerationProgress.Complete
            
            PdfGenerationResult.Success(
                file = file,
                pageCount = pageCount,
                generationTimeMs = System.currentTimeMillis() - startTime
            )
        } catch (e: Exception) {
            _progress.value = PdfGenerationProgress.Error(e.message ?: "Unknown error")
            PdfGenerationResult.Failure.RenderError(0, e)
        }
    }
    
    /**
     * Generate Project Report
     */
    suspend fun generateProjectReport(
        data: ProjectReportData,
        config: ReportConfig
    ): PdfGenerationResult = withContext(Dispatchers.IO) {
        resetCancellation()
        val startTime = System.currentTimeMillis()
        
        try {
            _progress.value = PdfGenerationProgress.Initializing
            _progress.value = PdfGenerationProgress.LoadingData(1f)
            
            val template = ProjectReportTemplate(pageRenderer, tableRenderer)
            val pdfDocument = PdfDocument()
            
            val pageCount = template.render(pdfDocument, data, config)
            
            if (isCancelled) {
                pdfDocument.close()
                return@withContext PdfGenerationResult.Failure.Cancelled
            }
            
            _progress.value = PdfGenerationProgress.Saving(0.5f)
            
            val file = saveDocument(pdfDocument, ReportType.PROJECT_REPORT, config)
            pdfDocument.close()
            
            _progress.value = PdfGenerationProgress.Complete
            
            PdfGenerationResult.Success(
                file = file,
                pageCount = pageCount,
                generationTimeMs = System.currentTimeMillis() - startTime
            )
        } catch (e: Exception) {
            _progress.value = PdfGenerationProgress.Error(e.message ?: "Unknown error")
            PdfGenerationResult.Failure.RenderError(0, e)
        }
    }
    
    /**
     * Generate Journal Entries Report
     */
    suspend fun generateJournalEntriesReport(
        entries: List<JournalEntryRow>,
        periodStart: String,
        periodEnd: String,
        config: ReportConfig
    ): PdfGenerationResult = withContext(Dispatchers.IO) {
        resetCancellation()
        val startTime = System.currentTimeMillis()
        
        try {
            _progress.value = PdfGenerationProgress.Initializing
            
            if (entries.isEmpty()) {
                return@withContext PdfGenerationResult.Failure.NoData
            }
            
            _progress.value = PdfGenerationProgress.LoadingData(1f)
            
            val template = JournalEntriesTemplate(pageRenderer, tableRenderer)
            val pdfDocument = PdfDocument()
            
            val pageCount = template.render(pdfDocument, entries, periodStart, periodEnd, config)
            
            if (isCancelled) {
                pdfDocument.close()
                return@withContext PdfGenerationResult.Failure.Cancelled
            }
            
            _progress.value = PdfGenerationProgress.Saving(0.5f)
            
            val file = saveDocument(pdfDocument, ReportType.JOURNAL_ENTRIES, config)
            pdfDocument.close()
            
            _progress.value = PdfGenerationProgress.Complete
            
            PdfGenerationResult.Success(
                file = file,
                pageCount = pageCount,
                generationTimeMs = System.currentTimeMillis() - startTime
            )
        } catch (e: Exception) {
            _progress.value = PdfGenerationProgress.Error(e.message ?: "Unknown error")
            PdfGenerationResult.Failure.RenderError(0, e)
        }
    }
    
    /**
     * Generate Analytics Summary Report (legacy support)
     */
    suspend fun generateAnalyticsSummary(
        totalIncome: Double,
        totalExpense: Double,
        netProfit: Double,
        activeProjects: Int,
        totalWorkers: Int,
        periodTitle: String
    ): PdfGenerationResult = withContext(Dispatchers.IO) {
        resetCancellation()
        val startTime = System.currentTimeMillis()
        
        try {
            _progress.value = PdfGenerationProgress.Initializing
            
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(
                PdfPageSettings.A4_WIDTH,
                PdfPageSettings.A4_HEIGHT,
                1
            ).create()
            
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            
            // Render header
            var currentY = pageRenderer.renderHeader(
                canvas = canvas,
                reportType = ReportType.ANALYTICS_SUMMARY,
                pageNumber = 1,
                isFirstPage = true
            )
            
            // Summary cards
            currentY = pageRenderer.renderSummaryCards(
                canvas = canvas,
                cards = listOf(
                    Triple("إجمالي الإيرادات", CurrencyFormatter.format(totalIncome), true),
                    Triple("إجمالي المصروفات", CurrencyFormatter.format(totalExpense), false),
                    Triple("صافي الربح", CurrencyFormatter.format(netProfit), netProfit >= 0)
                ),
                currentY = currentY
            )
            
            // Quick stats section
            currentY = pageRenderer.renderSectionHeader(canvas, "إحصائيات سريعة", currentY + 20f)
            
            // Stats info card
            currentY = pageRenderer.renderInfoCard(
                canvas = canvas,
                title = "معلومات عامة",
                items = listOf(
                    "الفترة" to periodTitle,
                    "المشاريع الجارية" to activeProjects.toString(),
                    "إجمالي العمال" to totalWorkers.toString(),
                    "نسبة الربح" to if (totalIncome > 0) "${((netProfit / totalIncome) * 100).toInt()}%" else "0%"
                ),
                currentY = currentY
            )
            
            // Net profit highlight
            currentY = pageRenderer.renderHighlightBox(
                canvas = canvas,
                title = "صافي الربح/الخسارة",
                value = CurrencyFormatter.format(netProfit),
                subtitle = if (netProfit >= 0) "ربح ✓" else "خسارة ✗",
                isPositive = netProfit >= 0,
                currentY = currentY + 20f
            )
            
            // Footer
            pageRenderer.renderFooter(canvas, 1, 1)
            
            pdfDocument.finishPage(page)
            
            _progress.value = PdfGenerationProgress.Saving(0.5f)
            
            val config = ReportConfig(ReportType.ANALYTICS_SUMMARY)
            val file = saveDocument(pdfDocument, ReportType.ANALYTICS_SUMMARY, config)
            pdfDocument.close()
            
            _progress.value = PdfGenerationProgress.Complete
            
            PdfGenerationResult.Success(
                file = file,
                pageCount = 1,
                generationTimeMs = System.currentTimeMillis() - startTime
            )
        } catch (e: Exception) {
            _progress.value = PdfGenerationProgress.Error(e.message ?: "Unknown error")
            PdfGenerationResult.Failure.RenderError(0, e)
        }
    }
    
    /**
     * Generate Bank Statement Report
     * يولد تقرير كشف الحساب البنكي
     */
    suspend fun generateBankStatement(
        data: BankStatementData,
        config: ReportConfig
    ): PdfGenerationResult = withContext(Dispatchers.IO) {
        resetCancellation()
        val startTime = System.currentTimeMillis()
        
        try {
            _progress.value = PdfGenerationProgress.Initializing
            
            if (data.transactions.isEmpty() && data.openingBalance.amount == 0.0) {
                return@withContext PdfGenerationResult.Failure.NoData
            }
            
            _progress.value = PdfGenerationProgress.LoadingData(0.5f)
            
            val template = BankStatementTemplate()
            val pdfDocument = PdfDocument()
            
            _progress.value = PdfGenerationProgress.LoadingData(1f)
            
            val pageCount = template.render(pdfDocument, data, config)
            
            if (isCancelled) {
                pdfDocument.close()
                return@withContext PdfGenerationResult.Failure.Cancelled
            }
            
            _progress.value = PdfGenerationProgress.Saving(0.5f)
            
            val file = saveDocument(pdfDocument, ReportType.BANK_STATEMENT, config)
            pdfDocument.close()
            
            _progress.value = PdfGenerationProgress.Complete
            
            PdfGenerationResult.Success(
                file = file,
                pageCount = pageCount,
                generationTimeMs = System.currentTimeMillis() - startTime
            )
        } catch (e: Exception) {
            _progress.value = PdfGenerationProgress.Error(e.message ?: "Unknown error")
            PdfGenerationResult.Failure.RenderError(0, e)
        }
    }
    
    // ══════════════════════════════════════════════════════════════════════
    // FILE OPERATIONS
    // ══════════════════════════════════════════════════════════════════════
    
    /**
     * Save PDF document to file
     */
    private fun saveDocument(
        document: PdfDocument,
        reportType: ReportType,
        config: ReportConfig
    ): File {
        val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            ?: context.filesDir
        val reportsDir = File(outputDir, "reports").apply { mkdirs() }
        
        val fileName = generateFileName(reportType, config)
        val file = File(reportsDir, fileName)
        
        FileOutputStream(file).use { output ->
            document.writeTo(output)
        }
        
        _progress.value = PdfGenerationProgress.Saving(1f)
        
        return file
    }
    
    /**
     * Generate file name based on report type and config
     */
    private fun generateFileName(reportType: ReportType, config: ReportConfig): String {
        val typePrefix = when (reportType) {
            ReportType.BANK_STATEMENT -> "bank_statement"
            ReportType.ACCOUNT_STATEMENT -> "account_statement"
            ReportType.PROFIT_LOSS -> "profit_loss"
            ReportType.TRANSACTION_LEDGER -> "transactions"
            ReportType.PAYROLL_SUMMARY -> "payroll"
            ReportType.WORKER_REPORT -> "worker"
            ReportType.PROJECT_REPORT -> "project"
            ReportType.JOURNAL_ENTRIES -> "journal"
            ReportType.ANALYTICS_SUMMARY -> "analytics"
        }
        
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        
        // Add ID if specific entity
        val suffix = when {
            config.accountId != null -> "_acc${config.accountId}"
            config.workerId != null -> "_wkr${config.workerId}"
            config.projectId != null -> "_prj${config.projectId}"
            else -> ""
        }
        
        return "${typePrefix}${suffix}_$timestamp.pdf"
    }
    
    /**
     * Share PDF file via system share intent
     */
    fun sharePdf(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            context.startActivity(
                Intent.createChooser(shareIntent, "مشاركة التقرير")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Open PDF file with default viewer
     */
    fun openPdf(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            context.startActivity(viewIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
