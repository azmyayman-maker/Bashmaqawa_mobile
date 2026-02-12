package com.bashmaqawa.presentation.screens.transaction

import com.bashmaqawa.data.database.entities.Account
import com.bashmaqawa.data.database.entities.Project
import com.bashmaqawa.data.database.entities.TransactionType
import com.bashmaqawa.data.database.entities.Worker
import java.time.LocalDate

/**
 * Transaction Category for UI display
 * فئة المعاملة للعرض في الواجهة
 */
data class TransactionCategory(
    val arabicName: String,
    val englishName: String,
    val icon: String = "💰"  // Emoji icon for display
)

/**
 * Payment Method options
 * طرق الدفع المتاحة
 */
enum class PaymentMethod(val arabicName: String, val englishName: String) {
    CASH("نقدي", "Cash"),
    BANK_TRANSFER("تحويل بنكي", "Bank Transfer"),
    CHEQUE("شيك", "Cheque"),
    MOBILE_WALLET("محفظة إلكترونية", "Mobile Wallet"),
    CREDIT("آجل", "Credit")
}

/**
 * Form field identifiers for validation error mapping
 * معرفات حقول النموذج لربط أخطاء التحقق
 */
enum class FormField {
    AMOUNT,
    SOURCE_ACCOUNT,
    DESTINATION_ACCOUNT,
    CATEGORY,
    DATE,
    DESCRIPTION,
    REFERENCE_NUMBER,
    PAYMENT_METHOD,
    PROJECT,
    WORKER,
    RECEIPT
}

/**
 * Complete form state for transaction entry
 * حالة النموذج الكاملة لإدخال المعاملة
 */
data class TransactionFormState(
    // Type & Amount
    val transactionType: TransactionType = TransactionType.EXPENSE,
    val amount: String = "",
    val amountDouble: Double = 0.0,
    
    // Account Selection
    val selectedSourceAccount: Account? = null,
    val selectedDestinationAccount: Account? = null,  // For transfers
    val availableAccounts: List<Account> = emptyList(),
    
    // Categorization
    val selectedCategory: TransactionCategory? = null,
    val availableCategories: List<TransactionCategory> = emptyList(),
    
    // Linking
    val selectedProject: Project? = null,
    val availableProjects: List<Project> = emptyList(),
    val selectedWorker: Worker? = null,
    val availableWorkers: List<Worker> = emptyList(),
    
    // Details
    val date: LocalDate = LocalDate.now(),
    val description: String = "",
    val referenceNumber: String = "",
    val paymentMethod: PaymentMethod? = null,
    val receiptImageUri: String? = null,
    
    // Validation
    val validationErrors: Map<FormField, String> = emptyMap(),
    val isFormValid: Boolean = false,
    
    // UI State
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val showSourceAccountPicker: Boolean = false,
    val showDestinationAccountPicker: Boolean = false,
    val showProjectPicker: Boolean = false,
    val showWorkerPicker: Boolean = false,
    val showDatePicker: Boolean = false,
    val showPaymentMethodPicker: Boolean = false,
    val showReceiptOptions: Boolean = false,
    
    // Balance Preview
    val sourceAccountBalance: Double = 0.0,
    val projectedBalance: Double = 0.0,
    val hasInsufficientBalance: Boolean = false
) {
    /**
     * Get error message for a specific field
     */
    fun getError(field: FormField): String? = validationErrors[field]
    
    /**
     * Check if a specific field has an error
     */
    fun hasError(field: FormField): Boolean = validationErrors.containsKey(field)
    
    /**
     * Check if worker field should be visible
     * العامل مرئي فقط لفئات الأجور والسلف
     */
    val isWorkerVisible: Boolean
        get() = selectedCategory?.englishName in listOf("Wages", "Advance", "Advance Recovery")
    
    /**
     * Check if destination account field should be visible
     * حساب الوجهة مرئي فقط للتحويلات
     */
    val isDestinationAccountVisible: Boolean
        get() = transactionType == TransactionType.TRANSFER
    
    /**
     * Check if category field should be visible
     * التصنيف غير مطلوب للتحويلات
     */
    val isCategoryVisible: Boolean
        get() = transactionType != TransactionType.TRANSFER
}

/**
 * User actions/events for the transaction form
 * أحداث المستخدم لنموذج المعاملة
 */
sealed class TransactionEvent {
    // Type & Amount
    data class TypeChanged(val type: TransactionType) : TransactionEvent()
    data class AmountChanged(val amount: String) : TransactionEvent()
    
    // Account Selection
    data class SourceAccountSelected(val account: Account) : TransactionEvent()
    data class DestinationAccountSelected(val account: Account) : TransactionEvent()
    data object ShowSourceAccountPicker : TransactionEvent()
    data object ShowDestinationAccountPicker : TransactionEvent()
    data object DismissAccountPicker : TransactionEvent()
    
    // Category
    data class CategorySelected(val category: TransactionCategory) : TransactionEvent()
    
    // Linking
    data class ProjectSelected(val project: Project?) : TransactionEvent()
    data class WorkerSelected(val worker: Worker?) : TransactionEvent()
    data object ShowProjectPicker : TransactionEvent()
    data object ShowWorkerPicker : TransactionEvent()
    data object DismissProjectPicker : TransactionEvent()
    data object DismissWorkerPicker : TransactionEvent()
    
    // Date
    data class DateSelected(val date: LocalDate) : TransactionEvent()
    data object ShowDatePicker : TransactionEvent()
    data object DismissDatePicker : TransactionEvent()
    
    // Details
    data class DescriptionChanged(val description: String) : TransactionEvent()
    data class ReferenceNumberChanged(val reference: String) : TransactionEvent()
    data class PaymentMethodSelected(val method: PaymentMethod) : TransactionEvent()
    data object ShowPaymentMethodPicker : TransactionEvent()
    data object DismissPaymentMethodPicker : TransactionEvent()
    
    // Receipt
    data class ReceiptAttached(val uri: String) : TransactionEvent()
    data object RemoveReceipt : TransactionEvent()
    data object ShowReceiptOptions : TransactionEvent()
    data object DismissReceiptOptions : TransactionEvent()
    data object LaunchCamera : TransactionEvent()
    data object LaunchGallery : TransactionEvent()
    
    // Form Actions
    data object ValidateForm : TransactionEvent()
    data object SubmitTransaction : TransactionEvent()
    data object NavigateBack : TransactionEvent()
}

/**
 * Side effects from the ViewModel
 * التأثيرات الجانبية من ViewModel
 */
sealed class TransactionEffect {
    data object NavigateBack : TransactionEffect()
    data class ShowSuccess(val message: String) : TransactionEffect()
    data class ShowError(val message: String) : TransactionEffect()
    data object LaunchCamera : TransactionEffect()
    data object LaunchGallery : TransactionEffect()
    data class ScrollToField(val field: FormField) : TransactionEffect()
}

/**
 * Predefined expense categories
 * فئات المصروفات المحددة مسبقاً
 */
object ExpenseCategories {
    val categories = listOf(
        TransactionCategory("مواد", "Material", "🧱"),
        TransactionCategory("أجور", "Wages", "👷"),
        TransactionCategory("نقل", "Transport", "🚚"),
        TransactionCategory("معدات", "Equipment", "⚙️"),
        TransactionCategory("إيجارات", "Rent", "🏠"),
        TransactionCategory("خدمات", "Services", "🔧"),
        TransactionCategory("صيانة", "Maintenance", "🛠️"),
        TransactionCategory("سلف", "Advance", "💵"),
        TransactionCategory("أخرى", "Other", "📋")
    )
}

/**
 * Predefined income categories
 * فئات الإيرادات المحددة مسبقاً
 */
object IncomeCategories {
    val categories = listOf(
        TransactionCategory("دفعة", "Payment", "💰"),
        TransactionCategory("إيداع", "Deposit", "🏦"),
        TransactionCategory("مستخلص", "Invoice", "📄"),
        TransactionCategory("استرداد سلفة", "Advance Recovery", "↩️"),
        TransactionCategory("أخرى", "Other", "📋")
    )
}
