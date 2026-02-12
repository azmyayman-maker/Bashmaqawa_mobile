package com.bashmaqawa.presentation.screens.financial

import com.bashmaqawa.data.database.dao.TransactionWithDetails
import com.bashmaqawa.data.database.entities.Account
import com.bashmaqawa.data.database.entities.JournalEntry
import com.bashmaqawa.data.database.entities.TransactionType

/**
 * Account Detail Screen UI State Classes
 * فئات حالة واجهة شاشة تفاصيل الحساب
 * 
 * Uses sub-state pattern to minimize recomposition
 */

// =====================================================
// MAIN UI STATE
// =====================================================

/**
 * Main UI State for Account Detail Screen
 * حالة الواجهة الرئيسية لشاشة تفاصيل الحساب
 */
data class AccountDetailUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    
    // Sub-states
    val accountState: AccountInfoState = AccountInfoState(),
    val analyticsState: AccountAnalyticsState = AccountAnalyticsState(),
    val transactionState: AccountTransactionState = AccountTransactionState(),
    val journalState: AccountJournalState = AccountJournalState(),
    val relatedState: AccountRelatedState = AccountRelatedState(),
    
    // UI Controls
    val selectedTab: AccountDetailTab = AccountDetailTab.TRANSACTIONS,
    val showEditSheet: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val showFabMenu: Boolean = false,
    
    // Messages
    val successMessage: String? = null
)

// =====================================================
// SUB-STATES
// =====================================================

/**
 * Account Information State
 * حالة معلومات الحساب
 */
data class AccountInfoState(
    val account: Account? = null,
    val balanceTrend: BalanceTrend = BalanceTrend.STABLE,
    val trendPercentage: Double = 0.0,
    val lastUpdated: String? = null,
    val pendingBalance: Double = 0.0,
    val availableBalance: Double = 0.0
)

/**
 * Account Analytics State
 * حالة تحليلات الحساب
 */
data class AccountAnalyticsState(
    val selectedTimeRange: AccountTimeRange = AccountTimeRange.MONTH,
    val chartData: List<ChartDataPoint> = emptyList(),
    val totalInflow: Double = 0.0,
    val totalOutflow: Double = 0.0,
    val netChange: Double = 0.0,
    val averageTransactionSize: Double = 0.0,
    val transactionCount: Int = 0,
    val largestTransaction: Double = 0.0,
    val isLoadingChart: Boolean = false
)

/**
 * Account Transaction State
 * حالة معاملات الحساب
 */
data class AccountTransactionState(
    val transactions: List<TransactionWithDetails> = emptyList(),
    val groupedTransactions: Map<String, List<TransactionWithDetails>> = emptyMap(),
    val filter: AccountTransactionFilter = AccountTransactionFilter.ALL,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val expandedTransactionId: Int? = null
)

/**
 * Account Journal State
 * حالة قيود الحساب
 */
data class AccountJournalState(
    val journalEntries: List<JournalEntryWithDetails> = emptyList(),
    val isLoading: Boolean = false,
    val expandedEntryId: Int? = null
)

/**
 * Account Related Entities State
 * حالة الكيانات المرتبطة بالحساب
 */
data class AccountRelatedState(
    val linkedWorkers: List<LinkedWorkerInfo> = emptyList(),
    val linkedProjects: List<LinkedProjectInfo> = emptyList(),
    val isLoading: Boolean = false
)

// =====================================================
// DATA CLASSES
// =====================================================

/**
 * Chart Data Point for Balance Trend
 * نقطة بيانات المخطط لاتجاه الرصيد
 */
data class ChartDataPoint(
    val date: String,
    val balance: Double,
    val label: String = ""
)

/**
 * Journal Entry with Account Names
 * قيد اليومية مع أسماء الحسابات
 */
data class JournalEntryWithDetails(
    val entry: JournalEntry,
    val debitAccountName: String,
    val creditAccountName: String
)

/**
 * Linked Worker Information
 * معلومات العامل المرتبط
 */
data class LinkedWorkerInfo(
    val workerId: Int,
    val workerName: String,
    val outstandingAdvance: Double,
    val lastTransactionDate: String?
)

/**
 * Linked Project Information
 * معلومات المشروع المرتبط
 */
data class LinkedProjectInfo(
    val projectId: Int,
    val projectName: String,
    val totalTransactions: Double,
    val transactionCount: Int
)

// =====================================================
// ENUMS
// =====================================================

/**
 * Account Detail Tabs
 * علامات تبويب تفاصيل الحساب
 */
enum class AccountDetailTab {
    TRANSACTIONS,  // المعاملات
    JOURNAL,       // قيود اليومية
    ANALYTICS,     // التحليلات
    RELATED        // المرتبطة
}

/**
 * Balance Trend Direction
 * اتجاه الرصيد
 */
enum class BalanceTrend {
    INCREASING,    // ارتفاع ↑
    DECREASING,    // انخفاض ↓
    STABLE         // مستقر →
}

/**
 * Time Range for Analytics
 * النطاق الزمني للتحليلات
 */
enum class AccountTimeRange(val days: Int, val labelAr: String, val labelEn: String) {
    WEEK(7, "أسبوع", "7D"),
    MONTH(30, "شهر", "30D"),
    QUARTER(90, "ربع سنة", "90D"),
    YEAR(365, "سنة", "1Y"),
    ALL(-1, "الكل", "ALL")
}

/**
 * Transaction Filter for Account Detail
 * فلتر المعاملات لتفاصيل الحساب
 */
enum class AccountTransactionFilter(val labelAr: String, val labelEn: String) {
    ALL("الكل", "All"),
    INCOME("إيرادات", "Income"),
    EXPENSE("مصروفات", "Expense"),
    TRANSFER("تحويلات", "Transfer")
}

/**
 * Transaction Action
 * إجراء المعاملة
 */
enum class TransactionAction {
    VIEW_DETAILS,
    VIEW_JOURNAL,
    REVERSE,
    EDIT
}

// =====================================================
// EXTENSION FUNCTIONS
// =====================================================

/**
 * Convert TransactionType to AccountTransactionFilter
 */
fun TransactionType?.toFilter(): AccountTransactionFilter = when (this) {
    TransactionType.INCOME -> AccountTransactionFilter.INCOME
    TransactionType.EXPENSE -> AccountTransactionFilter.EXPENSE
    TransactionType.TRANSFER -> AccountTransactionFilter.TRANSFER
    null -> AccountTransactionFilter.ALL
}

/**
 * Check if filter matches transaction type
 */
fun AccountTransactionFilter.matches(type: TransactionType?): Boolean = when (this) {
    AccountTransactionFilter.ALL -> true
    AccountTransactionFilter.INCOME -> type == TransactionType.INCOME
    AccountTransactionFilter.EXPENSE -> type == TransactionType.EXPENSE
    AccountTransactionFilter.TRANSFER -> type == TransactionType.TRANSFER
}
