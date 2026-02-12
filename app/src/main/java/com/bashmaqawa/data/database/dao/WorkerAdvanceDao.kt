package com.bashmaqawa.data.database.dao

import androidx.room.*
import com.bashmaqawa.data.database.entities.WorkerAdvance
import kotlinx.coroutines.flow.Flow

/**
 * DAO for WorkerAdvance operations
 * Enhanced with settlement tracking queries
 */
@Dao
interface WorkerAdvanceDao {
    
    @Query("SELECT * FROM worker_advances WHERE worker_id = :workerId ORDER BY date DESC")
    fun getAdvancesByWorkerId(workerId: Int): Flow<List<WorkerAdvance>>
    
    @Query("SELECT * FROM worker_advances WHERE worker_id = :workerId AND is_settled = 0 ORDER BY date ASC")
    fun getUnsettledAdvancesByWorkerIdFlow(workerId: Int): Flow<List<WorkerAdvance>>
    
    @Query("SELECT * FROM worker_advances WHERE worker_id = :workerId AND is_settled = 0 ORDER BY date ASC")
    suspend fun getUnsettledAdvancesListByWorkerId(workerId: Int): List<WorkerAdvance>
    
    @Query("SELECT * FROM worker_advances WHERE worker_id = :workerId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getAdvancesByWorkerIdInRange(workerId: Int, startDate: String, endDate: String): Flow<List<WorkerAdvance>>
    
    @Query("SELECT * FROM worker_advances WHERE id = :id")
    suspend fun getAdvanceById(id: Int): WorkerAdvance?
    
    @Query("SELECT SUM(amount) FROM worker_advances WHERE worker_id = :workerId")
    suspend fun getTotalAdvancesByWorkerId(workerId: Int): Double?
    
    @Query("SELECT SUM(amount) FROM worker_advances WHERE worker_id = :workerId AND is_settled = 0")
    suspend fun getUnsettledAdvancesByWorkerId(workerId: Int): Double?
    
    @Query("SELECT SUM(amount) FROM worker_advances WHERE worker_id = :workerId AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalAdvancesByWorkerIdInRange(workerId: Int, startDate: String, endDate: String): Double?
    
    @Query("SELECT SUM(amount) FROM worker_advances WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getTotalAdvancesInRange(startDate: String, endDate: String): Double?
    
    @Query("SELECT SUM(amount) FROM worker_advances WHERE is_settled = 0")
    suspend fun getTotalUnsettledAdvances(): Double?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdvance(advance: WorkerAdvance): Long
    
    @Update
    suspend fun updateAdvance(advance: WorkerAdvance)
    
    @Query("UPDATE worker_advances SET is_settled = 1, settled_date = :settledDate, settled_amount = amount, settlement_transaction_id = :transactionId WHERE id = :id")
    suspend fun markAdvanceAsSettled(id: Int, settledDate: String, transactionId: Int)
    
    @Delete
    suspend fun deleteAdvance(advance: WorkerAdvance)
    
    @Query("DELETE FROM worker_advances WHERE id = :id")
    suspend fun deleteAdvanceById(id: Int)
}
