package com.bashmaqawa.data.database.dao

import androidx.room.*
import com.bashmaqawa.data.database.entities.PayrollEntry
import com.bashmaqawa.data.database.entities.PayrollStatus
import com.bashmaqawa.data.database.entities.PayrollSummary
import kotlinx.coroutines.flow.Flow

/**
 * Payroll with worker details for display
 */
data class PayrollWithWorker(
    val id: Int,
    val workerId: Int,
    val workerName: String,
    val periodStart: String,
    val periodEnd: String,
    val daysPresent: Double,
    val halfDays: Double,
    val overtimeHours: Double,
    val dailyRate: Double,
    val overtimeRate: Double,
    val grossWage: Double,
    val deductions: Double,
    val advancesDeducted: Double,
    val netWage: Double,
    val status: String,
    val projectId: Int?,
    val projectName: String?,
    val transactionId: Int?,
    val notes: String?,
    val createdAt: String?,
    val approvedAt: String?,
    val paidAt: String?
)

/**
 * DAO for PayrollEntry operations
 * Handles wage calculations and payroll management
 */
@Dao
interface PayrollDao {
    
    @Query("SELECT * FROM payroll_entries ORDER BY period_end DESC, id DESC")
    fun getAllPayrollEntries(): Flow<List<PayrollEntry>>
    
    @Query("""
        SELECT p.id, p.worker_id as workerId, w.name as workerName,
               p.period_start as periodStart, p.period_end as periodEnd,
               p.days_present as daysPresent, p.half_days as halfDays,
               p.overtime_hours as overtimeHours, p.daily_rate as dailyRate,
               p.overtime_rate as overtimeRate, p.gross_wage as grossWage,
               p.deductions, p.advances_deducted as advancesDeducted,
               p.net_wage as netWage, p.status, p.project_id as projectId,
               pr.name as projectName, p.transaction_id as transactionId,
               p.notes, p.created_at as createdAt, p.approved_at as approvedAt,
               p.paid_at as paidAt
        FROM payroll_entries p
        INNER JOIN workers w ON p.worker_id = w.id
        LEFT JOIN projects pr ON p.project_id = pr.id
        ORDER BY p.period_end DESC, p.id DESC
    """)
    fun getAllPayrollEntriesWithWorker(): Flow<List<PayrollWithWorker>>
    
    @Query("SELECT * FROM payroll_entries WHERE worker_id = :workerId ORDER BY period_end DESC")
    fun getPayrollEntriesByWorkerId(workerId: Int): Flow<List<PayrollEntry>>
    
    @Query("""
        SELECT p.id, p.worker_id as workerId, w.name as workerName,
               p.period_start as periodStart, p.period_end as periodEnd,
               p.days_present as daysPresent, p.half_days as halfDays,
               p.overtime_hours as overtimeHours, p.daily_rate as dailyRate,
               p.overtime_rate as overtimeRate, p.gross_wage as grossWage,
               p.deductions, p.advances_deducted as advancesDeducted,
               p.net_wage as netWage, p.status, p.project_id as projectId,
               pr.name as projectName, p.transaction_id as transactionId,
               p.notes, p.created_at as createdAt, p.approved_at as approvedAt,
               p.paid_at as paidAt
        FROM payroll_entries p
        INNER JOIN workers w ON p.worker_id = w.id
        LEFT JOIN projects pr ON p.project_id = pr.id
        WHERE p.worker_id = :workerId
        ORDER BY p.period_end DESC
    """)
    fun getPayrollEntriesWithWorkerByWorkerId(workerId: Int): Flow<List<PayrollWithWorker>>
    
    @Query("SELECT * FROM payroll_entries WHERE status = :status ORDER BY period_end DESC")
    fun getPayrollEntriesByStatus(status: PayrollStatus): Flow<List<PayrollEntry>>
    
    @Query("SELECT * FROM payroll_entries WHERE project_id = :projectId ORDER BY period_end DESC")
    fun getPayrollEntriesByProjectId(projectId: Int): Flow<List<PayrollEntry>>
    
    @Query("SELECT * FROM payroll_entries WHERE period_start >= :startDate AND period_end <= :endDate ORDER BY period_end DESC")
    fun getPayrollEntriesInRange(startDate: String, endDate: String): Flow<List<PayrollEntry>>
    
    @Query("SELECT * FROM payroll_entries WHERE id = :id")
    suspend fun getPayrollEntryById(id: Int): PayrollEntry?
    
    @Query("SELECT * FROM payroll_entries WHERE worker_id = :workerId AND period_start = :periodStart AND period_end = :periodEnd")
    suspend fun getPayrollEntryForWorkerAndPeriod(workerId: Int, periodStart: String, periodEnd: String): PayrollEntry?
    
    @Query("SELECT SUM(net_wage) FROM payroll_entries WHERE status = :status")
    suspend fun getTotalByStatus(status: PayrollStatus): Double?
    
    @Query("SELECT SUM(net_wage) FROM payroll_entries WHERE status = :status AND project_id = :projectId")
    suspend fun getTotalByStatusAndProject(status: PayrollStatus, projectId: Int): Double?
    
    @Query("SELECT SUM(net_wage) FROM payroll_entries WHERE worker_id = :workerId AND period_start >= :startDate AND period_end <= :endDate")
    suspend fun getTotalWagesByWorkerInRange(workerId: Int, startDate: String, endDate: String): Double?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayrollEntry(payrollEntry: PayrollEntry): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayrollEntries(payrollEntries: List<PayrollEntry>)
    
    @Update
    suspend fun updatePayrollEntry(payrollEntry: PayrollEntry)
    
    @Query("UPDATE payroll_entries SET status = :status, approved_at = :approvedAt WHERE id = :id")
    suspend fun updatePayrollStatus(id: Int, status: PayrollStatus, approvedAt: String?)
    
    @Query("UPDATE payroll_entries SET status = :status, transaction_id = :transactionId, paid_at = :paidAt WHERE id = :id")
    suspend fun markPayrollAsPaid(id: Int, status: PayrollStatus, transactionId: Int?, paidAt: String?)
    
    @Delete
    suspend fun deletePayrollEntry(payrollEntry: PayrollEntry)
    
    @Query("DELETE FROM payroll_entries WHERE id = :id")
    suspend fun deletePayrollEntryById(id: Int)
    
    @Query("SELECT COUNT(*) FROM payroll_entries")
    suspend fun getTotalPayrollEntryCount(): Int
    
    @Query("SELECT COUNT(*) FROM payroll_entries WHERE status = :status")
    suspend fun getPayrollCountByStatus(status: PayrollStatus): Int
}
