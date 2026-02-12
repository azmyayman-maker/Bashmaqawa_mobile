package com.bashmaqawa.pdf.models

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Bank Statement Data Models
 * نماذج بيانات كشف الحساب البنكي
 * 
 * Following ISO 20022 banking standards for comprehensive statement generation.
 */

// ══════════════════════════════════════════════════════════════════════
// ENUMS
// ══════════════════════════════════════════════════════════════════════

/**
 * Balance Type Enum
 * نوع الرصيد
 */
enum class BalanceType(val arabicName: String, val englishName: String) {
    OPENING("رصيد افتتاحي", "Opening Balance"),
    CLOSING("رصيد ختامي", "Closing Balance"),
    AVAILABLE("رصيد متاح", "Available Balance"),
    PENDING("رصيد معلق", "Pending Balance")
}

/**
 * Credit/Debit Indicator
 * مؤشر الدائن/المدين
 */
enum class CreditDebitIndicator(val arabicName: String, val englishName: String) {
    CREDIT("دائن", "Credit"),
    DEBIT("مدين", "Debit")
}

/**
 * Transaction Status
 * حالة المعاملة
 */
enum class StatementTransactionStatus(val arabicName: String, val englishName: String) {
    PENDING("معلقة", "Pending"),
    CLEARED("مقاصة", "Cleared"),
    VOID("ملغاة", "Void"),
    REVERSED("معكوسة", "Reversed")
}

// ══════════════════════════════════════════════════════════════════════
// DATA CLASSES
// ══════════════════════════════════════════════════════════════════════

/**
 * Balance Information
 * معلومات الرصيد
 */
data class BalanceInfo(
    val amount: Double,
    val type: BalanceType,
    val date: LocalDate,
    val creditDebitIndicator: CreditDebitIndicator
) {
    /**
     * Get formatted amount with sign
     */
    val signedAmount: Double
        get() = if (creditDebitIndicator == CreditDebitIndicator.DEBIT) -amount else amount
}

/**
 * Statement Transaction
 * معاملة الكشف
 * 
 * Enhanced transaction data for bank statement display.
 */
data class StatementTransaction(
    val id: Int,
    val sequenceNumber: Int,                   // Transaction sequence in statement
    val valueDate: LocalDate,                  // Value date (when funds available)
    val bookingDate: LocalDate,                // Booking date (when recorded)
    val transactionType: String,               // INCOME, EXPENSE, TRANSFER
    val description: String,
    val narrativeText: String? = null,         // Additional details
    
    // Amount Information
    val amount: Double,
    val creditDebitIndicator: CreditDebitIndicator,
    val runningBalance: Double,
    
    // References
    val referenceNumber: String? = null,
    val checkNumber: String? = null,
    val counterpartyName: String? = null,      // Name of other party in transfer
    val counterpartyAccount: String? = null,
    
    // Categorization
    val category: String? = null,
    val subCategory: String? = null,
    
    // Status
    val status: StatementTransactionStatus = StatementTransactionStatus.CLEARED,
    val isReconciled: Boolean = false
) {
    /**
     * Get debit amount (positive if this is a debit transaction)
     */
    val debitAmount: Double
        get() = if (creditDebitIndicator == CreditDebitIndicator.DEBIT) amount else 0.0
    
    /**
     * Get credit amount (positive if this is a credit transaction)
     */
    val creditAmount: Double
        get() = if (creditDebitIndicator == CreditDebitIndicator.CREDIT) amount else 0.0
}

/**
 * Transaction Summary
 * ملخص المعاملة
 */
data class TransactionSummary(
    val description: String,
    val amount: Double,
    val date: LocalDate
)

/**
 * Category Analysis
 * تحليل الفئة
 */
data class CategoryAnalysis(
    val category: String,
    val totalAmount: Double,
    val transactionCount: Int,
    val percentage: Float
)

/**
 * Daily Balance
 * الرصيد اليومي
 */
data class DailyBalance(
    val date: LocalDate,
    val closingBalance: Double
)

/**
 * Statement Analytics
 * تحليلات الكشف
 */
data class StatementAnalytics(
    val averageDailyBalance: Double,
    val highestBalance: Double,
    val lowestBalance: Double,
    val largestDebit: TransactionSummary? = null,
    val largestCredit: TransactionSummary? = null,
    val categoryBreakdown: List<CategoryAnalysis> = emptyList(),
    val dailyBalanceTrend: List<DailyBalance> = emptyList()
)

/**
 * Company Information
 * معلومات الشركة
 */
data class CompanyInfo(
    val name: String,
    val logoPath: String? = null,
    val address: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val website: String? = null,
    val taxId: String? = null
)

/**
 * Comprehensive Bank Statement Data
 * بيانات كشف الحساب البنكي الشامل
 * 
 * Main data container for bank statement PDF generation.
 * Follows ISO 20022 banking standards.
 */
data class BankStatementData(
    // Statement Identification
    val statementId: String,                    // Unique statement identifier
    val sequenceNumber: Int = 1,                // Statement sequence in period
    
    // Account Details
    val accountId: Int,
    val accountName: String,
    val accountNumber: String? = null,
    val iban: String? = null,                   // International Bank Account Number
    val bankName: String? = null,
    val branchName: String? = null,
    val branchCode: String? = null,
    val accountType: String,
    val currency: String = "EGP",               // ISO 4217 currency code
    
    // Statement Period
    val periodStart: LocalDate,
    val periodEnd: LocalDate,
    val generationDate: LocalDateTime = LocalDateTime.now(),
    
    // Balance Information
    val openingBalance: BalanceInfo,
    val closingBalance: BalanceInfo,
    val availableBalance: BalanceInfo? = null,
    
    // Transaction Summary
    val totalDebits: Double,
    val totalCredits: Double,
    val transactionCount: Int,
    
    // Transaction Detail List
    val transactions: List<StatementTransaction>,
    
    // Analytics & Insights (Optional)
    val analytics: StatementAnalytics? = null,
    
    // Company Branding (Optional)
    val companyInfo: CompanyInfo? = null
) {
    /**
     * Net change during the period
     */
    val netChange: Double
        get() = totalCredits - totalDebits
    
    /**
     * Check if statement has any transactions
     */
    val hasTransactions: Boolean
        get() = transactions.isNotEmpty()
    
    /**
     * Get period as formatted string
     */
    fun getPeriodString(arabicFormat: Boolean = true): String {
        val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return if (arabicFormat) {
            "${periodEnd.format(formatter)} - ${periodStart.format(formatter)}"
        } else {
            "${periodStart.format(formatter)} - ${periodEnd.format(formatter)}"
        }
    }
}
