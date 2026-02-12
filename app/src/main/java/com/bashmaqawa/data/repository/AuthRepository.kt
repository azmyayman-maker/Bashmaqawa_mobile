package com.bashmaqawa.data.repository

import com.bashmaqawa.data.database.dao.UserDao
import com.bashmaqawa.data.database.entities.User
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for User authentication operations
 * مستودع عمليات المستخدمين
 */
@Singleton
class AuthRepository @Inject constructor(
    private val userDao: UserDao
) {
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    
    suspend fun authenticate(username: String, password: String): User? {
        // In production, password should be hashed before comparison
        val user = userDao.authenticate(username, password)
        user?.let {
            val now = LocalDateTime.now().format(dateTimeFormatter)
            userDao.updateLastLogin(it.id, now)
        }
        return user
    }
    
    suspend fun getUserByUsername(username: String): User? =
        userDao.getUserByUsername(username)
    
    suspend fun getUserById(id: Int): User? =
        userDao.getUserById(id)
    
    fun getAllUsers(): Flow<List<User>> =
        userDao.getAllActiveUsers()
    
    suspend fun updatePassword(userId: Int, newPassword: String) {
        // In production, password should be hashed
        userDao.updatePassword(userId, newPassword)
    }
    
    suspend fun insertUser(user: User): Long {
        val now = LocalDateTime.now().format(dateTimeFormatter)
        return userDao.insertUser(user.copy(createdAt = now))
    }
    
    suspend fun updateUser(user: User) =
        userDao.updateUser(user)
    
    suspend fun deleteUser(id: Int) =
        userDao.deleteUserById(id)
    
    suspend fun getUserCount(): Int =
        userDao.getUserCount()
}
