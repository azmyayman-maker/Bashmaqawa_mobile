package com.bashmaqawa.presentation.screens.home

import androidx.compose.runtime.Immutable

/**
 * Home Screen State Management
 * إدارة حالة الصفحة الرئيسية
 * 
 * Uses sub-states for efficient recomposition - each section can update
 * independently without triggering full screen recomposition.
 */

/**
 * Main HomeUiState combining all sub-states
 * الحالة الرئيسية للصفحة الرئيسية
 */
@Immutable
data class HomeUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val header: HeaderState = HeaderState(),
    val financial: FinancialState = FinancialState(),
    val projects: ProjectsState = ProjectsState(),
    val attendance: AttendanceState = AttendanceState(),
    val transactions: TransactionsState = TransactionsState()
)

/**
 * Header Section State
 * حالة قسم الرأس
 */
@Immutable
data class HeaderState(
    val userName: String = "",
    val greeting: String = "مرحباً",
    val currentDate: String = "",
    val notificationCount: Int = 0
)

/**
 * Financial Overview State
 * حالة النظرة المالية العامة
 */
@Immutable
data class FinancialState(
    val netWorth: Double = 0.0,
    val netWorthTrend: Double = 0.0,
    val totalAssets: Double = 0.0,
    val assetsTrend: Double = 0.0,
    val totalLiabilities: Double = 0.0,
    val liabilitiesTrend: Double = 0.0,
    val cashBalance: Double = 0.0,
    val bankBalance: Double = 0.0,
    val walletBalance: Double = 0.0,
    val todayIncome: Double = 0.0,
    val todayExpenses: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val monthlyExpenses: Double = 0.0,
    val cashFlowData: List<CashFlowPoint> = emptyList()
)

/**
 * Cash Flow Data Point for 7-day chart
 * نقطة بيانات التدفق النقدي للرسم البياني
 */
@Immutable
data class CashFlowPoint(
    val dayName: String,         // اسم اليوم (السبت، الأحد...)
    val date: String,            // التاريخ yyyy-MM-dd
    val income: Double,          // الإيرادات
    val expense: Double          // المصروفات
) {
    val netFlow: Double get() = income - expense
}

/**
 * Active Projects State
 * حالة المشاريع النشطة
 */
@Immutable
data class ProjectsState(
    val activeProjects: List<ProjectSummary> = emptyList(),
    val totalActiveCount: Int = 0
)

/**
 * Project Summary for Dashboard
 * ملخص المشروع للوحة التحكم
 */
@Immutable
data class ProjectSummary(
    val id: Int,
    val name: String,
    val clientName: String,
    val progress: Float,           // 0f to 1f
    val workerCount: Int,
    val lastActivityTime: String,  // Relative time (منذ ساعتين)
    val budget: Double,
    val spent: Double
)

/**
 * Today's Attendance State
 * حالة حضور اليوم
 */
@Immutable
data class AttendanceState(
    val presentCount: Int = 0,
    val absentCount: Int = 0,
    val lateCount: Int = 0,
    val notRecordedCount: Int = 0,
    val totalWorkers: Int = 0
) {
    val recordedCount: Int get() = presentCount + absentCount + lateCount
    val attendancePercentage: Float get() = 
        if (totalWorkers > 0) presentCount.toFloat() / totalWorkers else 0f
}

/**
 * Recent Transactions State
 * حالة المعاملات الأخيرة
 */
@Immutable
data class TransactionsState(
    val recentTransactions: List<TransactionSummary> = emptyList()
)

/**
 * Transaction Summary for Dashboard
 * ملخص المعاملة للوحة التحكم
 */
@Immutable
data class TransactionSummary(
    val id: Int,
    val type: TransactionDisplayType,
    val amount: Double,
    val description: String,
    val projectName: String?,
    val workerName: String?,
    val relativeTime: String,      // منذ 5 دقائق
    val date: String
)

/**
 * Transaction Display Type for UI
 * نوع عرض المعاملة للواجهة
 */
enum class TransactionDisplayType {
    INCOME,      // إيراد - Green
    EXPENSE,     // مصروف - Red
    TRANSFER     // تحويل - Blue
}

/**
 * Quick Action Item
 * عنصر إجراء سريع
 */
@Immutable
data class QuickAction(
    val id: String,
    val titleResId: Int,
    val iconResId: Int,
    val route: String,
    val color: androidx.compose.ui.graphics.Color
)
