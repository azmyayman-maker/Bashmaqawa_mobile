package com.bashmaqawa.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * المعاملات المالية - Transactions Entity
 * Represents financial transactions with double-entry accounting support.
 * Every transaction affects at least one account (sourceAccountId).
 * For transfers, both source and destination accounts are affected.
 */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Project::class,
            parentColumns = ["id"],
            childColumns = ["project_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["account_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["source_account_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["destination_account_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = CostCenter::class,
            parentColumns = ["id"],
            childColumns = ["cost_center_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Worker::class,
            parentColumns = ["id"],
            childColumns = ["worker_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("project_id"),
        Index("account_id"),
        Index("source_account_id"),
        Index("destination_account_id"),
        Index("cost_center_id"),
        Index("worker_id"),
        Index("date"),
        Index("type"),
        Index("transaction_state")
    ]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    @ColumnInfo(name = "project_id")
    val projectId: Int? = null,
    
    // Legacy field - kept for backward compatibility
    @ColumnInfo(name = "account_id")
    val accountId: Int? = null,
    
    // New double-entry fields
    @ColumnInfo(name = "source_account_id")
    val sourceAccountId: Int? = null,
    
    @ColumnInfo(name = "destination_account_id")
    val destinationAccountId: Int? = null,
    
    @ColumnInfo(name = "amount")
    val amount: Double? = null,
    
    @ColumnInfo(name = "category")
    val category: String? = null, // Material, Wages, Deposit, etc.
    
    @ColumnInfo(name = "type")
    val type: TransactionType? = null,
    
    @ColumnInfo(name = "transaction_state")
    val transactionState: TransactionState = TransactionState.CLEARED,
    
    @ColumnInfo(name = "description")
    val description: String? = null,
    
    @ColumnInfo(name = "date")
    val date: String? = null,
    
    @ColumnInfo(name = "cost_center_id")
    val costCenterId: Int? = null,
    
    @ColumnInfo(name = "worker_id")
    val workerId: Int? = null,
    
    @ColumnInfo(name = "invoice_image")
    val invoiceImage: String? = null, // URI to image
    
    @ColumnInfo(name = "reference_number")
    val referenceNumber: String? = null,
    
    @ColumnInfo(name = "payment_method")
    val paymentMethod: String? = null,
    
    @ColumnInfo(name = "is_reconciled")
    val isReconciled: Boolean = false,
    
    @ColumnInfo(name = "journal_entry_id")
    val journalEntryId: Int? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: String? = null,
    
    @ColumnInfo(name = "modified_at")
    val modifiedAt: String? = null
) {
    /**
     * Get the effective source account ID (uses legacy accountId if sourceAccountId not set)
     */
    val effectiveSourceAccountId: Int?
        get() = sourceAccountId ?: accountId
}

/**
 * نوع المعاملة - Transaction Type Enum
 */
enum class TransactionType {
    INCOME,     // إيراد
    EXPENSE,    // مصروف
    TRANSFER    // تحويل بين حسابات
}

/**
 * حالة المعاملة - Transaction State Enum
 * Tracks the lifecycle state of a transaction
 */
enum class TransactionState {
    PENDING,    // معلقة - awaiting clearance/approval
    CLEARED,    // مقاصة - completed and balanced
    VOID        // ملغاة - reversed/cancelled transaction
}

/**
 * فئات المعاملات الشائعة - Common Transaction Categories
 */
object TransactionCategories {
    val expenseCategories = listOf(
        "مواد" to "Material",
        "أجور" to "Wages",
        "نقل" to "Transport",
        "معدات" to "Equipment",
        "إيجارات" to "Rent",
        "خدمات" to "Services",
        "صيانة" to "Maintenance",
        "سلف" to "Advance",
        "أخرى" to "Other"
    )
    
    val incomeCategories = listOf(
        "دفعة" to "Payment",
        "إيداع" to "Deposit",
        "مستخلص" to "Invoice",
        "استرداد سلفة" to "Advance Recovery",
        "أخرى" to "Other"
    )
}

