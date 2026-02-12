package com.bashmaqawa.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * فئات العمال - Worker Categories Entity
 * Categories for classifying workers by their skill/trade
 */
@Entity(tableName = "worker_categories")
data class WorkerCategory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "description")
    val description: String? = null
)

/**
 * Seed data for worker categories
 * البيانات الأولية لفئات العمال
 */
object WorkerCategorySeedData {
    val categories = listOf(
        WorkerCategory(id = 1, name = "محارة", description = "أعمال المحارة والبياض"),
        WorkerCategory(id = 2, name = "سباكة", description = "أعمال السباكة والصرف"),
        WorkerCategory(id = 3, name = "كهرباء", description = "أعمال الكهرباء والتوصيلات"),
        WorkerCategory(id = 4, name = "نجارة", description = "أعمال النجارة والأبواب"),
        WorkerCategory(id = 5, name = "حدادة", description = "أعمال الحدادة واللحام"),
        WorkerCategory(id = 6, name = "دهانات", description = "أعمال الدهانات والطلاء"),
        WorkerCategory(id = 7, name = "سيراميك", description = "أعمال السيراميك والأرضيات"),
        WorkerCategory(id = 8, name = "عامل", description = "أعمال عامة ومساعدة")
    )
}
