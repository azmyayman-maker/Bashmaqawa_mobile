package com.bashmaqawa.data.repository

import com.bashmaqawa.data.database.dao.AttendanceDao
import com.bashmaqawa.data.database.dao.AttendanceWithWorker
import com.bashmaqawa.data.database.entities.Attendance
import com.bashmaqawa.data.database.entities.AttendanceStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Attendance operations
 * مستودع عمليات الحضور
 */
@Singleton
class AttendanceRepository @Inject constructor(
    private val attendanceDao: AttendanceDao
) {
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    
    fun getAttendanceByDate(date: String): Flow<List<Attendance>> =
        attendanceDao.getAttendanceByDate(date)
    
    fun getAttendanceWithWorkerByDate(date: String): Flow<List<AttendanceWithWorker>> =
        attendanceDao.getAttendanceWithWorkerByDate(date)
    
    fun getWorkerAttendanceInRange(workerId: Int, startDate: String, endDate: String): Flow<List<Attendance>> =
        attendanceDao.getWorkerAttendanceInRange(workerId, startDate, endDate)
    
    fun getWorkerAttendanceWithDetailsInRange(
        workerId: Int, 
        startDate: String, 
        endDate: String
    ): Flow<List<AttendanceWithWorker>> =
        attendanceDao.getWorkerAttendanceWithDetailsInRange(workerId, startDate, endDate)
    
    fun getProjectAttendanceInRange(projectId: Int, startDate: String, endDate: String): Flow<List<Attendance>> =
        attendanceDao.getProjectAttendanceInRange(projectId, startDate, endDate)
    
    suspend fun getAttendanceByWorkerAndDate(workerId: Int, date: String): Attendance? =
        attendanceDao.getAttendanceByWorkerAndDate(workerId, date)
    
    suspend fun countWorkerPresentDays(workerId: Int, startDate: String, endDate: String): Int =
        attendanceDao.countWorkerAttendanceByStatus(workerId, AttendanceStatus.PRESENT, startDate, endDate)
    
    suspend fun countWorkerHalfDays(workerId: Int, startDate: String, endDate: String): Int =
        attendanceDao.countWorkerAttendanceByStatus(workerId, AttendanceStatus.HALF_DAY, startDate, endDate)
    
    suspend fun countWorkerOvertimeDays(workerId: Int, startDate: String, endDate: String): Int =
        attendanceDao.countWorkerAttendanceByStatus(workerId, AttendanceStatus.OVERTIME, startDate, endDate)
    
    // Note: Expense totals are now stored as JSON and should be parsed in the presentation layer
    
    suspend fun insertAttendance(attendance: Attendance): Long {
        val now = LocalDateTime.now().format(dateTimeFormatter)
        return attendanceDao.insertAttendance(attendance.copy(createdAt = now))
    }
    
    suspend fun insertBatchAttendance(attendanceList: List<Attendance>) {
        val now = LocalDateTime.now().format(dateTimeFormatter)
        val timestampedList = attendanceList.map { it.copy(createdAt = now) }
        attendanceDao.insertAttendanceList(timestampedList)
    }
    
    suspend fun updateAttendance(attendance: Attendance) =
        attendanceDao.updateAttendance(attendance)
    
    suspend fun updateAttendanceStatus(id: Int, status: AttendanceStatus) =
        attendanceDao.updateAttendanceStatus(id, status)
    
    suspend fun deleteAttendance(id: Int) =
        attendanceDao.deleteAttendanceById(id)
    
    suspend fun deleteAttendanceByDate(date: String) =
        attendanceDao.deleteAttendanceByDate(date)
}
