package com.bashmaqawa.presentation.screens.bankstatement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bashmaqawa.core.Resource
import com.bashmaqawa.data.database.entities.Account
import com.bashmaqawa.data.database.entities.AccountType
import com.bashmaqawa.data.repository.FinancialRepository
import com.bashmaqawa.pdf.PdfReportEngine
import com.bashmaqawa.pdf.models.PdfGenerationProgress
import com.bashmaqawa.pdf.models.PdfGenerationResult
import com.bashmaqawa.pdf.models.ReportConfig
import com.bashmaqawa.pdf.models.ReportType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

/**
 * Bank Statement ViewModel
 * نموذج عرض كشف الحساب البنكي
 */
@HiltViewModel
class BankStatementViewModel @Inject constructor(
    private val financialRepository: FinancialRepository,
    private val pdfReportEngine: PdfReportEngine
) : ViewModel() {
    
    private val _state = MutableStateFlow(BankStatementState())
    val state: StateFlow<BankStatementState> = _state.asStateFlow()
    
    private val _effect = MutableSharedFlow<BankStatementEffect>()
    val effect: SharedFlow<BankStatementEffect> = _effect.asSharedFlow()
    
    init {
        // Collect PDF generation progress
        viewModelScope.launch {
            pdfReportEngine.progress.collect { progress ->
                _state.update { it.copy(generationProgress = progress) }
            }
        }
        
        // Load accounts initially
        onEvent(BankStatementEvent.LoadAccounts)
    }
    
    /**
     * Handle UI events
     */
    fun onEvent(event: BankStatementEvent) {
        when (event) {
            // Account Selection
            is BankStatementEvent.LoadAccounts -> loadAccounts()
            is BankStatementEvent.SelectAccount -> selectAccount(event.account)
            is BankStatementEvent.ShowAccountSelector -> _state.update { it.copy(showAccountSelectorSheet = true) }
            is BankStatementEvent.HideAccountSelector -> _state.update { it.copy(showAccountSelectorSheet = false) }
            
            // Date Range
            is BankStatementEvent.SetStartDate -> setStartDate(event.date)
            is BankStatementEvent.SetEndDate -> setEndDate(event.date)
            is BankStatementEvent.ShowStartDatePicker -> _state.update { it.copy(showStartDatePicker = true) }
            is BankStatementEvent.ShowEndDatePicker -> _state.update { it.copy(showEndDatePicker = true) }
            is BankStatementEvent.HideDatePickers -> _state.update { 
                it.copy(showStartDatePicker = false, showEndDatePicker = false) 
            }
            
            // Quick Date Range Presets
            is BankStatementEvent.SetThisMonth -> setThisMonth()
            is BankStatementEvent.SetLastMonth -> setLastMonth()
            is BankStatementEvent.SetLast3Months -> setLast3Months()
            is BankStatementEvent.SetThisYear -> setThisYear()
            
            // Options
            is BankStatementEvent.SetIncludeAnalytics -> _state.update { 
                it.copy(includeAnalytics = event.include) 
            }
            
            // Generation
            is BankStatementEvent.GenerateStatement -> generateStatement()
            is BankStatementEvent.CancelGeneration -> cancelGeneration()
            is BankStatementEvent.LoadPreview -> loadPreview()
            
            // Actions
            is BankStatementEvent.SharePdf -> sharePdf()
            is BankStatementEvent.OpenPdf -> openPdf()
            is BankStatementEvent.DismissSuccessDialog -> _state.update { 
                it.copy(showSuccessDialog = false) 
            }
            is BankStatementEvent.DismissError -> _state.update { it.copy(error = null) }
        }
    }
    
    /**
     * Load available accounts for selection
     */
    private fun loadAccounts() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingAccounts = true) }
            
            // Get bank, cash box, and wallet accounts
            financialRepository.getAllActiveAccounts()
                .catch { e -> 
                    _state.update { it.copy(error = "فشل في تحميل الحسابات: ${e.message}") }
                }
                .collect { accounts ->
                    val eligibleAccounts = accounts.filter { account ->
                        account.type in listOf(
                            AccountType.BANK,
                            AccountType.CASH_BOX,
                            AccountType.WALLET
                        )
                    }
                    _state.update { 
                        it.copy(
                            availableAccounts = eligibleAccounts,
                            isLoadingAccounts = false,
                            // Auto-select if only one account
                            selectedAccount = if (eligibleAccounts.size == 1) eligibleAccounts.first() else it.selectedAccount
                        )
                    }
                }
        }
    }
    
    /**
     * Select an account
     */
    private fun selectAccount(account: Account) {
        _state.update { 
            it.copy(
                selectedAccount = account,
                showAccountSelectorSheet = false,
                previewData = null // Clear preview when account changes
            ) 
        }
    }
    
    /**
     * Set start date
     */
    private fun setStartDate(date: LocalDate) {
        _state.update { 
            it.copy(
                startDate = date,
                showStartDatePicker = false,
                previewData = null // Clear preview when date changes
            ) 
        }
    }
    
    /**
     * Set end date
     */
    private fun setEndDate(date: LocalDate) {
        _state.update { 
            it.copy(
                endDate = date,
                showEndDatePicker = false,
                previewData = null // Clear preview when date changes
            ) 
        }
    }
    
    /**
     * Set this month date range
     */
    private fun setThisMonth() {
        val now = LocalDate.now()
        val startOfMonth = now.with(TemporalAdjusters.firstDayOfMonth())
        _state.update { 
            it.copy(
                startDate = startOfMonth,
                endDate = now,
                previewData = null
            ) 
        }
    }
    
    /**
     * Set last month date range
     */
    private fun setLastMonth() {
        val now = LocalDate.now()
        val startOfLastMonth = now.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth())
        val endOfLastMonth = now.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth())
        _state.update { 
            it.copy(
                startDate = startOfLastMonth,
                endDate = endOfLastMonth,
                previewData = null
            ) 
        }
    }
    
    /**
     * Set last 3 months date range
     */
    private fun setLast3Months() {
        val now = LocalDate.now()
        val startDate = now.minusMonths(3)
        _state.update { 
            it.copy(
                startDate = startDate,
                endDate = now,
                previewData = null
            ) 
        }
    }
    
    /**
     * Set this year date range
     */
    private fun setThisYear() {
        val now = LocalDate.now()
        val startOfYear = now.with(TemporalAdjusters.firstDayOfYear())
        _state.update { 
            it.copy(
                startDate = startOfYear,
                endDate = now,
                previewData = null
            ) 
        }
    }
    
    /**
     * Load statement preview data
     */
    private fun loadPreview() {
        val account = _state.value.selectedAccount ?: return
        
        viewModelScope.launch {
            _state.update { it.copy(isLoadingPreview = true) }
            
            val startDate = _state.value.startDate.toString()
            val endDate = _state.value.endDate.toString()
            
            when (val result = financialRepository.generateBankStatementData(
                accountId = account.id,
                startDate = startDate,
                endDate = endDate,
                includeAnalytics = _state.value.includeAnalytics
            )) {
                is Resource.Success -> {
                    _state.update { 
                        it.copy(
                            previewData = result.data,
                            isLoadingPreview = false
                        ) 
                    }
                }
                is Resource.Error -> {
                    _state.update { 
                        it.copy(
                            error = result.message,
                            isLoadingPreview = false
                        ) 
                    }
                }
                else -> {
                    _state.update { it.copy(isLoadingPreview = false) }
                }
            }
        }
    }
    
    /**
     * Generate bank statement PDF
     */
    private fun generateStatement() {
        val account = _state.value.selectedAccount ?: return
        
        viewModelScope.launch {
            _state.update { 
                it.copy(
                    isGenerating = true,
                    error = null,
                    generatedFile = null
                ) 
            }
            
            val startDate = _state.value.startDate.toString()
            val endDate = _state.value.endDate.toString()
            
            // First, get the statement data
            when (val dataResult = financialRepository.generateBankStatementData(
                accountId = account.id,
                startDate = startDate,
                endDate = endDate,
                includeAnalytics = _state.value.includeAnalytics
            )) {
                is Resource.Success -> {
                    // Configure report
                    val config = ReportConfig(
                        reportType = ReportType.BANK_STATEMENT,
                        accountId = account.id
                    )
                    
                    // Generate PDF
                    when (val pdfResult = pdfReportEngine.generateBankStatement(dataResult.data, config)) {
                        is PdfGenerationResult.Success -> {
                            _state.update { 
                                it.copy(
                                    isGenerating = false,
                                    generatedFile = pdfResult.file,
                                    previewData = dataResult.data,
                                    showSuccessDialog = true
                                ) 
                            }
                        }
                        is PdfGenerationResult.Failure -> {
                            _state.update { 
                                it.copy(
                                    isGenerating = false,
                                    error = pdfResult.getMessage()
                                ) 
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    _state.update { 
                        it.copy(
                            isGenerating = false,
                            error = dataResult.message
                        ) 
                    }
                }
                else -> {
                    _state.update { it.copy(isGenerating = false) }
                }
            }
        }
    }
    
    /**
     * Cancel ongoing generation
     */
    private fun cancelGeneration() {
        pdfReportEngine.cancelGeneration()
        _state.update { 
            it.copy(
                isGenerating = false,
                generationProgress = PdfGenerationProgress.Initializing
            ) 
        }
    }
    
    /**
     * Share generated PDF
     */
    private fun sharePdf() {
        val file = _state.value.generatedFile ?: return
        pdfReportEngine.sharePdf(file)
    }
    
    /**
     * Open generated PDF
     */
    private fun openPdf() {
        val file = _state.value.generatedFile ?: return
        pdfReportEngine.openPdf(file)
    }
}
