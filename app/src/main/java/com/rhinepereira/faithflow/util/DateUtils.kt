package com.rhinepereira.faithflow.util

import java.util.Calendar

/**
 * Utility functions and constants for date and calendar calculations.
 */
object DateUtils {

    const val MILLIS_PER_DAY: Long = 24L * 60 * 60 * 1000

    /**
     * Truncates the given timestamp (in milliseconds) to the start of the day (00:00:00.000).
     */
    fun getStartOfDay(timestamp: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * Checks if two timestamps fall on the exact same calendar day and year.
     */
    fun isSameDay(t1: Long, t2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = t1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = t2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * Generates a list of Calendar instances representing the days of the month for the given calendar,
     * prefixed with `null` padding for days preceding the first day of the week (Sunday-indexed).
     */
    fun getDaysInMonth(calendar: Calendar): List<Calendar?> {
        val days = mutableListOf<Calendar?>()
        val cal = calendar.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
        repeat(firstDayOfWeek) { days.add(null) }
        val totalDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        repeat(totalDays) {
            days.add(cal.clone() as Calendar)
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        return days
    }
}
