package com.bashmaqawa.presentation.screens.transaction

import com.bashmaqawa.data.database.entities.Account
import com.bashmaqawa.data.database.entities.TransactionType
import java.time.LocalDate

/**
 * Validation result for form fields
 * نتيجة التحقق من صحة الحقول
 */
sealed class ValidationResult {
    data object Valid : ValidationResult()
    data class Error(val message: String) : ValidationResult()
    
    val isValid: Boolean get() = this is Valid
    val errorMessage: String? get() = (this as? Error)?.message
}

/**
 * Transaction validation rules
 * قواعد التحقق من صحة المعاملات
 * 
 * Provides fintech-grade validation for all transaction fields.
 * يوفر تحققًا بمستوى التطبيقات المالية لجميع حقول المعاملة.
 */
object TransactionValidationRules {
    
    // Amount limits
    private const val MIN_AMOUNT = 0.01
    private const val MAX_AMOUNT = 999_999_999.99
    private const val MAX_DESCRIPTION_LENGTH = 500
    private const val MAX_REFERENCE_LENGTH = 50
    
    /**
     * Validate transaction amount
     * يتحقق من صحة مبلغ المعاملة
     */
    fun validateAmount(amount: String): ValidationResult {
        if (amount.isBlank()) {
            return ValidationResult.Error("المبلغ مطلوب")
        }
        
        val parsed = amount.toDoubleOrNull()
        
        return when {
            parsed == null -> ValidationResult.Error("المبلغ غير صالح")
            parsed < MIN_AMOUNT -> ValidationResult.Error("المبلغ يجب أن يكون أكبر من صفر")
            parsed > MAX_AMOUNT -> ValidationResult.Error("المبلغ كبير جداً")
            // Check for too many decimal places
            amount.contains(".") && amount.substringAfter(".").length > 2 -> 
                ValidationResult.Error("المبلغ يجب أن يحتوي على رقمين عشريين كحد أقصى")
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Validate source account selection
     * يتحقق من صحة اختيار الحساب المصدر
     */
    fun validateSourceAccount(
        type: TransactionType,
        account: Account?,
        amount: Double
    ): ValidationResult {
        return when {
            // Source account required for expense and transfer
            account == null && type == TransactionType.EXPENSE -> 
                ValidationResult.Error("يجب اختيار حساب المصدر")
            
            account == null && type == TransactionType.TRANSFER -> 
                ValidationResult.Error("يجب اختيار حساب المصدر للتحويل")
            
            // For income, source account is where money goes (optional but recommended)
            account == null && type == TransactionType.INCOME -> 
                ValidationResult.Error("يجب اختيار الحساب المستلم")
            
            // Check sufficient balance for expenses
            type == TransactionType.EXPENSE && account != null && account.balance < amount ->
                ValidationResult.Error("الرصيد غير كافي (المتاح: ${formatAmount(account.balance)})")
            
            // Check sufficient balance for transfers
            type == TransactionType.TRANSFER && account != null && account.balance < amount ->
                ValidationResult.Error("الرصيد غير كافي للتحويل (المتاح: ${formatAmount(account.balance)})")
            
            // Account is deactivated
            account != null && !account.isActive ->
                ValidationResult.Error("الحساب المحدد غير نشط")
            
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Validate destination account for transfers
     * يتحقق من صحة حساب الوجهة للتحويلات
     */
    fun validateDestinationAccount(
        type: TransactionType,
        sourceAccount: Account?,
        destinationAccount: Account?
    ): ValidationResult {
        return when {
            // Destination only required for transfers
            type != TransactionType.TRANSFER -> ValidationResult.Valid
            
            destinationAccount == null ->
                ValidationResult.Error("يجب اختيار حساب الوجهة للتحويل")
            
            sourceAccount?.id == destinationAccount.id ->
                ValidationResult.Error("لا يمكن التحويل لنفس الحساب")
            
            !destinationAccount.isActive ->
                ValidationResult.Error("حساب الوجهة غير نشط")
            
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Validate category selection
     * يتحقق من صحة اختيار التصنيف
     */
    fun validateCategory(
        type: TransactionType,
        category: TransactionCategory?
    ): ValidationResult {
        return when {
            // Category not required for transfers
            type == TransactionType.TRANSFER -> ValidationResult.Valid
            
            category == null -> ValidationResult.Error("يجب اختيار التصنيف")
            
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Validate transaction date
     * يتحقق من صحة تاريخ المعاملة
     */
    fun validateDate(date: LocalDate): ValidationResult {
        val today = LocalDate.now()
        val fiveYearsAgo = today.minusYears(5)
        
        return when {
            date.isAfter(today) -> 
                ValidationResult.Error("لا يمكن اختيار تاريخ مستقبلي")
            
            date.isBefore(fiveYearsAgo) -> 
                ValidationResult.Error("التاريخ قديم جداً (أكثر من 5 سنوات)")
            
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Validate description
     * يتحقق من صحة الوصف
     */
    fun validateDescription(description: String): ValidationResult {
        return when {
            description.length > MAX_DESCRIPTION_LENGTH ->
                ValidationResult.Error("الوصف طويل جداً (الحد الأقصى $MAX_DESCRIPTION_LENGTH حرف)")
            
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Validate reference number
     * يتحقق من صحة رقم المرجع
     */
    fun validateReferenceNumber(reference: String): ValidationResult {
        return when {
            reference.length > MAX_REFERENCE_LENGTH ->
                ValidationResult.Error("رقم المرجع طويل جداً (الحد الأقصى $MAX_REFERENCE_LENGTH حرف)")
            
            // Optional: Check for valid characters (alphanumeric + dash)
            reference.isNotBlank() && !reference.matches(Regex("^[a-zA-Z0-9\\-_/]+$")) ->
                ValidationResult.Error("رقم المرجع يجب أن يحتوي على حروف وأرقام فقط")
            
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Validate the complete form
     * يتحقق من صحة النموذج الكامل
     * 
     * Returns a map of field to error message for all invalid fields.
     */
    fun validateForm(state: TransactionFormState): Map<FormField, String> {
        val errors = mutableMapOf<FormField, String>()
        
        // Amount validation
        validateAmount(state.amount).errorMessage?.let {
            errors[FormField.AMOUNT] = it
        }
        
        // Source account validation
        validateSourceAccount(
            type = state.transactionType,
            account = state.selectedSourceAccount,
            amount = state.amountDouble
        ).errorMessage?.let {
            errors[FormField.SOURCE_ACCOUNT] = it
        }
        
        // Destination account validation (for transfers)
        if (state.transactionType == TransactionType.TRANSFER) {
            validateDestinationAccount(
                type = state.transactionType,
                sourceAccount = state.selectedSourceAccount,
                destinationAccount = state.selectedDestinationAccount
            ).errorMessage?.let {
                errors[FormField.DESTINATION_ACCOUNT] = it
            }
        }
        
        // Category validation
        validateCategory(
            type = state.transactionType,
            category = state.selectedCategory
        ).errorMessage?.let {
            errors[FormField.CATEGORY] = it
        }
        
        // Date validation
        validateDate(state.date).errorMessage?.let {
            errors[FormField.DATE] = it
        }
        
        // Description validation
        validateDescription(state.description).errorMessage?.let {
            errors[FormField.DESCRIPTION] = it
        }
        
        // Reference number validation
        validateReferenceNumber(state.referenceNumber).errorMessage?.let {
            errors[FormField.REFERENCE_NUMBER] = it
        }
        
        return errors
    }
    
    /**
     * Check if form is valid for submission
     * يتحقق إذا كان النموذج صالحاً للإرسال
     */
    fun isFormValid(state: TransactionFormState): Boolean {
        return validateForm(state).isEmpty()
    }
    
    /**
     * Get the first error field for scrolling
     * يحصل على أول حقل به خطأ للتمرير إليه
     */
    fun getFirstErrorField(errors: Map<FormField, String>): FormField? {
        val fieldOrder = listOf(
            FormField.AMOUNT,
            FormField.SOURCE_ACCOUNT,
            FormField.DESTINATION_ACCOUNT,
            FormField.CATEGORY,
            FormField.DATE,
            FormField.DESCRIPTION,
            FormField.REFERENCE_NUMBER
        )
        
        return fieldOrder.firstOrNull { errors.containsKey(it) }
    }
    
    /**
     * Format amount for display in error messages
     */
    private fun formatAmount(amount: Double): String {
        return String.format("%.2f ج.م", amount)
    }
}

/**
 * Extension function to validate and return updated state
 * دالة امتداد للتحقق وإرجاع الحالة المحدثة
 */
fun TransactionFormState.validate(): TransactionFormState {
    val errors = TransactionValidationRules.validateForm(this)
    return copy(
        validationErrors = errors,
        isFormValid = errors.isEmpty()
    )
}
