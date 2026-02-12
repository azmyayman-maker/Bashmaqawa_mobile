package com.bashmaqawa.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * المستخدمين - User Entity
 * Represents system users for authentication
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    @ColumnInfo(name = "username")
    val username: String,
    
    @ColumnInfo(name = "password_hash")
    val passwordHash: String,
    
    @ColumnInfo(name = "display_name")
    val displayName: String? = null,
    
    @ColumnInfo(name = "role")
    val role: UserRole = UserRole.USER,
    
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,
    
    @ColumnInfo(name = "last_login")
    val lastLogin: String? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: String? = null
)

/**
 * دور المستخدم - User Role Enum
 */
enum class UserRole {
    ADMIN,  // مدير
    USER    // مستخدم
}

/**
 * Default admin user for initial setup
 */
object DefaultUser {
    val admin = User(
        id = 1,
        username = "admin",
        passwordHash = "1234", // In production, use proper hashing
        displayName = "المدير",
        role = UserRole.ADMIN
    )
}
