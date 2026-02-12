package com.bashmaqawa.presentation.screens.bankstatement

import com.bashmaqawa.data.database.entities.Account
import com.bashmaqawa.pdf.models.BankStatementData
import com.bashmaqawa.pdf.models.PdfGenerationProgress
import java.io.File
import java.time.LocalDate

/**
 * Bank Statement UI State
 * حالة واجهة كشف الحساب البنكي
 */
data class BankStatementState(
    // Account Selection
    val availableAccounts: List<Account> = emptyList(),
    val selectedAccount: Account? = null,
    val isLoadingAccounts: Boolean = false,
    
    // Date Range Selection
    val startDate: LocalDate = LocalDate.now().minusMonths(1),
    val endDate: LocalDate = LocalDate.now(),
    val showStartDatePicker: Boolean = false,
    val showEndDatePicker: Boolean = false,
    
    // Statement Options
    val includeAnalytics: Boolean = true,
    
    // Generation State
    val isGenerating: Boolean = false,
    val generationProgress: PdfGenerationProgress = PdfGenerationProgress.Initializing,
    
    // Preview Data
    val previewData: BankStatementData? = null,
    val isLoadingPreview: Boolean = false,
    
    // Result
    val generatedFile: File? = null,
    val error: String? = null,
    
    // Dialog States
    val showSuccessDialog: Boolean = false,
    val showAccountSelectorSheet: Boolean = false
) {
    /**
     * Check if statement can be generated
     */
    val canGenerate: Boolean
        get() = selectedAccount != null && 
                !isGenerating && 
                !startDate.isAfter(endDate)
    
    /**
     * Check if dates are valid
     */
    val isDateRangeValid: Boolean
        get() = !startDate.isAfter(endDate)
    
    /**
     * Get date range summary text
     */
    val dateRangeSummary: String
        get() = "${startDate.dayOfMonth}/${startDate.monthValue}/${startDate.year} - ${endDate.dayOfMonth}/${endDate.monthValue}/${endDate.year}"
    
    /**
     * Get progress percentage (0-100)
     */
    val progressPercentage: Float
        get() = generationProgress.getProgressPercent() * 100f
}

/**
 * Bank Statement UI Events
 * أحداث واجهة كشف الحساب البنكي
 */
sealed interface BankStatementEvent {
    // Account Selection
    data object LoadAccounts : BankStatementEvent
    data class SelectAccount(val account: Account) : BankStatementEvent
    data object ShowAccountSelector : BankStatementEvent
    data object HideAccountSelector : BankStatementEvent
    
    // Date Range
    data class SetStartDate(val date: LocalDate) : BankStatementEvent
    data class SetEndDate(val date: LocalDate) : BankStatementEvent
    data object ShowStartDatePicker : BankStatementEvent
    data object ShowEndDatePicker : BankStatementEvent
    data object HideDatePickers : BankStatementEvent
    
    // Quick Date Range Presets
    data object SetThisMonth : BankStatementEvent
    data object SetLastMonth : BankStatementEvent
    data object SetLast3Months : BankStatementEvent
    data object SetThisYear : BankStatementEvent
    
    // Options
    data class SetIncludeAnalytics(val include: Boolean) : BankStatementEvent
    
    // Generation
    data object GenerateStatement : BankStatementEvent
    data object CancelGeneration : BankStatementEvent
    data object LoadPreview : BankStatementEvent
    
    // Actions
    data object SharePdf : BankStatementEvent
    data object OpenPdf : BankStatementEvent
    data object DismissSuccessDialog : BankStatementEvent
    data object DismissError : BankStatementEvent
}

/**
 * Bank Statement Navigation Effects
 */
sealed interface BankStatementEffect {
    data object NavigateBack : BankStatementEffect
    data class ShowSnackbar(val message: String) : BankStatementEffect
}
