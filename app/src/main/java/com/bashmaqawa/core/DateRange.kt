package com.bashmaqawa.core

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * نطاق التاريخ - Date Range Utility
 * Immutable data class for handling date range operations.
 * Used primarily for payroll calculations and report generation.
 */
data class DateRange(
    val startDate: LocalDate,
    val endDate: LocalDate
) {
    init {
        require(startDate <= endDate) { 
            "Start date ($startDate) must be before or equal to end date ($endDate)" 
        }
    }
    
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    
    /**
     * Get start date as formatted string
     */
    val startDateString: String get() = startDate.format(formatter)
    
    /**
     * Get end date as formatted string
     */
    val endDateString: String get() = endDate.format(formatter)
    
    /**
     * Calculate the number of days in this range (inclusive)
     */
    val dayCount: Long get() = ChronoUnit.DAYS.between(startDate, endDate) + 1
    
    /**
     * Check if a date falls within this range (inclusive)
     */
    fun contains(date: LocalDate): Boolean = date in startDate..endDate
    
    /**
     * Check if a date string falls within this range
     */
    fun contains(dateString: String): Boolean {
        return try {
            contains(LocalDate.parse(dateString, formatter))
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check if this range overlaps with another range
     */
    fun overlaps(other: DateRange): Boolean =
        startDate <= other.endDate && endDate >= other.startDate
    
    /**
     * Get the intersection of two ranges, or null if they don't overlap
     */
    fun intersect(other: DateRange): DateRange? {
        if (!overlaps(other)) return null
        return DateRange(
            maxOf(startDate, other.startDate),
            minOf(endDate, other.endDate)
        )
    }
    
    /**
     * Extend this range by a number of days on each side
     */
    fun extend(daysBefore: Long = 0, daysAfter: Long = 0): DateRange =
        DateRange(
            startDate.minusDays(daysBefore),
            endDate.plusDays(daysAfter)
        )
    
    /**
     * Get list of all dates in this range
     */
    fun toDateList(): List<LocalDate> {
        val dates = mutableListOf<LocalDate>()
        var current = startDate
        while (current <= endDate) {
            dates.add(current)
            current = current.plusDays(1)
        }
        return dates
    }
    
    /**
     * Get list of all date strings in this range
     */
    fun toDateStringList(): List<String> = toDateList().map { it.format(formatter) }
    
    companion object {
        private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        
        /**
         * Create a DateRange for the current week (Monday to Sunday)
         */
        fun currentWeek(): DateRange {
            val today = LocalDate.now()
            val monday = today.minusDays(today.dayOfWeek.value.toLong() - 1)
            val sunday = monday.plusDays(6)
            return DateRange(monday, sunday)
        }
        
        /**
         * Create a DateRange for the current month
         */
        fun currentMonth(): DateRange {
            val today = LocalDate.now()
            return DateRange(
                today.withDayOfMonth(1),
                today.withDayOfMonth(today.lengthOfMonth())
            )
        }
        
        /**
         * Create a DateRange for the last N days
         */
        fun lastDays(days: Int): DateRange {
            val today = LocalDate.now()
            return DateRange(today.minusDays(days.toLong() - 1), today)
        }
        
        /**
         * Create a DateRange from string dates
         */
        fun fromStrings(startDate: String, endDate: String): DateRange {
            return DateRange(
                LocalDate.parse(startDate, formatter),
                LocalDate.parse(endDate, formatter)
            )
        }
        
        /**
         * Create a DateRange for a single day
         */
        fun singleDay(date: LocalDate): DateRange = DateRange(date, date)
        
        /**
         * Create a DateRange for a single day from string
         */
        fun singleDay(dateString: String): DateRange {
            val date = LocalDate.parse(dateString, formatter)
            return DateRange(date, date)
        }
    }
    
    override fun toString(): String = "$startDateString to $endDateString"
}
