package com.bashmaqawa.core

/**
 * نتيجة العملية - Resource Wrapper
 * A generic sealed class for handling operation results with explicit error handling.
 * Used throughout the app for clean error propagation.
 * 
 * @param T The type of data on success
 */
sealed class Resource<out T> {
    
    /**
     * نجاح العملية - Success state
     * Contains the successful result data
     */
    data class Success<T>(val data: T) : Resource<T>()
    
    /**
     * فشل العملية - Error state
     * Contains error message and optional cause
     */
    data class Error(
        val message: String,
        val cause: Throwable? = null,
        val errorCode: ErrorCode = ErrorCode.UNKNOWN
    ) : Resource<Nothing>()
    
    /**
     * جاري التحميل - Loading state
     * Indicates an operation is in progress
     */
    data object Loading : Resource<Nothing>()
    
    /**
     * Check if this resource is successful
     */
    val isSuccess: Boolean get() = this is Success
    
    /**
     * Check if this resource is an error
     */
    val isError: Boolean get() = this is Error
    
    /**
     * Check if this resource is loading
     */
    val isLoading: Boolean get() = this is Loading
    
    /**
     * Get data or null if not successful
     */
    fun getOrNull(): T? = (this as? Success)?.data
    
    /**
     * Get data or throw if not successful
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw cause ?: IllegalStateException(message)
        is Loading -> throw IllegalStateException("Resource is still loading")
    }
    
    /**
     * Get data or default value if not successful
     */
    fun getOrDefault(default: @UnsafeVariance T): T = getOrNull() ?: default
    
    /**
     * Transform the data if successful
     */
    inline fun <R> map(transform: (T) -> R): Resource<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
        is Loading -> Loading
    }
    
    /**
     * Handle both success and error cases
     */
    inline fun <R> fold(
        onSuccess: (T) -> R,
        onError: (Error) -> R,
        onLoading: () -> R
    ): R = when (this) {
        is Success -> onSuccess(data)
        is Error -> onError(this)
        is Loading -> onLoading()
    }
    
    /**
     * Execute action only on success
     */
    inline fun onSuccess(action: (T) -> Unit): Resource<T> {
        if (this is Success) action(data)
        return this
    }
    
    /**
     * Execute action only on error
     */
    inline fun onError(action: (Error) -> Unit): Resource<T> {
        if (this is Error) action(this)
        return this
    }
    
    companion object {
        /**
         * Create a successful resource
         */
        fun <T> success(data: T): Resource<T> = Success(data)
        
        /**
         * Create an error resource
         */
        fun error(message: String, cause: Throwable? = null, errorCode: ErrorCode = ErrorCode.UNKNOWN): Resource<Nothing> =
            Error(message, cause, errorCode)
        
        /**
         * Create a loading resource
         */
        fun loading(): Resource<Nothing> = Loading
    }
}

/**
 * رموز الأخطاء - Error Codes
 * Standardized error codes for the financial system
 */
enum class ErrorCode {
    UNKNOWN,
    VALIDATION_FAILED,
    INSUFFICIENT_BALANCE,
    ACCOUNT_NOT_FOUND,
    TRANSACTION_NOT_FOUND,
    WORKER_NOT_FOUND,
    PROJECT_NOT_FOUND,
    INVALID_AMOUNT,
    INVALID_DATE_RANGE,
    OPERATION_CANCELLED,
    DATABASE_ERROR,
    PAYROLL_ALREADY_PAID,
    ADVANCE_ALREADY_SETTLED,
    TRANSACTION_ALREADY_VOID
}

/**
 * Extension function to convert a suspend block to Resource
 */
suspend inline fun <T> safeCall(crossinline block: suspend () -> T): Resource<T> {
    return try {
        Resource.Success(block())
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Unknown error occurred", e)
    }
}

/**
 * Extension function to run multiple Resources and combine results
 */
inline fun <T1, T2, R> combineResources(
    r1: Resource<T1>,
    r2: Resource<T2>,
    transform: (T1, T2) -> R
): Resource<R> {
    return when {
        r1 is Resource.Error -> r1
        r2 is Resource.Error -> r2
        r1 is Resource.Loading || r2 is Resource.Loading -> Resource.Loading
        r1 is Resource.Success && r2 is Resource.Success -> 
            Resource.Success(transform(r1.data, r2.data))
        else -> Resource.Error("Unexpected state")
    }
}
