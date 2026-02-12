package com.bashmaqawa.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * المشاريع - Projects Entity
 * Represents a construction project
 */
@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "client_name")
    val clientName: String? = null,
    
    @ColumnInfo(name = "location")
    val location: String? = null,
    
    @ColumnInfo(name = "area_sqm")
    val areaSqm: Double? = null,
    
    @ColumnInfo(name = "price_per_meter")
    val pricePerMeter: Double? = null,
    
    @ColumnInfo(name = "start_date")
    val startDate: String? = null,
    
    @ColumnInfo(name = "end_date")
    val endDate: String? = null,
    
    @ColumnInfo(name = "status")
    val status: ProjectStatus = ProjectStatus.PENDING,
    
    @ColumnInfo(name = "notes")
    val notes: String? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: String? = null,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: String? = null
)

/**
 * حالات المشروع - Project Status Enum
 */
enum class ProjectStatus {
    PENDING,    // قيد الانتظار
    ACTIVE,     // جاري
    COMPLETED,  // مكتمل
    PAUSED      // متوقف
}
