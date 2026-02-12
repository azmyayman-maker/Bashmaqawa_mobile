package com.bashmaqawa.utils

import java.text.NumberFormat
import java.util.*

/**
 * Currency Formatter Utility
 * أداة تنسيق العملة
 */
object CurrencyFormatter {
    private val arabicLocale = Locale("ar", "EG")
    private val numberFormat = NumberFormat.getNumberInstance(arabicLocale).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    
    /**
     * Format amount with Egyptian Pound symbol
     */
    fun format(amount: Double): String {
        return "${numberFormat.format(amount)} ج.م"
    }
    
    /**
     * Format amount without currency symbol
     */
    fun formatNumber(amount: Double): String {
        return numberFormat.format(amount)
    }
    
    /**
     * Format amount in short form for compact display (e.g., 1.5K, 2.3M)
     * تنسيق المبلغ بشكل مختصر
     */
    fun formatShort(amount: Double): String {
        return when {
            amount >= 1_000_000 -> {
                val value = amount / 1_000_000
                String.format(Locale.US, "%.1fM", value)
            }
            amount >= 1_000 -> {
                val value = amount / 1_000
                String.format(Locale.US, "%.1fK", value)
            }
            else -> {
                String.format(Locale.US, "%.0f", amount)
            }
        }
    }
    
    /**
     * Parse Arabic formatted number
     */
    fun parse(formatted: String): Double? {
        return try {
            val cleaned = formatted
                .replace("ج.م", "")
                .replace(",", "")
                .replace("٬", "")
                .trim()
            cleaned.toDoubleOrNull()
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Date Formatter Utility
 * أداة تنسيق التاريخ
 */
object DateFormatter {
    private val arabicLocale = Locale("ar", "EG")
    
    /**
     * Format date for display in Arabic
     */
    fun formatArabic(date: java.time.LocalDate): String {
        return date.format(
            java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy", arabicLocale)
        )
    }
    
    /**
     * Format date with day name in Arabic
     */
    fun formatWithDay(date: java.time.LocalDate): String {
        return date.format(
            java.time.format.DateTimeFormatter.ofPattern("EEEE، d MMMM yyyy", arabicLocale)
        )
    }
    
    /**
     * Format for database storage (ISO format)
     */
    fun formatForDb(date: java.time.LocalDate): String {
        return date.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
    }
    
    /**
     * Parse from database format
     */
    fun parseFromDb(dateStr: String): java.time.LocalDate? {
        return try {
            java.time.LocalDate.parse(dateStr)
        } catch (e: Exception) {
            null
        }
    }
}
