package com.bashmaqawa.data.database.dao

import androidx.room.*
import com.bashmaqawa.data.database.entities.Account
import com.bashmaqawa.data.database.entities.AccountType
import com.bashmaqawa.data.database.entities.AccountCategory
import kotlinx.coroutines.flow.Flow

/**
 * Account summary for display
 */
data class AccountSummary(
    val id: Int,
    val name: String,
    val type: String?,
    val category: String,
    val accountCode: String?,
    val balance: Double,
    val isSystemAccount: Boolean
)

/**
 * DAO for Account operations
 * Enhanced with atomic operations for double-entry bookkeeping
 */
@Dao
interface AccountDao {
    
    @Query("SELECT * FROM accounts WHERE is_active = 1 ORDER BY account_code ASC, name ASC")
    fun getAllActiveAccounts(): Flow<List<Account>>
    
    @Query("SELECT * FROM accounts ORDER BY account_code ASC, name ASC")
    fun getAllAccounts(): Flow<List<Account>>

    @Query("SELECT * FROM accounts ORDER BY account_code ASC, name ASC")
    suspend fun getAllAccountsList(): List<Account>
    
    @Query("SELECT * FROM accounts WHERE type = :type AND is_active = 1 ORDER BY name ASC")
    fun getAccountsByType(type: AccountType): Flow<List<Account>>
    
    @Query("SELECT * FROM accounts WHERE category = :category AND is_active = 1 ORDER BY account_code ASC, name ASC")
    fun getAccountsByCategory(category: AccountCategory): Flow<List<Account>>
    
    @Query("SELECT * FROM accounts WHERE parent_id IS NULL AND is_active = 1 ORDER BY account_code ASC, name ASC")
    fun getRootAccounts(): Flow<List<Account>>
    
    @Query("SELECT * FROM accounts WHERE parent_id = :parentId AND is_active = 1 ORDER BY name ASC")
    fun getChildAccounts(parentId: Int): Flow<List<Account>>
    
    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: Int): Account?
    
    @Query("SELECT * FROM accounts WHERE id = :id")
    fun getAccountByIdFlow(id: Int): Flow<Account?>
    
    @Query("SELECT * FROM accounts WHERE account_code = :accountCode AND is_active = 1")
    suspend fun getAccountByCode(accountCode: String): Account?
    
    @Query("SELECT * FROM accounts WHERE is_system_account = 1 AND is_active = 1 ORDER BY account_code ASC")
    fun getSystemAccounts(): Flow<List<Account>>
    
    @Query("SELECT * FROM accounts WHERE is_system_account = 1 AND type = :type AND is_active = 1")
    suspend fun getSystemAccountByType(type: AccountType): Account?
    
    @Query("SELECT balance FROM accounts WHERE id = :id")
    suspend fun getAccountBalance(id: Int): Double?
    
    @Query("SELECT SUM(balance) FROM accounts WHERE type = :type AND is_active = 1")
    suspend fun getTotalBalanceByType(type: AccountType): Double?
    
    @Query("SELECT SUM(balance) FROM accounts WHERE category = :category AND is_active = 1")
    suspend fun getTotalBalanceByCategory(category: AccountCategory): Double?
    
    @Query("SELECT SUM(balance) FROM accounts WHERE category = 'ASSET' AND is_active = 1")
    suspend fun getTotalAssets(): Double?
    
    @Query("SELECT SUM(balance) FROM accounts WHERE category = 'LIABILITY' AND is_active = 1")
    suspend fun getTotalLiabilities(): Double?
    
    @Query("SELECT SUM(balance) FROM accounts WHERE category = 'EQUITY' AND is_active = 1")
    suspend fun getTotalEquity(): Double?
    
    @Query("""
        SELECT id, name, type, category, account_code as accountCode, balance, is_system_account as isSystemAccount
        FROM accounts WHERE is_active = 1
        ORDER BY account_code ASC, name ASC
    """)
    fun getAccountSummaries(): Flow<List<AccountSummary>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: Account): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<Account>)
    
    @Update
    suspend fun updateAccount(account: Account)
    
    @Query("UPDATE accounts SET balance = balance + :amount, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateAccountBalance(id: Int, amount: Double, updatedAt: String)
    
    @Query("UPDATE accounts SET balance = :newBalance, updated_at = :updatedAt WHERE id = :id")
    suspend fun setAccountBalance(id: Int, newBalance: Double, updatedAt: String)
    
    @Query("UPDATE accounts SET is_active = :isActive, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateAccountActiveStatus(id: Int, isActive: Boolean, updatedAt: String)
    
    @Delete
    suspend fun deleteAccount(account: Account)
    
    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteAccountById(id: Int)
    
    @Query("SELECT COUNT(*) FROM accounts WHERE is_active = 1")
    suspend fun getActiveAccountCount(): Int
}

