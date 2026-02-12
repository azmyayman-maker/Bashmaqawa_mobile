package com.bashmaqawa.data.repository

import com.bashmaqawa.data.database.dao.WorkerDao
import com.bashmaqawa.data.database.dao.WorkerAdvanceDao
import com.bashmaqawa.data.database.dao.WorkerDeductionDao
import com.bashmaqawa.data.database.dao.WorkerWithCategory
import com.bashmaqawa.data.database.entities.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Worker operations
 * مستودع عمليات العمال
 */
@Singleton
class WorkerRepository @Inject constructor(
    private val workerDao: WorkerDao,
    private val advanceDao: WorkerAdvanceDao,
    private val deductionDao: WorkerDeductionDao
) {
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    
    // Worker operations
    fun getActiveWorkers(): Flow<List<Worker>> = 
        workerDao.getWorkersByStatus(WorkerStatus.ACTIVE)
    
    fun getArchivedWorkers(): Flow<List<Worker>> = 
        workerDao.getWorkersByStatus(WorkerStatus.ARCHIVED)
    
    fun getAllWorkers(): Flow<List<Worker>> = 
        workerDao.getAllWorkers()
    
    fun getActiveWorkersWithCategory(): Flow<List<WorkerWithCategory>> =
        workerDao.getWorkersWithCategoryByStatus(WorkerStatus.ACTIVE)
    
    fun getWorkerByIdFlow(id: Int): Flow<Worker?> = 
        workerDao.getWorkerByIdFlow(id)
    
    suspend fun getWorkerById(id: Int): Worker? = 
        workerDao.getWorkerById(id)
    
    suspend fun getWorkerWithCategory(id: Int): WorkerWithCategory? =
        workerDao.getWorkerWithCategory(id)
    
    fun searchWorkers(query: String): Flow<List<Worker>> = 
        workerDao.searchWorkers(query)
    
    suspend fun getActiveWorkerCount(): Int = 
        workerDao.getWorkerCountByStatus(WorkerStatus.ACTIVE)
    
    suspend fun insertWorker(worker: Worker): Long {
        val now = LocalDateTime.now().format(dateTimeFormatter)
        return workerDao.insertWorker(worker.copy(createdAt = now, updatedAt = now))
    }
    
    suspend fun updateWorker(worker: Worker) {
        val now = LocalDateTime.now().format(dateTimeFormatter)
        workerDao.updateWorker(worker.copy(updatedAt = now))
    }
    
    suspend fun archiveWorker(id: Int) {
        val now = LocalDateTime.now().format(dateTimeFormatter)
        workerDao.updateWorkerStatus(id, WorkerStatus.ARCHIVED, now)
    }
    
    suspend fun activateWorker(id: Int) {
        val now = LocalDateTime.now().format(dateTimeFormatter)
        workerDao.updateWorkerStatus(id, WorkerStatus.ACTIVE, now)
    }
    
    suspend fun deleteWorker(id: Int) = 
        workerDao.deleteWorkerById(id)
    
    // Advance operations
    fun getAdvancesByWorkerId(workerId: Int): Flow<List<WorkerAdvance>> =
        advanceDao.getAdvancesByWorkerId(workerId)
    
    fun getAdvancesByWorkerIdInRange(workerId: Int, startDate: String, endDate: String): Flow<List<WorkerAdvance>> =
        advanceDao.getAdvancesByWorkerIdInRange(workerId, startDate, endDate)
    
    suspend fun getTotalAdvancesByWorkerId(workerId: Int): Double =
        advanceDao.getTotalAdvancesByWorkerId(workerId) ?: 0.0
    
    suspend fun getTotalAdvancesByWorkerIdInRange(workerId: Int, startDate: String, endDate: String): Double =
        advanceDao.getTotalAdvancesByWorkerIdInRange(workerId, startDate, endDate) ?: 0.0
    
    suspend fun insertAdvance(advance: WorkerAdvance): Long {
        val now = LocalDateTime.now().format(dateTimeFormatter)
        return advanceDao.insertAdvance(advance.copy(createdAt = now))
    }
    
    suspend fun updateAdvance(advance: WorkerAdvance) =
        advanceDao.updateAdvance(advance)
    
    suspend fun deleteAdvance(id: Int) =
        advanceDao.deleteAdvanceById(id)
    
    // Deduction operations
    fun getDeductionsByWorkerId(workerId: Int): Flow<List<WorkerDeduction>> =
        deductionDao.getDeductionsByWorkerId(workerId)
    
    fun getDeductionsByWorkerIdInRange(workerId: Int, startDate: String, endDate: String): Flow<List<WorkerDeduction>> =
        deductionDao.getDeductionsByWorkerIdInRange(workerId, startDate, endDate)
    
    suspend fun getTotalDeductionsByWorkerId(workerId: Int): Double =
        deductionDao.getTotalDeductionsByWorkerId(workerId) ?: 0.0
    
    suspend fun getTotalDeductionsByWorkerIdInRange(workerId: Int, startDate: String, endDate: String): Double =
        deductionDao.getTotalDeductionsByWorkerIdInRange(workerId, startDate, endDate) ?: 0.0
    
    suspend fun insertDeduction(deduction: WorkerDeduction): Long {
        val now = LocalDateTime.now().format(dateTimeFormatter)
        return deductionDao.insertDeduction(deduction.copy(createdAt = now))
    }
    
    suspend fun updateDeduction(deduction: WorkerDeduction) =
        deductionDao.updateDeduction(deduction)
    
    suspend fun deleteDeduction(id: Int) =
        deductionDao.deleteDeductionById(id)
}
