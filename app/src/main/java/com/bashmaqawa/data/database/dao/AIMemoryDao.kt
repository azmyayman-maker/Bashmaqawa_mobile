package com.bashmaqawa.data.database.dao

import androidx.room.*
import com.bashmaqawa.data.database.entities.AIMemory
import kotlinx.coroutines.flow.Flow

/**
 * DAO for AIMemory operations
 */
@Dao
interface AIMemoryDao {
    
    @Query("SELECT * FROM ai_memory ORDER BY success_count DESC, last_used DESC")
    fun getAllMemories(): Flow<List<AIMemory>>
    
    @Query("SELECT * FROM ai_memory WHERE user_question LIKE '%' || :query || '%' ORDER BY success_count DESC LIMIT 10")
    suspend fun searchMemories(query: String): List<AIMemory>
    
    @Query("SELECT * FROM ai_memory WHERE id = :id")
    suspend fun getMemoryById(id: Int): AIMemory?
    
    @Query("SELECT * FROM ai_memory ORDER BY success_count DESC LIMIT :limit")
    suspend fun getTopMemories(limit: Int): List<AIMemory>
    
    @Query("UPDATE ai_memory SET success_count = success_count + 1, last_used = :lastUsed WHERE id = :id")
    suspend fun incrementSuccessCount(id: Int, lastUsed: String)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: AIMemory): Long
    
    @Update
    suspend fun updateMemory(memory: AIMemory)
    
    @Delete
    suspend fun deleteMemory(memory: AIMemory)
    
    @Query("DELETE FROM ai_memory WHERE id = :id")
    suspend fun deleteMemoryById(id: Int)
    
    @Query("DELETE FROM ai_memory")
    suspend fun clearAllMemories()
}
