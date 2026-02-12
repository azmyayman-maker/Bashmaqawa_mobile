package com.bashmaqawa.data.database.dao

import androidx.room.*
import com.bashmaqawa.data.database.entities.CostCenter
import kotlinx.coroutines.flow.Flow

/**
 * DAO for CostCenter operations
 */
@Dao
interface CostCenterDao {
    
    @Query("SELECT * FROM cost_centers WHERE project_id = :projectId ORDER BY name ASC")
    fun getCostCentersByProjectId(projectId: Int): Flow<List<CostCenter>>
    
    @Query("SELECT * FROM cost_centers ORDER BY name ASC")
    fun getAllCostCenters(): Flow<List<CostCenter>>
    
    @Query("SELECT * FROM cost_centers WHERE id = :id")
    suspend fun getCostCenterById(id: Int): CostCenter?
    
    @Query("SELECT SUM(budget) FROM cost_centers WHERE project_id = :projectId")
    suspend fun getTotalBudgetByProjectId(projectId: Int): Double?
    
    @Query("SELECT SUM(spent) FROM cost_centers WHERE project_id = :projectId")
    suspend fun getTotalSpentByProjectId(projectId: Int): Double?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCostCenter(costCenter: CostCenter): Long
    
    @Update
    suspend fun updateCostCenter(costCenter: CostCenter)
    
    @Query("UPDATE cost_centers SET spent = spent + :amount WHERE id = :id")
    suspend fun addSpentAmount(id: Int, amount: Double)
    
    @Delete
    suspend fun deleteCostCenter(costCenter: CostCenter)
    
    @Query("DELETE FROM cost_centers WHERE id = :id")
    suspend fun deleteCostCenterById(id: Int)
}
