package com.bashmaqawa.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * العمال - Workers Entity
 * Represents a worker in the system
 */
@Entity(
    tableName = "workers",
    foreignKeys = [
        ForeignKey(
            entity = WorkerCategory::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("category_id")]
)
data class Worker(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "role")
    val role: String? = null, // صنايعي / عامل
    
    @ColumnInfo(name = "daily_rate")
    val dailyRate: Double? = null,
    
    @ColumnInfo(name = "phone")
    val phone: String? = null,
    
    @ColumnInfo(name = "whatsapp_phone")
    val whatsappPhone: String? = null,
    
    @ColumnInfo(name = "national_id")
    val nationalId: String? = null,
    
    @ColumnInfo(name = "skill_level")
    val skillLevel: SkillLevel = SkillLevel.HELPER,
    
    @ColumnInfo(name = "status")
    val status: WorkerStatus = WorkerStatus.ACTIVE,
    
    @ColumnInfo(name = "category_id")
    val categoryId: Int? = null,
    
    @ColumnInfo(name = "notes")
    val notes: String? = null,
    
    @ColumnInfo(name = "photo_uri")
    val photoUri: String? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: String? = null,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: String? = null
)

/**
 * مستوى المهارة - Skill Level Enum
 */
enum class SkillLevel {
    HELPER,     // عامل
    SKILLED,    // صنايعي
    MASTER      // أسطى
}

/**
 * حالة العامل - Worker Status Enum
 */
enum class WorkerStatus {
    ACTIVE,     // نشط
    ARCHIVED    // مؤرشف
}
