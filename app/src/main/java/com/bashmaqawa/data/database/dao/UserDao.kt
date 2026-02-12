package com.bashmaqawa.data.database.dao

import androidx.room.*
import com.bashmaqawa.data.database.entities.User
import kotlinx.coroutines.flow.Flow

/**
 * DAO for User operations
 */
@Dao
interface UserDao {
    
    @Query("SELECT * FROM users WHERE is_active = 1 ORDER BY username ASC")
    fun getAllActiveUsers(): Flow<List<User>>
    
    @Query("SELECT * FROM users ORDER BY username ASC")
    fun getAllUsers(): Flow<List<User>>
    
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Int): User?
    
    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): User?
    
    @Query("SELECT * FROM users WHERE username = :username AND password_hash = :passwordHash")
    suspend fun authenticate(username: String, passwordHash: String): User?
    
    @Query("UPDATE users SET last_login = :lastLogin WHERE id = :id")
    suspend fun updateLastLogin(id: Int, lastLogin: String)
    
    @Query("UPDATE users SET password_hash = :newPasswordHash WHERE id = :id")
    suspend fun updatePassword(id: Int, newPasswordHash: String)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long
    
    @Update
    suspend fun updateUser(user: User)
    
    @Delete
    suspend fun deleteUser(user: User)
    
    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUserById(id: Int)
    
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}
