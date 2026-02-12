package com.bashmaqawa.presentation.screens.transaction

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bashmaqawa.core.Resource
import com.bashmaqawa.data.database.entities.Account
import com.bashmaqawa.data.database.entities.Project
import com.bashmaqawa.data.database.entities.Transaction
import com.bashmaqawa.data.database.entities.TransactionState
import com.bashmaqawa.data.database.entities.TransactionType
import com.bashmaqawa.data.database.entities.Worker
import com.bashmaqawa.data.repository.FinancialRepository
import com.bashmaqawa.data.repository.ProjectRepository
import com.bashmaqawa.data.repository.WorkerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * ViewModel for Add Transaction Screen
 * فيو موديل شاشة إضافة المعاملة
 * 
 * Implements MVI pattern with:
 * - StateFlow for UI state
 * - Channel for one-time effects
 * - Event-driven state mutations
 */
@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val financialRepository: FinancialRepository,
    private val projectRepository: ProjectRepository,
    private val workerRepository: WorkerRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    
    // UI State
    private val _state = MutableStateFlow(TransactionFormState())
    val state: StateFlow<TransactionFormState> = _state.asStateFlow()
    
    // One-time effects channel
    private val _effect = Channel<TransactionEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()
    
    // Preselected IDs from navigation arguments
    private val preselectedProjectId: Int? = savedStateHandle.get<Int>("projectId")?.takeIf { it > 0 }
    private val preselectedWorkerId: Int? = savedStateHandle.get<Int>("workerId")?.takeIf { it > 0 }
    
    init {
        loadInitialData()
    }
    
    /**
     * Load initial data: accounts, projects, workers, and categories
     * تحميل البيانات الأولية: الحسابات والمشاريع والعمال والفئات
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                // Load accounts
                val accounts = financialRepository.getAllActiveAccounts().first()
                
                // Load active projects
                val projects = projectRepository.getActiveProjects().first()
                
                // Load active workers
                val workers = workerRepository.getActiveWorkers().first()
                
                // Set initial categories based on transaction type
                val categories = ExpenseCategories.categories
                
                // Set initial state with loaded data
                _state.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        availableAccounts = accounts,
                        availableProjects = projects,
                        availableWorkers = workers,
                        availableCategories = categories,
                        selectedCategory = categories.firstOrNull()
                    )
                }
                
                // Apply preselections if provided
                applyPreselections()
                
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                _effect.send(TransactionEffect.ShowError("خطأ في تحميل البيانات: ${e.message}"))
            }
        }
    }
    
    /**
     * Apply preselected project/worker from navigation arguments
     */
    private suspend fun applyPreselections() {
        preselectedProjectId?.let { projectId ->
            val project = projectRepository.getProjectById(projectId)
            if (project != null) {
                _state.update { it.copy(selectedProject = project) }
            }
        }
        
        preselectedWorkerId?.let { workerId ->
            val worker = workerRepository.getWorkerById(workerId)
            if (worker != null) {
                _state.update { it.copy(selectedWorker = worker) }
            }
        }
    }
    
    /**
     * Process incoming events from the UI
     * معالجة الأحداث الواردة من الواجهة
     */
    fun onEvent(event: TransactionEvent) {
        when (event) {
            // Type & Amount
            is TransactionEvent.TypeChanged -> handleTypeChange(event.type)
            is TransactionEvent.AmountChanged -> handleAmountChange(event.amount)
            
            // Account Selection
            is TransactionEvent.SourceAccountSelected -> handleSourceAccountSelected(event.account)
            is TransactionEvent.DestinationAccountSelected -> handleDestinationAccountSelected(event.account)
            is TransactionEvent.ShowSourceAccountPicker -> showPicker { it.copy(showSourceAccountPicker = true) }
            is TransactionEvent.ShowDestinationAccountPicker -> showPicker { it.copy(showDestinationAccountPicker = true) }
            is TransactionEvent.DismissAccountPicker -> dismissAllPickers()
            
            // Category
            is TransactionEvent.CategorySelected -> handleCategorySelected(event.category)
            
            // Linking
            is TransactionEvent.ProjectSelected -> handleProjectSelected(event.project)
            is TransactionEvent.WorkerSelected -> handleWorkerSelected(event.worker)
            is TransactionEvent.ShowProjectPicker -> showPicker { it.copy(showProjectPicker = true) }
            is TransactionEvent.ShowWorkerPicker -> showPicker { it.copy(showWorkerPicker = true) }
            is TransactionEvent.DismissProjectPicker -> showPicker { it.copy(showProjectPicker = false) }
            is TransactionEvent.DismissWorkerPicker -> showPicker { it.copy(showWorkerPicker = false) }
            
            // Date
            is TransactionEvent.DateSelected -> handleDateSelected(event.date)
            is TransactionEvent.ShowDatePicker -> showPicker { it.copy(showDatePicker = true) }
            is TransactionEvent.DismissDatePicker -> showPicker { it.copy(showDatePicker = false) }
            
            // Details
            is TransactionEvent.DescriptionChanged -> handleDescriptionChanged(event.description)
            is TransactionEvent.ReferenceNumberChanged -> handleReferenceNumberChanged(event.reference)
            is TransactionEvent.PaymentMethodSelected -> handlePaymentMethodSelected(event.method)
            is TransactionEvent.ShowPaymentMethodPicker -> showPicker { it.copy(showPaymentMethodPicker = true) }
            is TransactionEvent.DismissPaymentMethodPicker -> showPicker { it.copy(showPaymentMethodPicker = false) }
            
            // Receipt
            is TransactionEvent.ReceiptAttached -> handleReceiptAttached(event.uri)
            is TransactionEvent.RemoveReceipt -> handleRemoveReceipt()
            is TransactionEvent.ShowReceiptOptions -> showPicker { it.copy(showReceiptOptions = true) }
            is TransactionEvent.DismissReceiptOptions -> showPicker { it.copy(showReceiptOptions = false) }
            is TransactionEvent.LaunchCamera -> launchCamera()
            is TransactionEvent.LaunchGallery -> launchGallery()
            
            // Form Actions
            is TransactionEvent.ValidateForm -> validateForm()
            is TransactionEvent.SubmitTransaction -> submitTransaction()
            is TransactionEvent.NavigateBack -> navigateBack()
        }
    }
    
    // =====================================================
    // EVENT HANDLERS
    // =====================================================
    
    private fun handleTypeChange(type: TransactionType) {
        val categories = when (type) {
            TransactionType.EXPENSE -> ExpenseCategories.categories
            TransactionType.INCOME -> IncomeCategories.categories
            TransactionType.TRANSFER -> emptyList()
        }
        
        _state.update { currentState ->
            currentState.copy(
                transactionType = type,
                availableCategories = categories,
                selectedCategory = if (type == TransactionType.TRANSFER) null else categories.firstOrNull(),
                // Clear destination account if not transfer
                selectedDestinationAccount = if (type == TransactionType.TRANSFER) 
                    currentState.selectedDestinationAccount else null,
                // Clear validation errors related to type change
                validationErrors = currentState.validationErrors - setOf(
                    FormField.CATEGORY, 
                    FormField.DESTINATION_ACCOUNT
                )
            )
        }
        
        // Recalculate balance preview
        updateBalancePreview()
    }
    
    private fun handleAmountChange(amount: String) {
        // Filter to allow only valid decimal input
        val filteredAmount = amount.filter { it.isDigit() || it == '.' }
            .let { str ->
                // Allow only one decimal point
                val parts = str.split(".")
                when {
                    parts.size <= 1 -> str
                    else -> "${parts[0]}.${parts[1].take(2)}"
                }
            }
        
        val amountDouble = filteredAmount.toDoubleOrNull() ?: 0.0
        
        _state.update { currentState ->
            currentState.copy(
                amount = filteredAmount,
                amountDouble = amountDouble,
                validationErrors = currentState.validationErrors - FormField.AMOUNT
            )
        }
        
        updateBalancePreview()
    }
    
    private fun handleSourceAccountSelected(account: Account) {
        _state.update { currentState ->
            currentState.copy(
                selectedSourceAccount = account,
                sourceAccountBalance = account.balance,
                showSourceAccountPicker = false,
                validationErrors = currentState.validationErrors - FormField.SOURCE_ACCOUNT
            )
        }
        
        updateBalancePreview()
    }
    
    private fun handleDestinationAccountSelected(account: Account) {
        _state.update { currentState ->
            currentState.copy(
                selectedDestinationAccount = account,
                showDestinationAccountPicker = false,
                validationErrors = currentState.validationErrors - FormField.DESTINATION_ACCOUNT
            )
        }
    }
    
    private fun handleCategorySelected(category: TransactionCategory) {
        _state.update { currentState ->
            currentState.copy(
                selectedCategory = category,
                // Clear worker if category doesn't require it
                selectedWorker = if (category.englishName in listOf("Wages", "Advance", "Advance Recovery"))
                    currentState.selectedWorker else null,
                validationErrors = currentState.validationErrors - FormField.CATEGORY
            )
        }
    }
    
    private fun handleProjectSelected(project: Project?) {
        _state.update { it.copy(selectedProject = project, showProjectPicker = false) }
    }
    
    private fun handleWorkerSelected(worker: Worker?) {
        _state.update { it.copy(selectedWorker = worker, showWorkerPicker = false) }
    }
    
    private fun handleDateSelected(date: LocalDate) {
        _state.update { currentState ->
            currentState.copy(
                date = date,
                showDatePicker = false,
                validationErrors = currentState.validationErrors - FormField.DATE
            )
        }
    }
    
    private fun handleDescriptionChanged(description: String) {
        _state.update { it.copy(description = description) }
    }
    
    private fun handleReferenceNumberChanged(reference: String) {
        _state.update { it.copy(referenceNumber = reference) }
    }
    
    private fun handlePaymentMethodSelected(method: PaymentMethod) {
        _state.update { it.copy(paymentMethod = method, showPaymentMethodPicker = false) }
    }
    
    private fun handleReceiptAttached(uri: String) {
        _state.update { it.copy(receiptImageUri = uri, showReceiptOptions = false) }
    }
    
    private fun handleRemoveReceipt() {
        _state.update { it.copy(receiptImageUri = null) }
    }
    
    private fun launchCamera() {
        _state.update { it.copy(showReceiptOptions = false) }
        viewModelScope.launch {
            _effect.send(TransactionEffect.LaunchCamera)
        }
    }
    
    private fun launchGallery() {
        _state.update { it.copy(showReceiptOptions = false) }
        viewModelScope.launch {
            _effect.send(TransactionEffect.LaunchGallery)
        }
    }
    
    private fun showPicker(update: (TransactionFormState) -> TransactionFormState) {
        _state.update(update)
    }
    
    private fun dismissAllPickers() {
        _state.update {
            it.copy(
                showSourceAccountPicker = false,
                showDestinationAccountPicker = false
            )
        }
    }
    
    private fun navigateBack() {
        viewModelScope.launch {
            _effect.send(TransactionEffect.NavigateBack)
        }
    }
    
    // =====================================================
    // BALANCE PREVIEW
    // =====================================================
    
    private fun updateBalancePreview() {
        val currentState = _state.value
        val account = currentState.selectedSourceAccount ?: return
        val amount = currentState.amountDouble
        
        val projected = when (currentState.transactionType) {
            TransactionType.EXPENSE -> account.balance - amount
            TransactionType.INCOME -> account.balance + amount
            TransactionType.TRANSFER -> account.balance - amount
        }
        
        val hasInsufficientBalance = currentState.transactionType != TransactionType.INCOME 
            && projected < 0
        
        _state.update {
            it.copy(
                sourceAccountBalance = account.balance,
                projectedBalance = projected,
                hasInsufficientBalance = hasInsufficientBalance
            )
        }
    }
    
    // =====================================================
    // VALIDATION & SUBMISSION
    // =====================================================
    
    private fun validateForm() {
        val errors = TransactionValidationRules.validateForm(_state.value)
        _state.update { 
            it.copy(
                validationErrors = errors,
                isFormValid = errors.isEmpty()
            )
        }
        
        // Scroll to first error if any
        if (errors.isNotEmpty()) {
            val firstError = TransactionValidationRules.getFirstErrorField(errors)
            firstError?.let {
                viewModelScope.launch {
                    _effect.send(TransactionEffect.ScrollToField(it))
                }
            }
        }
    }
    
    private fun submitTransaction() {
        // Validate first
        val errors = TransactionValidationRules.validateForm(_state.value)
        if (errors.isNotEmpty()) {
            _state.update { 
                it.copy(
                    validationErrors = errors,
                    isFormValid = false
                )
            }
            val firstError = TransactionValidationRules.getFirstErrorField(errors)
            firstError?.let {
                viewModelScope.launch {
                    _effect.send(TransactionEffect.ScrollToField(it))
                }
            }
            return
        }
        
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            
            val currentState = _state.value
            
            // Build transaction object
            val transaction = Transaction(
                type = currentState.transactionType,
                amount = currentState.amountDouble,
                category = currentState.selectedCategory?.englishName,
                description = currentState.description.takeIf { it.isNotBlank() },
                sourceAccountId = currentState.selectedSourceAccount?.id,
                destinationAccountId = currentState.selectedDestinationAccount?.id,
                projectId = currentState.selectedProject?.id,
                workerId = currentState.selectedWorker?.id,
                date = currentState.date.format(dateFormatter),
                referenceNumber = currentState.referenceNumber.takeIf { it.isNotBlank() },
                paymentMethod = currentState.paymentMethod?.englishName,
                invoiceImage = currentState.receiptImageUri,
                transactionState = TransactionState.CLEARED
            )
            
            // Process via repository (ACID-compliant)
            when (val result = financialRepository.processTransaction(transaction)) {
                is Resource.Success -> {
                    _state.update { it.copy(isSaving = false) }
                    _effect.send(TransactionEffect.ShowSuccess("تم حفظ المعاملة بنجاح"))
                    _effect.send(TransactionEffect.NavigateBack)
                }
                is Resource.Error -> {
                    _state.update { it.copy(isSaving = false) }
                    _effect.send(TransactionEffect.ShowError(result.message ?: "خطأ في حفظ المعاملة"))
                }
                is Resource.Loading -> {
                    // Handle loading state if needed
                }
            }
        }
    }
}
