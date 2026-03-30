package com.rhinepereira.faithflow.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rhinepereira.faithflow.data.DailyRecord
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DailyScreen(viewModel: DailyViewModel = viewModel()) {
    val currentRecord by viewModel.currentRecord.collectAsState()
    val targetDate by viewModel.targetDate.collectAsState()
    val isSealing by viewModel.isSealing.collectAsState()
    val allRecords by viewModel.allDailyRecords.collectAsState(initial = emptyList())
    val scrollState = rememberScrollState()

    // Calendar State
    var currentMonth by remember { mutableStateOf(Calendar.getInstance().apply { timeInMillis = targetDate }) }
    val daysInMonth = remember(currentMonth) { getDaysInMonth(currentMonth) }
    val monthYearFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }

    // Local states for UI stability
    var whatRead by remember { mutableStateOf("") }
    var prayerTime by remember { mutableStateOf(0) }
    var prophecy by remember { mutableStateOf("") }
    
    var isReadingChecked by remember { mutableStateOf(false) }
    var isPrayerChecked by remember { mutableStateOf(false) }
    
    var isCustomTimeVisible by remember { mutableStateOf(false) }

    val todayMillis = remember { getStartOfDay(System.currentTimeMillis()) }
    val isFutureDate = targetDate > todayMillis

    // Sync month when targetDate changes (e.g. via arrows)
    LaunchedEffect(targetDate) {
        val dateMonth = Calendar.getInstance().apply { timeInMillis = targetDate }
        if (dateMonth.get(Calendar.MONTH) != currentMonth.get(Calendar.MONTH) || 
            dateMonth.get(Calendar.YEAR) != currentMonth.get(Calendar.YEAR)) {
            currentMonth = dateMonth
        }
    }

    // Update local state when the underlying record changes (e.g. date change or toggle clear)
    LaunchedEffect(currentRecord?.id, targetDate, currentRecord?.readToday, currentRecord?.prayedToday) {
        currentRecord?.let { record ->
            isReadingChecked = record.readToday
            isPrayerChecked = record.prayedToday
            
            if (!record.readToday || whatRead.isEmpty()) {
                whatRead = record.whatRead ?: ""
            }
            if (!record.prayedToday || prayerTime == 0) {
                prayerTime = record.totalPrayerTimeMinutes
            }
            
            prophecy = record.prophecy ?: ""
        } ?: run {
            whatRead = ""
            prayerTime = 0
            prophecy = ""
            isReadingChecked = false
            isPrayerChecked = false
        }
    }

    // Debounced sync for text fields to prevent character skipping
    LaunchedEffect(whatRead) {
        if (whatRead != (currentRecord?.whatRead ?: "")) {
            delay(500)
            viewModel.updateDailyRecord(whatRead = whatRead)
        }
    }

    LaunchedEffect(prophecy) {
        if (prophecy != (currentRecord?.prophecy ?: "")) {
            delay(800)
            viewModel.updateDailyRecord(prophecy = prophecy)
        }
    }

    val colorScheme = MaterialTheme.colorScheme
    val primaryColor = colorScheme.primary
    val backgroundColor = colorScheme.background
    val surfaceContainerColor = colorScheme.surfaceVariant
    val onSurfaceColor = colorScheme.onSurface
    val onSurfaceVariantColor = colorScheme.onSurfaceVariant
    val gradientBrush = Brush.linearGradient(
        colors = listOf(primaryColor, primaryColor.copy(alpha = 0.8f))
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .imePadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // --- CALENDAR SECTION ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(surfaceContainerColor.copy(alpha = 0.3f))
                    .padding(16.dp)
            ) {
                // Month Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        currentMonth = (currentMonth.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Prev Month", tint = primaryColor)
                    }

                    Text(
                        text = monthYearFormat.format(currentMonth.time),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = primaryColor
                    )

                    IconButton(onClick = {
                        currentMonth = (currentMonth.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Month", tint = primaryColor)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Weekdays
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = onSurfaceVariantColor.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Grid using standard Column/Row for scroll stability
                val entireGrid = daysInMonth.toMutableList()
                while (entireGrid.size % 7 != 0) {
                    entireGrid.add(null)
                }
                val rows = entireGrid.chunked(7)
                
                rows.forEach { week ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        week.forEach { date ->
                            Box(modifier = Modifier.weight(1f).aspectRatio(1f)) {
                                if (date != null) {
                                    val dateMillis = date.timeInMillis
                                    val record = allRecords.find { isSameDay(it.date, dateMillis) }
                                    val isSelected = isSameDay(targetDate, dateMillis)
                                    val isToday = isSameDay(dateMillis, todayMillis)
                                    
                                    val cellColor = when {
                                        dateMillis > todayMillis -> Color.Transparent
                                        record == null -> Color(0xFFE57373)
                                        record.readToday && record.prayedToday -> Color(0xFF4CAF50)
                                        record.readToday || record.prayedToday -> Color(0xFFFBC02D)
                                        else -> Color(0xFFE57373)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize(0.85f) // Scalable based on cell size
                                            .align(Alignment.Center)
                                            .clip(CircleShape)
                                            .background(cellColor)
                                            .then(
                                                if (record == null && dateMillis <= todayMillis) Modifier.border(
                                                    width = 1.dp,
                                                    color = onSurfaceVariantColor.copy(alpha = 0.15f),
                                                    shape = CircleShape
                                                ) else if (dateMillis > todayMillis) Modifier.border(
                                                    width = 1.dp,
                                                    color = onSurfaceVariantColor.copy(alpha = 0.05f),
                                                    shape = CircleShape
                                                ) else Modifier
                                            )
                                            .then(
                                                if (isSelected) Modifier.border(
                                                    width = 2.dp,
                                                    color = primaryColor,
                                                    shape = CircleShape
                                                ) else Modifier
                                            )
                                            .clickable { 
                                                viewModel.setTargetDate(dateMillis)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                            Text(
                                                text = date.get(Calendar.DAY_OF_MONTH).toString(),
                                                color = if (record != null || (record == null && dateMillis <= todayMillis)) Color.White else onSurfaceColor,
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                                fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Medium
                                            )
                                            if (isToday) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(4.dp)
                                                        .background(if (record == null) primaryColor else Color.White, CircleShape)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Selected Date Header and Status
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val dateStr = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date(targetDate))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = dateStr,
                        color = primaryColor,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (currentRecord?.isSealed == true) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Sealed",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                if (isFutureDate) {
                    Text(
                        "Locked - This trip hasn't begun yet.",
                        color = onSurfaceVariantColor.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // --- TRACKER CANVAS --- (Disabled if future date)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (isFutureDate) Modifier.alpha(0.5f) else Modifier),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // Reading Tracker
                TrackerSection(
                    title = "Have you read the Word?",
                    subtitle = "Divine nourishment for the soul.",
                    isChecked = isReadingChecked,
                    onCheckedChange = { 
                        if (!isFutureDate) {
                            viewModel.updateDailyRecord(readToday = it) 
                        }
                    },
                    enabled = !isFutureDate,
                    primaryColor = primaryColor,
                    onSurfaceColor = onSurfaceColor,
                    onSurfaceVariantColor = onSurfaceVariantColor
                ) {
                    Column(
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "WHICH CHAPTERS OR VERSES DID YOU STUDY?",
                            style = MaterialTheme.typography.labelSmall,
                            color = onSurfaceVariantColor,
                            letterSpacing = 2.sp
                        )
                        OutlinedTextField(
                            value = whatRead,
                            onValueChange = { if (!isFutureDate) whatRead = it },
                            readOnly = isFutureDate,
                            placeholder = { 
                                Text("e.g. Psalm 23, John 1:1-14", color = onSurfaceVariantColor.copy(alpha = 0.4f)) 
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                unfocusedContainerColor = colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                focusedIndicatorColor = primaryColor,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = onSurfaceColor,
                                unfocusedTextColor = onSurfaceColor
                            ),
                            shape = RoundedCornerShape(4.dp)
                        )
                    }
                }

                HorizontalDivider(color = onSurfaceVariantColor.copy(alpha = 0.1f))

                // Prayer Tracker
                TrackerSection(
                    title = "Time in personal prayer?",
                    subtitle = "Communing with the Creator.",
                    isChecked = isPrayerChecked,
                    onCheckedChange = { 
                        if (!isFutureDate) {
                            viewModel.updateDailyRecord(prayedToday = it) 
                        }
                    },
                    enabled = !isFutureDate,
                    primaryColor = primaryColor,
                    onSurfaceColor = onSurfaceColor,
                    onSurfaceVariantColor = onSurfaceVariantColor
                ) {
                    Column(
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "DEDICATION DURATION",
                            style = MaterialTheme.typography.labelSmall,
                            color = onSurfaceVariantColor,
                            letterSpacing = 2.sp
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DurationButton(
                                "15 min", 
                                isSelected = prayerTime == 15,
                                enabled = !isFutureDate,
                                onClick = { 
                                    viewModel.updateDailyRecord(prayerTime = 15)
                                },
                                primaryColor = primaryColor,
                                onSurfaceVariantColor = onSurfaceVariantColor
                            )
                            DurationButton(
                                "30 min", 
                                isSelected = prayerTime == 30,
                                enabled = !isFutureDate,
                                onClick = { 
                                    viewModel.updateDailyRecord(prayerTime = 30)
                                },
                                primaryColor = primaryColor,
                                onSurfaceVariantColor = onSurfaceVariantColor
                            )
                            DurationButton(
                                "1 hr", 
                                isSelected = prayerTime == 60,
                                enabled = !isFutureDate,
                                onClick = { 
                                    viewModel.updateDailyRecord(prayerTime = 60)
                                },
                                primaryColor = primaryColor,
                                onSurfaceVariantColor = onSurfaceVariantColor
                            )
                            DurationButton(
                                "Custom", 
                                isSelected = isCustomTimeVisible,
                                enabled = !isFutureDate,
                                onClick = { isCustomTimeVisible = !isCustomTimeVisible },
                                primaryColor = primaryColor,
                                onSurfaceVariantColor = onSurfaceVariantColor
                            )
                        }

                        if (isCustomTimeVisible) {
                            OutlinedTextField(
                                value = if (prayerTime > 0) prayerTime.toString() else "",
                                onValueChange = {
                                    if (!isFutureDate && it.all { char -> char.isDigit() }) {
                                        val time = it.toIntOrNull() ?: 0
                                        viewModel.updateDailyRecord(prayerTime = time)
                                    }
                                },
                                readOnly = isFutureDate,
                                placeholder = { 
                                    Text("Enter duration (min)", color = onSurfaceVariantColor.copy(alpha = 0.4f)) 
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }

                // Prophetic Word Section
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = primaryColor)
                        Text("Prophetic Word & Insights", style = MaterialTheme.typography.headlineSmall, color = onSurfaceColor, fontWeight = FontWeight.SemiBold)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(surfaceContainerColor)
                            .padding(24.dp)
                    ) {
                        BasicTextField(
                            value = prophecy,
                            onValueChange = { if (!isFutureDate) prophecy = it },
                            readOnly = isFutureDate,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = onSurfaceColor, lineHeight = 28.sp),
                            decorationBox = { innerTextField ->
                                if (prophecy.isEmpty()) {
                                    Text("What is the Spirit whispering to your heart?", color = onSurfaceVariantColor.copy(alpha = 0.3f), style = MaterialTheme.typography.bodyLarge)
                                }
                                innerTextField()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(112.dp))
        }

        // --- STICKY SEAL FOOTER ---
        if (!isFutureDate) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = backgroundColor.copy(alpha = 0.95f),
                    tonalElevation = 8.dp,
                    shadowElevation = 4.dp
                ) {
                    Button(
                        onClick = { viewModel.sealTodayWalk() },
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        enabled = !isSealing && !isFutureDate
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(gradientBrush),
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedContent(targetState = isSealing, label = "SealFeedback") { sealing ->
                                if (sealing) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = colorScheme.onPrimary, strokeWidth = 2.dp)
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (currentRecord?.isSealed == true) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp), tint = colorScheme.onPrimary)
                                        }
                                        Text(
                                            text = if (currentRecord?.isSealed == true) "Your Walk is Sealed" else "Seal Today's Walk",
                                            color = colorScheme.onPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrackerSection(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    primaryColor: Color,
    onSurfaceColor: Color,
    onSurfaceVariantColor: Color,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge, color = onSurfaceColor)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = onSurfaceVariantColor)
            }
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = primaryColor.copy(alpha = 0.8f),
                    disabledCheckedTrackColor = primaryColor.copy(alpha = 0.3f)
                )
            )
        }
        
        AnimatedVisibility(visible = isChecked && enabled) {
            content()
        }
    }
}

@Composable
fun DurationButton(
    text: String,
    isSelected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
    primaryColor: Color,
    onSurfaceVariantColor: Color
) {
    val colorScheme = MaterialTheme.colorScheme
    val backgroundColor = when {
        isSelected -> colorScheme.primaryContainer
        else -> colorScheme.surfaceVariant.copy(alpha = 0.2f)
    }
    
    Box(
        modifier = Modifier
            .height(48.dp)
            .width(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(1.dp, if (isSelected) primaryColor else Color.Transparent, RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = if (isSelected) primaryColor else onSurfaceVariantColor, style = MaterialTheme.typography.labelSmall)
    }
}

private fun getDaysInMonth(calendar: Calendar): List<Calendar?> {
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

private fun isSameDay(t1: Long, t2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = t1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = t2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

private fun getStartOfDay(timestamp: Long): Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}
