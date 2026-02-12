package com.bashmaqawa.data.database.dao

import androidx.room.*
import com.bashmaqawa.data.database.entities.Project
import com.bashmaqawa.data.database.entities.ProjectStatus
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Project operations
 */
@Dao
interface ProjectDao {
    
    @Query("SELECT * FROM projects ORDER BY created_at DESC")
    fun getAllProjects(): Flow<List<Project>>

    @Query("SELECT * FROM projects ORDER BY created_at DESC")
    suspend fun getAllProjectsList(): List<Project>
    
    @Query("SELECT * FROM projects WHERE status = :status ORDER BY created_at DESC")
    fun getProjectsByStatus(status: ProjectStatus): Flow<List<Project>>
    
    @Query("SELECT * FROM projects WHERE status IN (:statuses) ORDER BY created_at DESC")
    fun getProjectsByStatuses(statuses: List<ProjectStatus>): Flow<List<Project>>
    
    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectById(id: Int): Project?
    
    @Query("SELECT * FROM projects WHERE id = :id")
    fun getProjectByIdFlow(id: Int): Flow<Project?>
    
    @Query("SELECT * FROM projects WHERE name LIKE '%' || :query || '%' OR client_name LIKE '%' || :query || '%' OR location LIKE '%' || :query || '%'")
    fun searchProjects(query: String): Flow<List<Project>>
    
    @Query("SELECT COUNT(*) FROM projects WHERE status = :status")
    suspend fun getProjectCountByStatus(status: ProjectStatus): Int
    
    @Query("SELECT COUNT(*) FROM projects")
    suspend fun getTotalProjectCount(): Int
    
    @Query("SELECT SUM(area_sqm * price_per_meter) FROM projects WHERE status = :status")
    suspend fun getTotalValueByStatus(status: ProjectStatus): Double?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: Project): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjects(projects: List<Project>)
    
    @Update
    suspend fun updateProject(project: Project)
    
    @Delete
    suspend fun deleteProject(project: Project)
    
    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProjectById(id: Int)
}
