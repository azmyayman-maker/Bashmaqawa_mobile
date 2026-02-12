package com.bashmaqawa.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * سلف العمال - Worker Advances Entity
 * Represents salary advances given to workers.
 * Advances are tracked as Assets (Receivables) for the company.
 * When settled, they are deducted from the worker's payroll.
 */
@Entity(
    tableName = "worker_advances",
    foreignKeys = [
        ForeignKey(
            entity = Worker::class,
            parentColumns = ["id"],
            childColumns = ["worker_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["account_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Transaction::class,
            parentColumns = ["id"],
            childColumns = ["transaction_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Transaction::class,
            parentColumns = ["id"],
            childColumns = ["settlement_transaction_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("worker_id"),
        Index("account_id"),
        Index("transaction_id"),
        Index("settlement_transaction_id"),
        Index("is_settled")
    ]
)
data class WorkerAdvance(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    @ColumnInfo(name = "worker_id")
    val workerId: Int,
    
    @ColumnInfo(name = "amount")
    val amount: Double,
    
    @ColumnInfo(name = "reason")
    val reason: String? = null,
    
    @ColumnInfo(name = "date")
    val date: String, // Format: "2024-01-15"
    
    @ColumnInfo(name = "account_id")
    val accountId: Int? = null, // The account the advance was paid from
    
    @ColumnInfo(name = "transaction_id")
    val transactionId: Int? = null, // Link to the advance payment transaction
    
    @ColumnInfo(name = "is_settled")
    val isSettled: Boolean = false, // Whether deducted from wages
    
    @ColumnInfo(name = "settled_date")
    val settledDate: String? = null, // Date when settled
    
    @ColumnInfo(name = "settled_amount")
    val settledAmount: Double? = null, // Partial settlement support
    
    @ColumnInfo(name = "settlement_transaction_id")
    val settlementTransactionId: Int? = null, // Link to payroll payment transaction
    
    @ColumnInfo(name = "notes")
    val notes: String? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: String? = null
) {
    /**
     * Get remaining unsettled amount for partial settlements
     */
    val remainingAmount: Double
        get() = amount - (settledAmount ?: 0.0)
    
    /**
     * Check if advance is fully settled
     */
    val isFullySettled: Boolean
        get() = isSettled || remainingAmount <= 0.0
}
