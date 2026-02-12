package com.bashmaqawa.pdf.models

import com.bashmaqawa.core.DateRange

/**
 * Report Type Enum
 * أنواع التقارير
 * 
 * Defines all supported PDF report types in the application.
 */
enum class ReportType(
    val titleArabic: String,
    val titleEnglish: String,
    val description: String,
    val category: ReportCategory
) {
    BANK_STATEMENT(
        titleArabic = "كشف حساب بنكي",
        titleEnglish = "Bank Statement",
        description = "كشف حساب بنكي تفصيلي مع تحليلات",
        category = ReportCategory.FINANCIAL
    ),
    
    ACCOUNT_STATEMENT(
        titleArabic = "كشف حساب",
        titleEnglish = "Account Statement",
        description = "تفاصيل حركات حساب معين",
        category = ReportCategory.FINANCIAL
    ),
    
    PROFIT_LOSS(
        titleArabic = "تقرير الربح والخسارة",
        titleEnglish = "Profit & Loss Report",
        description = "ملخص الإيرادات والمصروفات",
        category = ReportCategory.FINANCIAL
    ),
    
    TRANSACTION_LEDGER(
        titleArabic = "سجل المعاملات",
        titleEnglish = "Transaction Ledger",
        description = "قائمة تفصيلية بجميع المعاملات",
        category = ReportCategory.FINANCIAL
    ),
    
    PAYROLL_SUMMARY(
        titleArabic = "تقرير الرواتب",
        titleEnglish = "Payroll Summary",
        description = "ملخص أجور العمال",
        category = ReportCategory.OPERATIONAL
    ),
    
    WORKER_REPORT(
        titleArabic = "تقرير العامل",
        titleEnglish = "Worker Report",
        description = "تفاصيل عامل معين",
        category = ReportCategory.OPERATIONAL
    ),
    
    PROJECT_REPORT(
        titleArabic = "تقرير المشروع",
        titleEnglish = "Project Report",
        description = "ملخص مالي للمشروع",
        category = ReportCategory.OPERATIONAL
    ),
    
    JOURNAL_ENTRIES(
        titleArabic = "قيود اليومية",
        titleEnglish = "Journal Entries",
        description = "سجل القيود المحاسبية",
        category = ReportCategory.AUDIT
    ),
    
    ANALYTICS_SUMMARY(
        titleArabic = "ملخص التحليلات",
        titleEnglish = "Analytics Summary",
        description = "نظرة عامة على الأداء المالي",
        category = ReportCategory.FINANCIAL
    );
    
    /**
     * Get localized title based on preference
     */
    fun getTitle(useArabic: Boolean = true): String {
        return if (useArabic) titleArabic else titleEnglish
    }
}

/**
 * Report Category
 * فئة التقرير
 */
enum class ReportCategory(
    val nameArabic: String,
    val nameEnglish: String
) {
    FINANCIAL("التقارير المالية", "Financial Reports"),
    OPERATIONAL("التقارير التشغيلية", "Operational Reports"),
    AUDIT("تقارير المراجعة", "Audit Reports");
    
    fun getName(useArabic: Boolean = true): String {
        return if (useArabic) nameArabic else nameEnglish
    }
}

/**
 * Report Configuration
 * إعدادات التقرير
 */
data class ReportConfig(
    val reportType: ReportType,
    val dateRange: DateRange? = null,
    val accountId: Int? = null,
    val workerId: Int? = null,
    val projectId: Int? = null,
    val includeDetails: Boolean = true,
    val includeCharts: Boolean = false,
    val pageOrientation: PageOrientation = PageOrientation.PORTRAIT,
    val showArabicOnly: Boolean = false
)

/**
 * Page orientation
 */
enum class PageOrientation {
    PORTRAIT,
    LANDSCAPE
}

/**
 * PDF Generation Progress
 * تقدم إنشاء PDF
 */
sealed class PdfGenerationProgress {
    data object Initializing : PdfGenerationProgress()
    data class LoadingData(val progress: Float) : PdfGenerationProgress()
    data class RenderingPage(val current: Int, val total: Int) : PdfGenerationProgress()
    data class Saving(val progress: Float) : PdfGenerationProgress()
    data object Complete : PdfGenerationProgress()
    data class Error(val message: String) : PdfGenerationProgress()
    
    fun getProgressPercent(): Float = when (this) {
        is Initializing -> 0f
        is LoadingData -> progress * 0.3f
        is RenderingPage -> 0.3f + (current.toFloat() / total) * 0.5f
        is Saving -> 0.8f + progress * 0.2f
        is Complete -> 1f
        is Error -> 0f
    }
    
    fun getStatusMessage(): String = when (this) {
        is Initializing -> "جاري التهيئة..."
        is LoadingData -> "جاري تحميل البيانات..."
        is RenderingPage -> "جاري إنشاء الصفحة $current من $total..."
        is Saving -> "جاري حفظ الملف..."
        is Complete -> "تم إنشاء التقرير بنجاح"
        is Error -> "خطأ: $message"
    }
}

/**
 * PDF Generation Result
 * نتيجة إنشاء PDF
 */
sealed class PdfGenerationResult {
    data class Success(
        val file: java.io.File,
        val pageCount: Int,
        val generationTimeMs: Long
    ) : PdfGenerationResult()
    
    sealed class Failure : PdfGenerationResult() {
        data object NoData : Failure()
        data object InsufficientStorage : Failure()
        data class RenderError(val page: Int, val cause: Throwable) : Failure()
        data class IOError(val cause: Throwable) : Failure()
        data object Cancelled : Failure()
        
        fun getMessage(): String = when (this) {
            is NoData -> "لا توجد بيانات لإنشاء التقرير"
            is InsufficientStorage -> "مساحة التخزين غير كافية"
            is RenderError -> "خطأ في إنشاء الصفحة $page"
            is IOError -> "خطأ في الكتابة: ${cause.message}"
            is Cancelled -> "تم إلغاء العملية"
        }
    }
}

/**
 * Report Data Models
 * نماذج بيانات التقارير
 */

/** Category total for P&L breakdown */
data class CategoryTotal(
    val category: String,
    val amount: Double,
    val transactionCount: Int = 0
)

/** Account statement data */
data class AccountStatementData(
    val accountId: Int,
    val accountName: String,
    val accountCode: String?,
    val accountType: String?,
    val bankName: String?,
    val accountNumber: String?,
    val openingBalance: Double,
    val closingBalance: Double,
    val totalDebits: Double,
    val totalCredits: Double,
    val transactions: List<TransactionRow>
)

/** Transaction row for reports */
data class TransactionRow(
    val id: Int,
    val date: String,
    val description: String,
    val category: String?,
    val referenceNumber: String?,
    val debit: Double,
    val credit: Double,
    val balance: Double,
    val type: String,
    val state: String
)

/** Profit/Loss report data */
data class ProfitLossData(
    val periodStart: String,
    val periodEnd: String,
    val incomeCategories: List<CategoryTotal>,
    val expenseCategories: List<CategoryTotal>,
    val totalIncome: Double,
    val totalExpenses: Double,
    val netProfit: Double
)

/** Payroll report data */
data class PayrollReportData(
    val periodStart: String,
    val periodEnd: String,
    val entries: List<PayrollRow>,
    val totalGrossWages: Double,
    val totalDeductions: Double,
    val totalAdvances: Double,
    val totalNetPay: Double
)

/** Payroll row */
data class PayrollRow(
    val workerId: Int,
    val workerName: String,
    val category: String?,
    val daysWorked: Double,
    val dailyRate: Double,
    val grossWage: Double,
    val deductions: Double,
    val advances: Double,
    val netPay: Double,
    val status: String
)

/** Project report data */
data class ProjectReportData(
    val projectId: Int,
    val projectName: String,
    val clientName: String?,
    val location: String?,
    val startDate: String?,
    val endDate: String?,
    val status: String,
    val totalIncome: Double,
    val totalExpenses: Double,
    val netProfit: Double,
    val expenseBreakdown: List<CategoryTotal>,
    val incomeBreakdown: List<CategoryTotal>
)

/** Journal entry row */
data class JournalEntryRow(
    val id: Int,
    val date: String,
    val description: String,
    val debitAccountName: String,
    val creditAccountName: String,
    val amount: Double,
    val referenceType: String?,
    val isReversing: Boolean
)
