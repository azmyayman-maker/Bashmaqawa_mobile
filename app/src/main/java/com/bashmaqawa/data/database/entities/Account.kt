package com.bashmaqawa.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * الحسابات - Accounts Entity
 * Represents financial accounts (cash boxes, banks, e-wallets, liability accounts)
 * Supports full Chart of Accounts structure for double-entry bookkeeping.
 */
@Entity(
    tableName = "accounts",
    foreignKeys = [
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["parent_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("parent_id"),
        Index("account_code"),
        Index("category")
    ]
)
data class Account(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "type")
    val type: AccountType? = null,
    
    @ColumnInfo(name = "balance")
    val balance: Double = 0.0,
    
    @ColumnInfo(name = "details")
    val details: String? = null,
    
    @ColumnInfo(name = "parent_id")
    val parentId: Int? = null,
    
    @ColumnInfo(name = "category")
    val category: AccountCategory = AccountCategory.ASSET,
    
    @ColumnInfo(name = "account_code")
    val accountCode: String? = null, // Standard accounting code (e.g., "1000" for Cash)
    
    @ColumnInfo(name = "account_number")
    val accountNumber: String? = null,
    
    @ColumnInfo(name = "bank_name")
    val bankName: String? = null,
    
    @ColumnInfo(name = "is_system_account")
    val isSystemAccount: Boolean = false, // True for system-managed accounts like Wages Payable
    
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,
    
    @ColumnInfo(name = "created_at")
    val createdAt: String? = null,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: String? = null
)

/**
 * نوع الحساب - Account Type Enum
 */
enum class AccountType {
    CASH_BOX,       // الخزنة
    BANK,           // حساب بنكي
    WALLET,         // محفظة إلكترونية
    RECEIVABLE,     // مستحقات (سلف عمال)
    PAYABLE         // التزامات (أجور مستحقة)
}

/**
 * فئة الحساب - Account Category Enum (Chart of Accounts)
 */
enum class AccountCategory {
    ASSET,      // أصول - Normal debit balance
    LIABILITY,  // خصوم - Normal credit balance
    EQUITY,     // حقوق ملكية - Normal credit balance
    REVENUE,    // إيرادات - Normal credit balance
    EXPENSE     // مصروفات - Normal debit balance
}

/**
 * Standard Account Codes for the Chart of Accounts
 */
object AccountCodes {
    // Assets (1xxx)
    const val CASH = "1000"
    const val BANK = "1100"
    const val WALLET = "1200"
    const val ADVANCES_RECEIVABLE = "1300"  // سلف العمال
    
    // Liabilities (2xxx)
    const val WAGES_PAYABLE = "2100"        // الأجور المستحقة
    const val ACCOUNTS_PAYABLE = "2200"
    
    // Equity (3xxx)
    const val OWNERS_EQUITY = "3000"
    const val RETAINED_EARNINGS = "3100"
    
    // Revenue (4xxx)
    const val PROJECT_INCOME = "4000"
    const val OTHER_INCOME = "4900"
    
    // Expenses (5xxx)
    const val MATERIALS_EXPENSE = "5100"
    const val WAGES_EXPENSE = "5200"
    const val TRANSPORT_EXPENSE = "5300"
    const val EQUIPMENT_EXPENSE = "5400"
    const val RENT_EXPENSE = "5500"
    const val SERVICES_EXPENSE = "5600"
    const val MAINTENANCE_EXPENSE = "5700"
    const val OTHER_EXPENSE = "5900"
}
