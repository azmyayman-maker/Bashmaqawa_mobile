package com.bashmaqawa.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * قيود اليومية - Journal Entry Entity
 * Audit trail for double-entry bookkeeping.
 * Every financial movement creates a journal entry for complete traceability.
 */
@Entity(
    tableName = "journal_entries",
    foreignKeys = [
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["debit_account_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["credit_account_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("debit_account_id"),
        Index("credit_account_id"),
        Index("entry_date"),
        Index("reference_type", "reference_id")
    ]
)
data class JournalEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    @ColumnInfo(name = "entry_date")
    val entryDate: String,
    
    @ColumnInfo(name = "description")
    val description: String,
    
    @ColumnInfo(name = "debit_account_id")
    val debitAccountId: Int,
    
    @ColumnInfo(name = "credit_account_id")
    val creditAccountId: Int,
    
    @ColumnInfo(name = "amount")
    val amount: Double,
    
    @ColumnInfo(name = "reference_type")
    val referenceType: JournalReferenceType? = null,
    
    @ColumnInfo(name = "reference_id")
    val referenceId: Int? = null,
    
    @ColumnInfo(name = "is_reversing")
    val isReversing: Boolean = false,
    
    @ColumnInfo(name = "reversed_entry_id")
    val reversedEntryId: Int? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: String? = null
)

/**
 * نوع المرجع - Journal Reference Type
 * Identifies what type of operation created this journal entry
 */
enum class JournalReferenceType {
    TRANSACTION,    // معاملة مالية عادية
    PAYROLL,        // دفع أجور
    ADVANCE,        // سلفة عامل
    TRANSFER,       // تحويل بين حسابات
    ADJUSTMENT,     // تسوية
    OPENING_BALANCE // رصيد افتتاحي
}
