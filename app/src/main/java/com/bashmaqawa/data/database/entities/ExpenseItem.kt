package com.bashmaqawa.data.database.entities

import kotlinx.serialization.Serializable

/**
 * عنصر مصروف واحد
 * Single expense item for attendance records
 */
@Serializable
data class ExpenseItem(
    val type: String,           // نوع المصروف: مواصلات، طعام، مواد، أخرى
    val amount: Double,         // المبلغ
    val description: String? = null  // وصف اختياري
)
