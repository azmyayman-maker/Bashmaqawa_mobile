package com.bashmaqawa.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * ذاكرة الذكاء الاصطناعي - AI Memory Entity
 * Stores learned query patterns for the AI assistant
 */
@Entity(tableName = "ai_memory")
data class AIMemory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    @ColumnInfo(name = "user_question")
    val userQuestion: String,
    
    @ColumnInfo(name = "sql_query")
    val sqlQuery: String,
    
    @ColumnInfo(name = "success_count")
    val successCount: Int = 1,
    
    @ColumnInfo(name = "last_used")
    val lastUsed: String,
    
    @ColumnInfo(name = "embedding")
    val embedding: String? = null, // JSON encoded vector
    
    @ColumnInfo(name = "response_template")
    val responseTemplate: String? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: String? = null
)
