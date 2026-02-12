package com.bashmaqawa.data.repository

import com.bashmaqawa.data.database.dao.ProjectDao
import com.bashmaqawa.data.database.dao.CostCenterDao
import com.bashmaqawa.data.database.entities.Project
import com.bashmaqawa.data.database.entities.ProjectStatus
import com.bashmaqawa.data.database.entities.CostCenter
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Project operations
 * مستودع عمليات المشاريع
 */
@Singleton
class ProjectRepository @Inject constructor(
    private val projectDao: ProjectDao,
    private val costCenterDao: CostCenterDao
) {
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    
    // Project operations
    fun getAllProjects(): Flow<List<Project>> = 
        projectDao.getAllProjects()
    
    fun getProjectsByStatus(status: ProjectStatus): Flow<List<Project>> = 
        projectDao.getProjectsByStatus(status)
    
    fun getActiveProjects(): Flow<List<Project>> = 
        projectDao.getProjectsByStatus(ProjectStatus.ACTIVE)
    
    fun getProjectByIdFlow(id: Int): Flow<Project?> = 
        projectDao.getProjectByIdFlow(id)
    
    suspend fun getProjectById(id: Int): Project? = 
        projectDao.getProjectById(id)
    
    fun searchProjects(query: String): Flow<List<Project>> = 
        projectDao.searchProjects(query)
    
    suspend fun getActiveProjectCount(): Int = 
        projectDao.getProjectCountByStatus(ProjectStatus.ACTIVE)
    
    suspend fun getTotalProjectCount(): Int = 
        projectDao.getTotalProjectCount()
    
    suspend fun insertProject(project: Project): Long {
        val now = LocalDateTime.now().format(dateTimeFormatter)
        return projectDao.insertProject(project.copy(createdAt = now, updatedAt = now))
    }
    
    suspend fun updateProject(project: Project) {
        val now = LocalDateTime.now().format(dateTimeFormatter)
        projectDao.updateProject(project.copy(updatedAt = now))
    }
    
    suspend fun updateProjectStatus(id: Int, status: ProjectStatus) {
        val project = projectDao.getProjectById(id)
        project?.let {
            val now = LocalDateTime.now().format(dateTimeFormatter)
            projectDao.updateProject(it.copy(status = status, updatedAt = now))
        }
    }
    
    suspend fun deleteProject(id: Int) = 
        projectDao.deleteProjectById(id)
    
    // Cost Center operations
    fun getCostCentersByProjectId(projectId: Int): Flow<List<CostCenter>> =
        costCenterDao.getCostCentersByProjectId(projectId)
    
    fun getAllCostCenters(): Flow<List<CostCenter>> =
        costCenterDao.getAllCostCenters()
    
    suspend fun getCostCenterById(id: Int): CostCenter? =
        costCenterDao.getCostCenterById(id)
    
    suspend fun getTotalBudgetByProjectId(projectId: Int): Double =
        costCenterDao.getTotalBudgetByProjectId(projectId) ?: 0.0
    
    suspend fun getTotalSpentByProjectId(projectId: Int): Double =
        costCenterDao.getTotalSpentByProjectId(projectId) ?: 0.0
    
    suspend fun insertCostCenter(costCenter: CostCenter): Long {
        val now = LocalDateTime.now().format(dateTimeFormatter)
        return costCenterDao.insertCostCenter(costCenter.copy(createdAt = now))
    }
    
    suspend fun updateCostCenter(costCenter: CostCenter) =
        costCenterDao.updateCostCenter(costCenter)
    
    suspend fun addSpentToCostCenter(id: Int, amount: Double) =
        costCenterDao.addSpentAmount(id, amount)
    
    suspend fun deleteCostCenter(id: Int) =
        costCenterDao.deleteCostCenterById(id)
}
