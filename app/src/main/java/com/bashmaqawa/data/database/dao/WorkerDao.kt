package com.bashmaqawa.data.database.dao

import androidx.room.*
import com.bashmaqawa.data.database.entities.Worker
import com.bashmaqawa.data.database.entities.WorkerStatus
import kotlinx.coroutines.flow.Flow

/**
 * Worker with category name for display
 */
data class WorkerWithCategory(
    val id: Int,
    val name: String,
    val role: String?,
    val dailyRate: Double?,
    val phone: String?,
    val whatsappPhone: String?,
    val nationalId: String?,
    val skillLevel: String,
    val status: String,
    val categoryId: Int?,
    val categoryName: String?,
    val notes: String?,
    val photoUri: String?,
    val createdAt: String?,
    val updatedAt: String?
)

/**
 * DAO for Worker operations
 */
@Dao
interface WorkerDao {
    
    @Query("SELECT * FROM workers WHERE status = :status ORDER BY name ASC")
    fun getWorkersByStatus(status: WorkerStatus): Flow<List<Worker>>
    
    @Query("SELECT * FROM workers ORDER BY name ASC")
    fun getAllWorkers(): Flow<List<Worker>>

    @Query("SELECT * FROM workers ORDER BY name ASC")
    suspend fun getAllWorkersList(): List<Worker>
    
    @Query("""
        SELECT w.id, w.name, w.role, w.daily_rate as dailyRate, w.phone, 
               w.whatsapp_phone as whatsappPhone, w.national_id as nationalId,
               w.skill_level as skillLevel, w.status, w.category_id as categoryId,
               c.name as categoryName, w.notes, w.photo_uri as photoUri,
               w.created_at as createdAt, w.updated_at as updatedAt
        FROM workers w
        LEFT JOIN worker_categories c ON w.category_id = c.id
        WHERE w.status = :status
        ORDER BY w.name ASC
    """)
    fun getWorkersWithCategoryByStatus(status: WorkerStatus): Flow<List<WorkerWithCategory>>
    
    @Query("SELECT * FROM workers WHERE id = :id")
    suspend fun getWorkerById(id: Int): Worker?
    
    @Query("SELECT * FROM workers WHERE id = :id")
    fun getWorkerByIdFlow(id: Int): Flow<Worker?>
    
    @Query("""
        SELECT w.id, w.name, w.role, w.daily_rate as dailyRate, w.phone, 
               w.whatsapp_phone as whatsappPhone, w.national_id as nationalId,
               w.skill_level as skillLevel, w.status, w.category_id as categoryId,
               c.name as categoryName, w.notes, w.photo_uri as photoUri,
               w.created_at as createdAt, w.updated_at as updatedAt
        FROM workers w
        LEFT JOIN worker_categories c ON w.category_id = c.id
        WHERE w.id = :id
    """)
    suspend fun getWorkerWithCategory(id: Int): WorkerWithCategory?
    
    @Query("SELECT * FROM workers WHERE category_id = :categoryId AND status = :status ORDER BY name ASC")
    fun getWorkersByCategoryAndStatus(categoryId: Int, status: WorkerStatus): Flow<List<Worker>>
    
    @Query("SELECT * FROM workers WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%'")
    fun searchWorkers(query: String): Flow<List<Worker>>
    
    @Query("SELECT COUNT(*) FROM workers WHERE status = :status")
    suspend fun getWorkerCountByStatus(status: WorkerStatus): Int
    
    @Query("SELECT COUNT(*) FROM workers")
    suspend fun getTotalWorkerCount(): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorker(worker: Worker): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkers(workers: List<Worker>)
    
    @Update
    suspend fun updateWorker(worker: Worker)
    
    @Query("UPDATE workers SET status = :status, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateWorkerStatus(id: Int, status: WorkerStatus, updatedAt: String)
    
    @Delete
    suspend fun deleteWorker(worker: Worker)
    
    @Query("DELETE FROM workers WHERE id = :id")
    suspend fun deleteWorkerById(id: Int)
    
    @Query("""
        SELECT SUM(d.amount) FROM worker_deductions d
        WHERE d.worker_id = :workerId AND d.date BETWEEN :startDate AND :endDate
    """)
    suspend fun getTotalDeductionsByWorkerInRange(workerId: Int, startDate: String, endDate: String): Double?
}
