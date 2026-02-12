package com.bashmaqawa.data.database.dao

import androidx.room.*
import com.bashmaqawa.data.database.entities.Transaction
import com.bashmaqawa.data.database.entities.TransactionType
import com.bashmaqawa.data.database.entities.TransactionState
import kotlinx.coroutines.flow.Flow

/**
 * Transaction with related entity names for display
 */
data class TransactionWithDetails(
    val id: Int,
    val projectId: Int?,
    val projectName: String?,
    val accountId: Int?,
    val accountName: String?,
    val sourceAccountId: Int?,
    val sourceAccountName: String?,
    val destinationAccountId: Int?,
    val destinationAccountName: String?,
    val amount: Double?,
    val category: String?,
    val type: String?,
    val transactionState: String?,
    val description: String?,
    val date: String?,
    val costCenterId: Int?,
    val costCenterName: String?,
    val workerId: Int?,
    val workerName: String?,
    val invoiceImage: String?,
    val referenceNumber: String?,
    val paymentMethod: String?,
    val isReconciled: Boolean,
    val createdAt: String?,
    val modifiedAt: String?
)

/**
 * Category summary for analytics
 */
data class CategorySummary(
    val category: String,
    val total: Double,
    val count: Int
)

/**
 * Profit/Loss summary by project
 */
data class ProjectProfitLoss(
    val projectId: Int,
    val projectName: String,
    val totalIncome: Double,
    val totalExpense: Double,
    val netProfit: Double
)

/**
 * DAO for Transaction operations
 * Enhanced with atomic transaction support for double-entry accounting
 */
@Dao
interface TransactionDao {
    
    @Query("SELECT * FROM transactions ORDER BY date DESC, created_at DESC")
    fun getAllTransactions(): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE transaction_state != 'VOID' ORDER BY date DESC, created_at DESC")
    fun getAllActiveTransactions(): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE type = :type AND transaction_state = :state ORDER BY date DESC")
    fun getTransactionsByTypeAndState(type: TransactionType, state: TransactionState): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE project_id = :projectId ORDER BY date DESC")
    fun getTransactionsByProjectId(projectId: Int): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE account_id = :accountId OR source_account_id = :accountId OR destination_account_id = :accountId ORDER BY date DESC")
    fun getTransactionsByAccountId(accountId: Int): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE worker_id = :workerId ORDER BY date DESC")
    fun getTransactionsByWorkerId(workerId: Int): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsInDateRange(startDate: String, endDate: String): Flow<List<Transaction>>
    
    @Query("""
        SELECT t.id, t.project_id as projectId, p.name as projectName,
               t.account_id as accountId, a.name as accountName,
               t.source_account_id as sourceAccountId, sa.name as sourceAccountName,
               t.destination_account_id as destinationAccountId, da.name as destinationAccountName,
               t.amount, t.category, t.type, t.transaction_state as transactionState, t.description, t.date,
               t.cost_center_id as costCenterId, c.name as costCenterName,
               t.worker_id as workerId, w.name as workerName,
               t.invoice_image as invoiceImage, t.reference_number as referenceNumber,
               t.payment_method as paymentMethod, t.is_reconciled as isReconciled,
               t.created_at as createdAt, t.modified_at as modifiedAt
        FROM transactions t
        LEFT JOIN projects p ON t.project_id = p.id
        LEFT JOIN accounts a ON t.account_id = a.id
        LEFT JOIN accounts sa ON t.source_account_id = sa.id
        LEFT JOIN accounts da ON t.destination_account_id = da.id
        LEFT JOIN cost_centers c ON t.cost_center_id = c.id
        LEFT JOIN workers w ON t.worker_id = w.id
        WHERE t.date BETWEEN :startDate AND :endDate
        ORDER BY t.date DESC
    """)
    fun getTransactionsWithDetailsInRange(startDate: String, endDate: String): Flow<List<TransactionWithDetails>>
    
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Int): Transaction?
    
    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type AND date BETWEEN :startDate AND :endDate AND transaction_state != 'VOID'")
    suspend fun getTotalByTypeInRange(type: TransactionType, startDate: String, endDate: String): Double?
    
    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type AND project_id = :projectId AND transaction_state != 'VOID'")
    suspend fun getTotalByTypeAndProject(type: TransactionType, projectId: Int): Double?
    
    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type AND date = :date AND transaction_state != 'VOID'")
    suspend fun getTotalByTypeAndDate(type: TransactionType, date: String): Double?
    
    @Query("""
        SELECT category, SUM(amount) as total, COUNT(*) as count 
        FROM transactions 
        WHERE type = :type AND date BETWEEN :startDate AND :endDate AND transaction_state != 'VOID'
        GROUP BY category
        ORDER BY total DESC
    """)
    suspend fun getCategorySummary(type: TransactionType, startDate: String, endDate: String): List<CategorySummary>
    
    @Query("""
        SELECT p.id as projectId, p.name as projectName,
               COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END), 0) as totalIncome,
               COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0) as totalExpense,
               COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END), 0) - 
               COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0) as netProfit
        FROM projects p
        LEFT JOIN transactions t ON p.id = t.project_id AND t.transaction_state != 'VOID'
        GROUP BY p.id, p.name
    """)
    suspend fun getProjectProfitLossSummary(): List<ProjectProfitLoss>
    
    @Query("""
        SELECT p.id as projectId, p.name as projectName,
               COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END), 0) as totalIncome,
               COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0) as totalExpense,
               COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END), 0) - 
               COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0) as netProfit
        FROM projects p
        LEFT JOIN transactions t ON p.id = t.project_id AND t.transaction_state != 'VOID'
        WHERE p.id = :projectId
        GROUP BY p.id, p.name
    """)
    suspend fun getProjectProfitLossById(projectId: Int): ProjectProfitLoss?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<Transaction>)
    
    @Update
    suspend fun updateTransaction(transaction: Transaction)
    
    @Query("UPDATE transactions SET transaction_state = :state, modified_at = :modifiedAt WHERE id = :id")
    suspend fun updateTransactionState(id: Int, state: TransactionState, modifiedAt: String)
    
    @Delete
    suspend fun deleteTransaction(transaction: Transaction)
    
    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Int)
    
    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getTotalTransactionCount(): Int
    
    @Query("SELECT COUNT(*) FROM transactions WHERE transaction_state = :state")
    suspend fun getTransactionCountByState(state: TransactionState): Int
}

