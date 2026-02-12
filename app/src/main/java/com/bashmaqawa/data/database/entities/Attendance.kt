package com.bashmaqawa.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * الحضور - Attendance Entity
 * Tracks daily attendance for workers on projects
 */
@Entity(
    tableName = "attendance",
    foreignKeys = [
        ForeignKey(
            entity = Worker::class,
            parentColumns = ["id"],
            childColumns = ["worker_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Project::class,
            parentColumns = ["id"],
            childColumns = ["project_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("worker_id"),
        Index("project_id"),
        Index("date")
    ]
)
data class Attendance(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    @ColumnInfo(name = "worker_id")
    val workerId: Int,
    
    @ColumnInfo(name = "project_id")
    val projectId: Int? = null,
    
    @ColumnInfo(name = "date")
    val date: String, // Format: "2024-01-15"
    
    @ColumnInfo(name = "status")
    val status: AttendanceStatus = AttendanceStatus.PRESENT,
    
    @ColumnInfo(name = "check_in_time")
    val checkInTime: String? = null,
    
    @ColumnInfo(name = "check_out_time")
    val checkOutTime: String? = null,
    
    @ColumnInfo(name = "hours_worked")
    val hoursWorked: Double? = null,
    
    @ColumnInfo(name = "overtime_hours")
    val overtimeHours: Double? = null,
    
    @ColumnInfo(name = "expenses_json")
    val expensesJson: String? = null,  // JSON list of ExpenseItem
    
    @ColumnInfo(name = "notes")
    val notes: String? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: String? = null
)

/**
 * حالة الحضور - Attendance Status Enum
 */
enum class AttendanceStatus {
    PRESENT,    // حاضر
    ABSENT,     // غائب
    HALF_DAY,   // نصف يوم
    OVERTIME    // إضافي
}
