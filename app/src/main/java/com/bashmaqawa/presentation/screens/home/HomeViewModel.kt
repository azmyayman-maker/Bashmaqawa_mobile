package com.bashmaqawa.presentation.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bashmaqawa.data.database.entities.AttendanceStatus
import com.bashmaqawa.data.database.entities.TransactionType
import com.bashmaqawa.data.repository.AttendanceRepository
import com.bashmaqawa.data.repository.FinancialRepository
import com.bashmaqawa.data.repository.ProjectRepository
import com.bashmaqawa.data.repository.WorkerRepository
import com.bashmaqawa.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import javax.inject.Inject

/**
 * Home Dashboard ViewModel - Optimized with Sub-States
 * فيو موديل لوحة التحكم الرئيسية - محسّن بالحالات الفرعية
 * 
 * Uses parallel data fetching and sub-states for efficient recomposition.
 * Each section updates independently without triggering full recomposition.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val workerRepository: WorkerRepository,
    private val attendanceRepository: AttendanceRepository,
    private val financialRepository: FinancialRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val arabicLocale = Locale("ar", "EG")
    
    // أسماء الأيام بالعربية - Arabic day names
    private val arabicDayNames = mapOf(
        "SATURDAY" to "السبت",
        "SUNDAY" to "الأحد",
        "MONDAY" to "الاثنين",
        "TUESDAY" to "الثلاثاء",
        "WEDNESDAY" to "الأربعاء",
        "THURSDAY" to "الخميس",
        "FRIDAY" to "الجمعة"
    )
    
    init {
        initializeHeader()
        loadDashboardData()
    }
    
    /**
     * Initialize header with greeting and date
     * تهيئة الرأس مع التحية والتاريخ
     */
    private fun initializeHeader() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val hour = LocalTime.now().hour
            
            val greeting = when {
                hour < 12 -> "صباح الخير"
                else -> "مساء الخير"
            }
            
            val dateFormatted = today.format(
                DateTimeFormatter.ofPattern("EEEE، d MMMM yyyy", arabicLocale)
            )
            
            // Try to get the first user as the current user
            val users = authRepository.getAllUsers().firstOrNull()
            val userName = users?.firstOrNull()?.let { it.displayName ?: it.username } ?: "المستخدم"
            
            _uiState.value = _uiState.value.copy(
                header = HeaderState(
                    userName = userName,
                    greeting = greeting,
                    currentDate = dateFormatted,
                    notificationCount = 0
                )
            )
        }
    }
    
    /**
     * Load all dashboard data in parallel
     * تحميل جميع بيانات لوحة التحكم بشكل متوازي
     */
    fun loadDashboardData() {
        viewModelScope.launch {
            if (!_uiState.value.isRefreshing) {
                _uiState.value = _uiState.value.copy(isLoading = true)
            }
            
            try {
                // تحميل متوازي لجميع البيانات
                coroutineScope {
                    val financialDeferred = async { loadFinancialData() }
                    val projectsDeferred = async { loadProjectsData() }
                    val attendanceDeferred = async { loadAttendanceData() }
                    val transactionsDeferred = async { loadRecentTransactions() }
                    
                    // انتظار جميع النتائج
                    financialDeferred.await()
                    projectsDeferred.await()
                    attendanceDeferred.await()
                    transactionsDeferred.await()
                }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = null
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error loading dashboard data", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = e.localizedMessage ?: "حدث خطأ غير متوقع"
                )
            }
        }
    }
    
    /**
     * Load financial data (assets, liabilities, balances, cash flow)
     * تحميل البيانات المالية
     */
    private suspend fun loadFinancialData() {
        val today = LocalDate.now()
        val todayStr = today.format(dateFormatter)
        val monthStart = today.withDayOfMonth(1).format(dateFormatter)
        val monthEnd = today.withDayOfMonth(today.lengthOfMonth()).format(dateFormatter)
        
        // حسابات موازية للبيانات المالية
        coroutineScope {
            val assetsDeferred = async { financialRepository.getTotalAssets() }
            val liabilitiesDeferred = async { financialRepository.getTotalLiabilities() }
            val cashDeferred = async { financialRepository.getTotalCashBalance() }
            val bankDeferred = async { financialRepository.getTotalBankBalance() }
            val walletDeferred = async { financialRepository.getTotalWalletBalance() }
            val todayIncomeDeferred = async { financialRepository.getTodayIncome(todayStr) }
            val todayExpenseDeferred = async { financialRepository.getTodayExpenses(todayStr) }
            val monthlyIncomeDeferred = async { 
                financialRepository.getTotalIncomeInRange(monthStart, monthEnd) 
            }
            val monthlyExpenseDeferred = async { 
                financialRepository.getTotalExpenseInRange(monthStart, monthEnd) 
            }
            val cashFlowDeferred = async { loadCashFlowData(today) }
            
            val assets = assetsDeferred.await()
            val liabilities = liabilitiesDeferred.await()
            val cash = cashDeferred.await()
            val bank = bankDeferred.await()
            val wallet = walletDeferred.await()
            val todayIncome = todayIncomeDeferred.await()
            val todayExpense = todayExpenseDeferred.await()
            val monthlyIncome = monthlyIncomeDeferred.await()
            val monthlyExpense = monthlyExpenseDeferred.await()
            val cashFlowData = cashFlowDeferred.await()
            
            val netWorth = assets - liabilities
            
            // TODO: حساب الاتجاه يحتاج مقارنة بالشهر السابق
            val netWorthTrend = 0.0 // Placeholder
            
            _uiState.value = _uiState.value.copy(
                financial = FinancialState(
                    netWorth = netWorth,
                    netWorthTrend = netWorthTrend,
                    totalAssets = assets,
                    assetsTrend = 0.0,
                    totalLiabilities = liabilities,
                    liabilitiesTrend = 0.0,
                    cashBalance = cash,
                    bankBalance = bank,
                    walletBalance = wallet,
                    todayIncome = todayIncome,
                    todayExpenses = todayExpense,
                    monthlyIncome = monthlyIncome,
                    monthlyExpenses = monthlyExpense,
                    cashFlowData = cashFlowData
                )
            )
        }
    }
    
    /**
     * Load 7-day cash flow data for chart
     * تحميل بيانات التدفق النقدي لآخر 7 أيام
     */
    private suspend fun loadCashFlowData(today: LocalDate): List<CashFlowPoint> {
        return (6 downTo 0).map { daysAgo ->
            val date = today.minusDays(daysAgo.toLong())
            val dateStr = date.format(dateFormatter)
            val dayName = arabicDayNames[date.dayOfWeek.name] ?: date.dayOfWeek.name
            
            val income = financialRepository.getTodayIncome(dateStr)
            val expense = financialRepository.getTodayExpenses(dateStr)
            
            CashFlowPoint(
                dayName = dayName,
                date = dateStr,
                income = income,
                expense = expense
            )
        }
    }
    
    /**
     * Load active projects summary
     * تحميل ملخص المشاريع النشطة
     */
    private suspend fun loadProjectsData() {
        try {
            val activeProjects = projectRepository.getActiveProjects().firstOrNull() ?: emptyList()
            val projectCount = activeProjects.size
            
            // جلب سجلات الحضور لليوم لحساب عدد العمال في كل مشروع
            val todayStr = LocalDate.now().format(dateFormatter)
            val todayAttendance = attendanceRepository.getAttendanceByDate(todayStr).firstOrNull() ?: emptyList()
            
            val summaries = activeProjects.take(5).map { project ->
                val budget = projectRepository.getTotalBudgetByProjectId(project.id)
                val spent = projectRepository.getTotalSpentByProjectId(project.id)
                val progress = if (budget > 0) (spent / budget).toFloat().coerceIn(0f, 1f) else 0f
                
                // حساب عدد العمال النشطين في المشروع اليوم
                val workerCount = todayAttendance.count { 
                    it.projectId == project.id && 
                    (it.status == AttendanceStatus.PRESENT || it.status == AttendanceStatus.HALF_DAY || it.status == AttendanceStatus.OVERTIME)
                }
                
                // حساب آخر نشاط
                val lastActivity = calculateRelativeTime(project.updatedAt ?: "")
                
                ProjectSummary(
                    id = project.id,
                    name = project.name,
                    clientName = project.clientName ?: "",
                    progress = progress,
                    workerCount = workerCount,
                    lastActivityTime = lastActivity,
                    budget = budget,
                    spent = spent
                )
            }
            
            _uiState.value = _uiState.value.copy(
                projects = ProjectsState(
                    activeProjects = summaries,
                    totalActiveCount = projectCount
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error loading projects data", e)
            _uiState.value = _uiState.value.copy(
                projects = ProjectsState(activeProjects = emptyList(), totalActiveCount = 0)
            )
        }
    }
    
    /**
     * Load today's attendance statistics
     * تحميل إحصائيات الحضور لليوم
     */
    private suspend fun loadAttendanceData() {
        try {
            val todayStr = LocalDate.now().format(dateFormatter)
            val totalWorkers = workerRepository.getActiveWorkerCount()
            
            // جلب سجلات الحضور لليوم
            val attendanceRecords = attendanceRepository.getAttendanceByDate(todayStr).firstOrNull() ?: emptyList()
            
            var presentCount = 0
            var absentCount = 0
            var lateCount = 0
            
            attendanceRecords.forEach { record ->
                when (record.status) {
                    AttendanceStatus.PRESENT -> presentCount++
                    AttendanceStatus.ABSENT -> absentCount++
                    AttendanceStatus.HALF_DAY -> lateCount++ // نصف يوم يعتبر متأخر
                    AttendanceStatus.OVERTIME -> presentCount++ // إضافي يعتبر حاضر
                }
            }
            
            val recordedWorkers = attendanceRecords.map { it.workerId }.distinct().size
            val notRecordedCount = (totalWorkers - recordedWorkers).coerceAtLeast(0)
            
            _uiState.value = _uiState.value.copy(
                attendance = AttendanceState(
                    presentCount = presentCount,
                    absentCount = absentCount,
                    lateCount = lateCount,
                    notRecordedCount = notRecordedCount,
                    totalWorkers = totalWorkers
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error loading attendance data", e)
            _uiState.value = _uiState.value.copy(
                attendance = AttendanceState()
            )
        }
    }
    
    /**
     * Load recent transactions (last 5)
     * تحميل آخر 5 معاملات
     */
    private suspend fun loadRecentTransactions() {
        try {
            val today = LocalDate.now()
            val startDate = today.minusDays(30).format(dateFormatter)
            val endDate = today.format(dateFormatter)
            
            val transactionsFlow = financialRepository
                .getTransactionsInDateRange(startDate, endDate)
            
            val transactions = transactionsFlow.firstOrNull() ?: emptyList()
            
            val validTransactions = transactions
                .filter { it.type != null && it.amount != null }
                .take(5)
            
            val summaries = validTransactions.mapNotNull { transaction ->
                try {
                    val transactionType = transaction.type ?: return@mapNotNull null
                    val displayType = when (transactionType) {
                        TransactionType.INCOME -> TransactionDisplayType.INCOME
                        TransactionType.EXPENSE -> TransactionDisplayType.EXPENSE
                        TransactionType.TRANSFER -> TransactionDisplayType.TRANSFER
                    }
                    
                    // جلب اسم المشروع والعامل إذا وجدوا
                    val projectName = transaction.projectId?.let { projectRepository.getProjectById(it)?.name }
                    val workerName = transaction.workerId?.let { workerRepository.getWorkerById(it)?.name }
                    
                    TransactionSummary(
                        id = transaction.id,
                        type = displayType,
                        amount = transaction.amount ?: 0.0,
                        description = transaction.description ?: "",
                        projectName = projectName,
                        workerName = workerName,
                        relativeTime = calculateRelativeTime(transaction.date ?: ""),
                        date = transaction.date ?: ""
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Error mapping transaction ${transaction.id}", e)
                    null
                }
            }
            
            _uiState.value = _uiState.value.copy(
                transactions = TransactionsState(recentTransactions = summaries)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error loading recent transactions", e)
            // Don't crash - just leave transactions empty
            _uiState.value = _uiState.value.copy(
                transactions = TransactionsState(recentTransactions = emptyList())
            )
        }
    }
    
    /**
     * Calculate relative time string
     * حساب الوقت النسبي
     */
    private fun calculateRelativeTime(dateTimeStr: String): String {
        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val dateTime = LocalDateTime.parse(dateTimeStr, formatter)
            val now = LocalDateTime.now()
            
            val minutes = ChronoUnit.MINUTES.between(dateTime, now)
            val hours = ChronoUnit.HOURS.between(dateTime, now)
            val days = ChronoUnit.DAYS.between(dateTime, now)
            
            when {
                minutes < 1 -> "الآن"
                minutes < 60 -> "منذ $minutes دقيقة"
                hours < 24 -> "منذ $hours ساعة"
                days == 1L -> "أمس"
                days < 7 -> "منذ $days أيام"
                else -> "منذ أكثر من أسبوع"
            }
        } catch (e: Exception) {
            // Fallback for date-only format
            try {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val date = LocalDate.parse(dateTimeStr, formatter)
                val today = LocalDate.now()
                val days = ChronoUnit.DAYS.between(date, today)
                
                when {
                    days == 0L -> "اليوم"
                    days == 1L -> "أمس"
                    days < 7 -> "منذ $days أيام"
                    else -> "منذ أكثر من أسبوع"
                }
            } catch (e2: Exception) {
                dateTimeStr
            }
        }
    }
    
    /**
     * Refresh all data
     * تحديث جميع البيانات
     */
    fun refresh() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        initializeHeader() // تحديث التحية في حال تغير الوقت
        loadDashboardData()
    }
    
    companion object {
        private const val TAG = "HomeViewModel"
    }
}
