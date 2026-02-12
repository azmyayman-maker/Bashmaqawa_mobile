package com.bashmaqawa.data.database

import androidx.room.TypeConverter
import com.bashmaqawa.data.database.entities.*

/**
 * Type converters for Room database enums
 */
class Converters {
    
    // ProjectStatus
    @TypeConverter
    fun fromProjectStatus(status: ProjectStatus): String = status.name
    
    @TypeConverter
    fun toProjectStatus(value: String): ProjectStatus = ProjectStatus.valueOf(value)
    
    // SkillLevel
    @TypeConverter
    fun fromSkillLevel(level: SkillLevel): String = level.name
    
    @TypeConverter
    fun toSkillLevel(value: String): SkillLevel = SkillLevel.valueOf(value)
    
    // WorkerStatus
    @TypeConverter
    fun fromWorkerStatus(status: WorkerStatus): String = status.name
    
    @TypeConverter
    fun toWorkerStatus(value: String): WorkerStatus = WorkerStatus.valueOf(value)
    
    // AttendanceStatus
    @TypeConverter
    fun fromAttendanceStatus(status: AttendanceStatus): String = status.name
    
    @TypeConverter
    fun toAttendanceStatus(value: String): AttendanceStatus = AttendanceStatus.valueOf(value)
    
    // AccountType
    @TypeConverter
    fun fromAccountType(type: AccountType?): String? = type?.name
    
    @TypeConverter
    fun toAccountType(value: String?): AccountType? = value?.let { AccountType.valueOf(it) }
    
    // AccountCategory
    @TypeConverter
    fun fromAccountCategory(category: AccountCategory): String = category.name
    
    @TypeConverter
    fun toAccountCategory(value: String): AccountCategory = AccountCategory.valueOf(value)
    
    // TransactionType
    @TypeConverter
    fun fromTransactionType(type: TransactionType?): String? = type?.name
    
    @TypeConverter
    fun toTransactionType(value: String?): TransactionType? = value?.let { TransactionType.valueOf(it) }
    
    // UserRole
    @TypeConverter
    fun fromUserRole(role: UserRole): String = role.name
    
    @TypeConverter
    fun toUserRole(value: String): UserRole = UserRole.valueOf(value)
}
