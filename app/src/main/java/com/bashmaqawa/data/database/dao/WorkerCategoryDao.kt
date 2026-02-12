package com.bashmaqawa.data.database.dao

import androidx.room.*
import com.bashmaqawa.data.database.entities.WorkerCategory
import kotlinx.coroutines.flow.Flow

/**
 * DAO for WorkerCategory operations
 */
@Dao
interface WorkerCategoryDao {
    
    @Query("SELECT * FROM worker_categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<WorkerCategory>>
    
    @Query("SELECT * FROM worker_categories WHERE id = :id")
    suspend fun getCategoryById(id: Int): WorkerCategory?
    
    @Query("SELECT * FROM worker_categories WHERE name = :name")
    suspend fun getCategoryByName(name: String): WorkerCategory?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: WorkerCategory): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<WorkerCategory>)
    
    @Update
    suspend fun updateCategory(category: WorkerCategory)
    
    @Delete
    suspend fun deleteCategory(category: WorkerCategory)
    
    @Query("SELECT COUNT(*) FROM worker_categories")
    suspend fun getCategoryCount(): Int
}
