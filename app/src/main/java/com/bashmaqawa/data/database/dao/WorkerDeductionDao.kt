package com.bashmaqawa.data.database.dao

import androidx.room.*
import com.bashmaqawa.data.database.entities.WorkerDeduction
import kotlinx.coroutines.flow.Flow

/**
 * DAO for WorkerDeduction operations
 */
@Dao
interface WorkerDeductionDao {
    
    @Query("SELECT * FROM worker_deductions WHERE worker_id = :workerId ORDER BY date DESC")
    fun getDeductionsByWorkerId(workerId: Int): Flow<List<WorkerDeduction>>
    
    @Query("SELECT * FROM worker_deductions WHERE worker_id = :workerId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getDeductionsByWorkerIdInRange(workerId: Int, startDate: String, endDate: String): Flow<List<WorkerDeduction>>
    
    @Query("SELECT * FROM worker_deductions WHERE id = :id")
    suspend fun getDeductionById(id: Int): WorkerDeduction?
    
    @Query("SELECT SUM(amount) FROM worker_deductions WHERE worker_id = :workerId")
    suspend fun getTotalDeductionsByWorkerId(workerId: Int): Double?
    
    @Query("SELECT SUM(amount) FROM worker_deductions WHERE worker_id = :workerId AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalDeductionsByWorkerIdInRange(workerId: Int, startDate: String, endDate: String): Double?
    
    @Query("SELECT SUM(amount) FROM worker_deductions WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getTotalDeductionsInRange(startDate: String, endDate: String): Double?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeduction(deduction: WorkerDeduction): Long
    
    @Update
    suspend fun updateDeduction(deduction: WorkerDeduction)
    
    @Delete
    suspend fun deleteDeduction(deduction: WorkerDeduction)
    
    @Query("DELETE FROM worker_deductions WHERE id = :id")
    suspend fun deleteDeductionById(id: Int)
}
