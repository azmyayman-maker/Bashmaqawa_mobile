package com.bashmaqawa.data.database.dao

import androidx.room.*
import com.bashmaqawa.data.database.entities.Attendance
import com.bashmaqawa.data.database.entities.AttendanceStatus
import kotlinx.coroutines.flow.Flow

/**
 * Attendance with worker details for display
 */
data class AttendanceWithWorker(
    val id: Int,
    val workerId: Int,
    val workerName: String,
    val workerDailyRate: Double?,
    val projectId: Int?,
    val projectName: String?,
    val date: String,
    val status: String,
    val checkInTime: String?,
    val checkOutTime: String?,
    val hoursWorked: Double?,
    val overtimeHours: Double?,
    val expensesJson: String?,
    val notes: String?
)

/**
 * DAO for Attendance operations
 */
@Dao
interface AttendanceDao {
    
    @Query("SELECT * FROM attendance WHERE date = :date ORDER BY worker_id ASC")
    fun getAttendanceByDate(date: String): Flow<List<Attendance>>
    
    @Query("""
        SELECT a.id, a.worker_id as workerId, w.name as workerName, w.daily_rate as workerDailyRate,
               a.project_id as projectId, p.name as projectName, a.date, a.status,
               a.check_in_time as checkInTime, a.check_out_time as checkOutTime,
               a.hours_worked as hoursWorked, a.overtime_hours as overtimeHours,
               a.expenses_json as expensesJson, a.notes
        FROM attendance a
        INNER JOIN workers w ON a.worker_id = w.id
        LEFT JOIN projects p ON a.project_id = p.id
        WHERE a.date = :date
        ORDER BY w.name ASC
    """)
    fun getAttendanceWithWorkerByDate(date: String): Flow<List<AttendanceWithWorker>>
    
    @Query("SELECT * FROM attendance WHERE worker_id = :workerId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getWorkerAttendanceInRange(workerId: Int, startDate: String, endDate: String): Flow<List<Attendance>>
    
    @Query("""
        SELECT a.id, a.worker_id as workerId, w.name as workerName, w.daily_rate as workerDailyRate,
               a.project_id as projectId, p.name as projectName, a.date, a.status,
               a.check_in_time as checkInTime, a.check_out_time as checkOutTime,
               a.hours_worked as hoursWorked, a.overtime_hours as overtimeHours,
               a.expenses_json as expensesJson, a.notes
        FROM attendance a
        INNER JOIN workers w ON a.worker_id = w.id
        LEFT JOIN projects p ON a.project_id = p.id
        WHERE a.worker_id = :workerId AND a.date BETWEEN :startDate AND :endDate
        ORDER BY a.date DESC
    """)
    fun getWorkerAttendanceWithDetailsInRange(
        workerId: Int, 
        startDate: String, 
        endDate: String
    ): Flow<List<AttendanceWithWorker>>
    
    @Query("SELECT * FROM attendance WHERE project_id = :projectId AND date BETWEEN :startDate AND :endDate")
    fun getProjectAttendanceInRange(projectId: Int, startDate: String, endDate: String): Flow<List<Attendance>>
    
    @Query("SELECT * FROM attendance WHERE worker_id = :workerId AND date = :date")
    suspend fun getAttendanceByWorkerAndDate(workerId: Int, date: String): Attendance?
    
    @Query("SELECT COUNT(*) FROM attendance WHERE worker_id = :workerId AND status = :status AND date BETWEEN :startDate AND :endDate")
    suspend fun countWorkerAttendanceByStatus(
        workerId: Int,
        status: AttendanceStatus,
        startDate: String,
        endDate: String
    ): Int
    // Note: Expense totals now need to be calculated in code by parsing expensesJson
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceList(attendanceList: List<Attendance>)
    
    @Update
    suspend fun updateAttendance(attendance: Attendance)
    
    @Query("UPDATE attendance SET status = :status WHERE id = :id")
    suspend fun updateAttendanceStatus(id: Int, status: AttendanceStatus)
    
    @Delete
    suspend fun deleteAttendance(attendance: Attendance)
    
    @Query("DELETE FROM attendance WHERE id = :id")
    suspend fun deleteAttendanceById(id: Int)
    
    @Query("DELETE FROM attendance WHERE date = :date")
    suspend fun deleteAttendanceByDate(date: String)
}
