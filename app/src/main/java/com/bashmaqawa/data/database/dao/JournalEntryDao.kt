package com.bashmaqawa.data.database.dao

import androidx.room.*
import com.bashmaqawa.data.database.entities.JournalEntry
import com.bashmaqawa.data.database.entities.JournalReferenceType
import kotlinx.coroutines.flow.Flow

/**
 * Journal Entry with account names for display
 */
data class JournalEntryWithDetails(
    val id: Int,
    val entryDate: String,
    val description: String,
    val debitAccountId: Int,
    val debitAccountName: String,
    val creditAccountId: Int,
    val creditAccountName: String,
    val amount: Double,
    val referenceType: String?,
    val referenceId: Int?,
    val isReversing: Boolean,
    val createdAt: String?
)

/**
 * DAO for JournalEntry operations
 * Handles the audit trail for double-entry bookkeeping
 */
@Dao
interface JournalEntryDao {
    
    @Query("SELECT * FROM journal_entries ORDER BY entry_date DESC, id DESC")
    fun getAllJournalEntries(): Flow<List<JournalEntry>>
    
    @Query("""
        SELECT j.id, j.entry_date as entryDate, j.description,
               j.debit_account_id as debitAccountId, da.name as debitAccountName,
               j.credit_account_id as creditAccountId, ca.name as creditAccountName,
               j.amount, j.reference_type as referenceType, j.reference_id as referenceId,
               j.is_reversing as isReversing, j.created_at as createdAt
        FROM journal_entries j
        INNER JOIN accounts da ON j.debit_account_id = da.id
        INNER JOIN accounts ca ON j.credit_account_id = ca.id
        ORDER BY j.entry_date DESC, j.id DESC
    """)
    fun getAllJournalEntriesWithDetails(): Flow<List<JournalEntryWithDetails>>
    
    @Query("""
        SELECT j.id, j.entry_date as entryDate, j.description,
               j.debit_account_id as debitAccountId, da.name as debitAccountName,
               j.credit_account_id as creditAccountId, ca.name as creditAccountName,
               j.amount, j.reference_type as referenceType, j.reference_id as referenceId,
               j.is_reversing as isReversing, j.created_at as createdAt
        FROM journal_entries j
        INNER JOIN accounts da ON j.debit_account_id = da.id
        INNER JOIN accounts ca ON j.credit_account_id = ca.id
        WHERE j.entry_date BETWEEN :startDate AND :endDate
        ORDER BY j.entry_date DESC, j.id DESC
    """)
    fun getJournalEntriesInRange(startDate: String, endDate: String): Flow<List<JournalEntryWithDetails>>
    
    @Query("SELECT * FROM journal_entries WHERE reference_type = :referenceType AND reference_id = :referenceId")
    suspend fun getJournalEntriesByReference(referenceType: JournalReferenceType, referenceId: Int): List<JournalEntry>
    
    @Query("SELECT * FROM journal_entries WHERE debit_account_id = :accountId OR credit_account_id = :accountId ORDER BY entry_date DESC")
    fun getJournalEntriesByAccount(accountId: Int): Flow<List<JournalEntry>>
    
    @Query("SELECT * FROM journal_entries WHERE id = :id")
    suspend fun getJournalEntryById(id: Int): JournalEntry?
    
    @Query("SELECT SUM(amount) FROM journal_entries WHERE debit_account_id = :accountId AND entry_date BETWEEN :startDate AND :endDate")
    suspend fun getTotalDebitsForAccount(accountId: Int, startDate: String, endDate: String): Double?
    
    @Query("SELECT SUM(amount) FROM journal_entries WHERE credit_account_id = :accountId AND entry_date BETWEEN :startDate AND :endDate")
    suspend fun getTotalCreditsForAccount(accountId: Int, startDate: String, endDate: String): Double?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournalEntry(journalEntry: JournalEntry): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournalEntries(journalEntries: List<JournalEntry>)
    
    @Update
    suspend fun updateJournalEntry(journalEntry: JournalEntry)
    
    @Delete
    suspend fun deleteJournalEntry(journalEntry: JournalEntry)
    
    @Query("DELETE FROM journal_entries WHERE id = :id")
    suspend fun deleteJournalEntryById(id: Int)
    
    @Query("SELECT COUNT(*) FROM journal_entries")
    suspend fun getTotalJournalEntryCount(): Int
}
