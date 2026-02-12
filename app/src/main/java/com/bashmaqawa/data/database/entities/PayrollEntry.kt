package com.bashmaqawa.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * كشف الأجور - Payroll Entry Entity
 * Tracks wage accruals and payments for workers.
 * Supports the wage calculation formula:
 * GrossWage = (DailyRate × DaysPresent) + (OvertimeHours × OvertimeRate)
 * NetWage = GrossWage - Deductions - AdvancesDeducted
 */
@Entity(
    tableName = "payroll_entries",
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
        ),
        ForeignKey(
            entity = Transaction::class,
            parentColumns = ["id"],
            childColumns = ["transaction_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("worker_id"),
        Index("project_id"),
        Index("transaction_id"),
        Index("period_start", "period_end"),
        Index("status")
    ]
)
data class PayrollEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    @ColumnInfo(name = "worker_id")
    val workerId: Int,
    
    @ColumnInfo(name = "period_start")
    val periodStart: String,
    
    @ColumnInfo(name = "period_end")
    val periodEnd: String,
    
    @ColumnInfo(name = "days_present")
    val daysPresent: Double = 0.0,
    
    @ColumnInfo(name = "half_days")
    val halfDays: Double = 0.0,
    
    @ColumnInfo(name = "overtime_hours")
    val overtimeHours: Double = 0.0,
    
    @ColumnInfo(name = "daily_rate")
    val dailyRate: Double = 0.0,
    
    @ColumnInfo(name = "overtime_rate")
    val overtimeRate: Double = 0.0,
    
    @ColumnInfo(name = "gross_wage")
    val grossWage: Double = 0.0,
    
    @ColumnInfo(name = "deductions")
    val deductions: Double = 0.0,
    
    @ColumnInfo(name = "advances_deducted")
    val advancesDeducted: Double = 0.0,
    
    @ColumnInfo(name = "net_wage")
    val netWage: Double = 0.0,
    
    @ColumnInfo(name = "status")
    val status: PayrollStatus = PayrollStatus.DRAFT,
    
    @ColumnInfo(name = "project_id")
    val projectId: Int? = null,
    
    @ColumnInfo(name = "transaction_id")
    val transactionId: Int? = null,
    
    @ColumnInfo(name = "notes")
    val notes: String? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: String? = null,
    
    @ColumnInfo(name = "approved_at")
    val approvedAt: String? = null,
    
    @ColumnInfo(name = "paid_at")
    val paidAt: String? = null
)

/**
 * حالة كشف الأجور - Payroll Status
 */
enum class PayrollStatus {
    DRAFT,      // مسودة - awaiting approval
    APPROVED,   // معتمد - approved, awaiting payment
    PAID,       // مدفوع - payment completed
    CANCELLED   // ملغي - cancelled before payment
}

/**
 * ملخص كشف الأجور - Payroll Summary
 * Used for displaying payroll lists
 */
data class PayrollSummary(
    val id: Int,
    val workerId: Int,
    val workerName: String,
    val periodStart: String,
    val periodEnd: String,
    val grossWage: Double,
    val netWage: Double,
    val status: PayrollStatus
)
