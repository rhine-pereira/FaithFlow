package com.rhinepereira.faithflow.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class DateUtilsTest {

    @Test
    fun getStartOfDay_zerosTimeFields() {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.SEPTEMBER, 20, 14, 35, 42)
            set(Calendar.MILLISECOND, 987)
        }

        val startOfDay = DateUtils.getStartOfDay(cal.timeInMillis)
        val resultCal = Calendar.getInstance().apply { timeInMillis = startOfDay }

        assertEquals(2026, resultCal.get(Calendar.YEAR))
        assertEquals(Calendar.SEPTEMBER, resultCal.get(Calendar.MONTH))
        assertEquals(20, resultCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, resultCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, resultCal.get(Calendar.MINUTE))
        assertEquals(0, resultCal.get(Calendar.SECOND))
        assertEquals(0, resultCal.get(Calendar.MILLISECOND))
    }

    @Test
    fun isSameDay_returnsTrueForSameDayDifferentTimes() {
        val cal1 = Calendar.getInstance().apply {
            set(2026, Calendar.SEPTEMBER, 20, 9, 0, 0)
        }
        val cal2 = Calendar.getInstance().apply {
            set(2026, Calendar.SEPTEMBER, 20, 23, 59, 59)
        }

        assertTrue(DateUtils.isSameDay(cal1.timeInMillis, cal2.timeInMillis))
    }

    @Test
    fun isSameDay_returnsFalseForDifferentDays() {
        val cal1 = Calendar.getInstance().apply {
            set(2026, Calendar.SEPTEMBER, 20, 23, 59, 59)
        }
        val cal2 = Calendar.getInstance().apply {
            set(2026, Calendar.SEPTEMBER, 21, 0, 0, 1)
        }

        assertFalse(DateUtils.isSameDay(cal1.timeInMillis, cal2.timeInMillis))
    }

    @Test
    fun getDaysInMonth_includesAllDaysOfMonth() {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.SEPTEMBER, 1)
        }
        val days = DateUtils.getDaysInMonth(cal)
        val nonNullDays = days.filterNotNull()

        assertEquals(30, nonNullDays.size) // September has 30 days
    }
}
