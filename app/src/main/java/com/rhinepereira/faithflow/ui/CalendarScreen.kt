package com.rhinepereira.versetrack.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rhinepereira.versetrack.data.DailyRecord
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarScreen(viewModel: DailyViewModel = viewModel()) {
    val allRecords by viewModel.allDailyRecords.collectAsState(initial = emptyList())
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDateRecord by remember { mutableStateOf<DailyRecord?>(null) }

    val daysInMonth = getDaysInMonth(currentMonth)
    val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Month Navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                val newMonth = currentMonth.clone() as Calendar
                newMonth.add(Calendar.MONTH, -1)
                currentMonth = newMonth
            }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Month")
            }
            Text(
                text = monthYearFormat.format(currentMonth.time),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = {
                val newMonth = currentMonth.clone() as Calendar
                newMonth.add(Calendar.MONTH, 1)
                currentMonth = newMonth
            }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Month")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Days Header
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Calendar Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.weight(1f)
        ) {
            items(daysInMonth) { date ->
                if (date == null) {
                    Box(modifier = Modifier.aspectRatio(1f))
                } else {
                    val record = allRecords.find { isSameDay(it.date, date.timeInMillis) }
                    DayCell(
                        date = date,
                        record = record,
                        onClick = { selectedDateRecord = record ?: DailyRecord(date = date.timeInMillis) }
                    )
                }
            }
        }

        // Selected Date Stats
        selectedDateRecord?.let { record ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val df = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    Text(df.format(Date(record.date)), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Bible Reading: ${if (record.readToday) "✅ (${record.totalReadTimeMinutes}m)" else "❌"}")
                    if (record.readToday) Text("Content: ${record.whatRead}", style = MaterialTheme.typography.bodySmall)
                    Text("Prayer: ${if (record.prayedToday) "✅ (${record.totalPrayerTimeMinutes}m)" else "❌"}")
                    if (record.prophecy.isNotBlank()) Text("Prophecy: ${record.prophecy}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun DayCell(date: Calendar, record: DailyRecord?, onClick: () -> Unit) {
    val isReadingDone = record?.readToday == true
    val isPrayerDone = record?.prayedToday == true
    
    val bgColor = when {
        isReadingDone && isPrayerDone -> Color(0xFF4CAF50) // Green
        isReadingDone || isPrayerDone -> Color(0xFFFFC107) // Yellow
        else -> Color(0xFFEF5350) // Red
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .background(bgColor, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date.get(Calendar.DAY_OF_MONTH).toString(),
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

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

fun isSameDay(t1: Long, t2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = t1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = t2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
