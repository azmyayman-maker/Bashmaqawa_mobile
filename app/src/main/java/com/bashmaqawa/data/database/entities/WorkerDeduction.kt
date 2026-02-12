package com.bashmaqawa.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * خصومات العمال - Worker Deductions Entity
 * Represents salary deductions for workers
 */
@Entity(
    tableName = "worker_deductions",
    foreignKeys = [
        ForeignKey(
            entity = Worker::class,
            parentColumns = ["id"],
            childColumns = ["worker_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("worker_id")]
)
data class WorkerDeduction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    @ColumnInfo(name = "worker_id")
    val workerId: Int,
    
    @ColumnInfo(name = "amount")
    val amount: Double,
    
    @ColumnInfo(name = "reason")
    val reason: String? = null,
    
    @ColumnInfo(name = "date")
    val date: String, // Format: "2024-01-15"
    
    @ColumnInfo(name = "notes")
    val notes: String? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: String? = null
)
