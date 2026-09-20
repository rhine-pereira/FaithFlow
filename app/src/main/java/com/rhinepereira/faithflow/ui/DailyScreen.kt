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
import com.rhinepereira.faithflow.util.DateUtils
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

private val DAILY_MISSED_COLOR = Color(0xFFE57373)
private val DAILY_COMPLETED_COLOR = Color(0xFF4CAF50)
private val DAILY_PARTIAL_COLOR = Color(0xFFFBC02D)

/**
 * Screen for tracking daily spiritual walk (Scripture reading, prayer duration, and reflections).
 */
@Composable
fun DailyScreen(
    viewModel: DailyViewModel = viewModel(),
    isVisible: Boolean = true
) {
    val currentRecord = viewModel.currentRecord.collectAsStateWhenVisible(isVisible)
    val targetDate = viewModel.targetDate.collectAsStateWhenVisible(isVisible)
    val isSealing = viewModel.isSealing.collectAsStateWhenVisible(isVisible)
    val allRecords = viewModel.allDailyRecords.collectAsStateWhenVisible(isVisible)
    val recordsByDay = remember(allRecords) {
        allRecords.associateBy { DateUtils.getStartOfDay(it.date) }
    }
    val scrollState = rememberScrollState()

    // Calendar State
    var currentMonth by remember { mutableStateOf(Calendar.getInstance().apply { timeInMillis = targetDate }) }
    val daysInMonth = remember(currentMonth) { DateUtils.getDaysInMonth(currentMonth) }
    val monthYearFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }

    // Local states for UI stability
    var whatRead by remember { mutableStateOf("") }
    var prayerTime by remember { mutableStateOf(0) }
    var prophecy by remember { mutableStateOf("") }

    var isReadingChecked by remember { mutableStateOf(false) }
    var isPrayerChecked by remember { mutableStateOf(false) }

    var isCustomTimeVisible by remember { mutableStateOf(false) }

    val todayMillis = remember { DateUtils.getStartOfDay() }
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
            CalendarGrid(
                currentMonth = currentMonth,
                daysInMonth = daysInMonth,
                targetDate = targetDate,
                todayMillis = todayMillis,
                recordsByDay = recordsByDay,
                monthYearFormat = monthYearFormat,
                primaryColor = primaryColor,
                surfaceContainerColor = surfaceContainerColor,
                onSurfaceColor = onSurfaceColor,
                onSurfaceVariantColor = onSurfaceVariantColor,
                onPrevMonth = {
                    currentMonth = (currentMonth.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                },
                onNextMonth = {
                    currentMonth = (currentMonth.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
                },
                onDateSelected = { dateMillis ->
                    viewModel.setTargetDate(dateMillis)
                }
            )

            // Selected Date Header and Status
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Date Title & Day Navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.moveDate(-1) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Day", tint = primaryColor)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = SimpleDateFormat("EEEE", Locale.getDefault()).format(Date(targetDate)).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                        Text(
                            text = SimpleDateFormat("MMMM d", Locale.getDefault()).format(Date(targetDate)),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = onSurfaceColor
                        )
                    }

                    val canGoForward = targetDate < todayMillis
                    IconButton(
                        onClick = { if (canGoForward) viewModel.moveDate(1) },
                        enabled = canGoForward
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Day",
                            tint = if (canGoForward) primaryColor else onSurfaceVariantColor.copy(alpha = 0.3f)
                        )
                    }
                }

                // Walk Sealed Badge
                if (currentRecord?.isSealed == true) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = primaryColor.copy(alpha = 0.1f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = primaryColor, modifier = Modifier.size(16.dp))
                            Text("Walk Sealed for Today", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = primaryColor)
                        }
                    }
                }
            }

            // --- TRACKING SECTION ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(if (isFutureDate) 0.38f else 1.0f),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 1. Scripture Section
                TrackerSection(
                    title = "Scripture",
                    subtitle = "Did you spend time in the Word?",
                    isChecked = isReadingChecked,
                    onCheckedChange = { checked ->
                        isReadingChecked = checked
                        viewModel.updateDailyRecord(readToday = checked)
                    },
                    enabled = !isFutureDate,
                    primaryColor = primaryColor,
                    onSurfaceColor = onSurfaceColor,
                    onSurfaceVariantColor = onSurfaceVariantColor
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("What did you read?", style = MaterialTheme.typography.labelMedium, color = onSurfaceVariantColor)
                        OutlinedTextField(
                            value = whatRead,
                            onValueChange = { whatRead = it },
                            placeholder = { Text("e.g. John 15, Psalm 23", color = onSurfaceVariantColor.copy(alpha = 0.5f)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = surfaceContainerColor
                            ),
                            enabled = !isFutureDate,
                            singleLine = true
                        )
                    }
                }

                HorizontalDivider(color = surfaceContainerColor.copy(alpha = 0.5f))

                // 2. Prayer Section
                TrackerSection(
                    title = "Prayer",
                    subtitle = "Did you spend time in communion?",
                    isChecked = isPrayerChecked,
                    onCheckedChange = { checked ->
                        isPrayerChecked = checked
                        viewModel.updateDailyRecord(prayedToday = checked)
                    },
                    enabled = !isFutureDate,
                    primaryColor = primaryColor,
                    onSurfaceColor = onSurfaceColor,
                    onSurfaceVariantColor = onSurfaceVariantColor
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("How long?", style = MaterialTheme.typography.labelMedium, color = onSurfaceVariantColor)
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf(15 to "15m", 30 to "30m", 60 to "1h").forEach { (minutes, label) ->
                                DurationButton(
                                    text = label,
                                    isSelected = prayerTime == minutes && !isCustomTimeVisible,
                                    enabled = !isFutureDate,
                                    onClick = { 
                                        isCustomTimeVisible = false
                                        prayerTime = minutes
                                        viewModel.updateDailyRecord(prayerTime = minutes)
                                    },
                                    primaryColor = primaryColor,
                                    onSurfaceVariantColor = onSurfaceVariantColor
                                )
                            }
                            DurationButton(
                                text = "Custom",
                                isSelected = isCustomTimeVisible || (prayerTime != 15 && prayerTime != 30 && prayerTime != 60 && prayerTime > 0),
                                enabled = !isFutureDate,
                                onClick = { isCustomTimeVisible = !isCustomTimeVisible },
                                primaryColor = primaryColor,
                                onSurfaceVariantColor = onSurfaceVariantColor
                            )
                        }

                        if (isCustomTimeVisible || (prayerTime != 15 && prayerTime != 30 && prayerTime != 60 && prayerTime > 0)) {
                            OutlinedTextField(
                                value = if (prayerTime == 0) "" else prayerTime.toString(),
                                onValueChange = { 
                                    val time = it.filter { char -> char.isDigit() }.toIntOrNull() ?: 0
                                    prayerTime = time
                                    viewModel.updateDailyRecord(prayerTime = time)
                                },
                                label = { Text("Minutes") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = primaryColor,
                                    unfocusedBorderColor = surfaceContainerColor
                                ),
                                enabled = !isFutureDate,
                                singleLine = true
                            )
                        }
                    }
                }

                HorizontalDivider(color = surfaceContainerColor.copy(alpha = 0.5f))

                // 3. Prophetic Whisper / Reflection
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = primaryColor, modifier = Modifier.size(20.dp))
                        Text("Reflection & Prophecy", style = MaterialTheme.typography.titleLarge, color = onSurfaceColor)
                    }
                    Text(
                        "What did the Lord impress on your heart today?",
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurfaceVariantColor
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = surfaceContainerColor.copy(alpha = 0.3f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, surfaceContainerColor.copy(alpha = 0.5f))
                    ) {
                        Box(modifier = Modifier.padding(16.dp)) {
                            if (prophecy.isEmpty()) {
                                Text(
                                    "A whisper, a scripture, a promise, or conviction...",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                                    color = onSurfaceVariantColor.copy(alpha = 0.5f)
                                )
                            }
                            BasicTextField(
                                value = prophecy,
                                onValueChange = { prophecy = it },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = onSurfaceColor),
                                enabled = !isFutureDate
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(64.dp))
        }

        // --- STICKY SEAL FOOTER ---
        if (!isFutureDate) {
            SealFooter(
                isSealed = currentRecord?.isSealed == true,
                isSealing = isSealing,
                backgroundColor = backgroundColor,
                gradientBrush = gradientBrush,
                onSealClick = { viewModel.sealTodayWalk() }
            )
        }
    }
}

/**
 * Calendar grid displaying month days with visual completion rings.
 */
@Composable
fun CalendarGrid(
    currentMonth: Calendar,
    daysInMonth: List<Calendar?>,
    targetDate: Long,
    todayMillis: Long,
    recordsByDay: Map<Long, DailyRecord>,
    monthYearFormat: SimpleDateFormat,
    primaryColor: Color,
    surfaceContainerColor: Color,
    onSurfaceColor: Color,
    onSurfaceVariantColor: Color,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
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
            IconButton(onClick = onPrevMonth) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Prev Month", tint = primaryColor)
            }

            Text(
                text = monthYearFormat.format(currentMonth.time),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = primaryColor
            )

            IconButton(onClick = onNextMonth) {
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
                            val record = recordsByDay[DateUtils.getStartOfDay(dateMillis)]
                            val isSelected = DateUtils.isSameDay(targetDate, dateMillis)
                            val isToday = DateUtils.isSameDay(dateMillis, todayMillis)

                            val cellColor = when {
                                dateMillis > todayMillis -> Color.Transparent
                                record == null -> DAILY_MISSED_COLOR
                                record.readToday && record.prayedToday -> DAILY_COMPLETED_COLOR
                                record.readToday || record.prayedToday -> DAILY_PARTIAL_COLOR
                                else -> DAILY_MISSED_COLOR
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxSize(0.85f)
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
                                        onDateSelected(dateMillis)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                    Text(
                                        text = date.get(Calendar.DAY_OF_MONTH).toString(),
                                        color = if (record != null || dateMillis <= todayMillis) Color.White else onSurfaceColor,
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
}

/**
 * Sticky action footer button to seal the daily spiritual walk.
 */
@Composable
fun SealFooter(
    isSealed: Boolean,
    isSealing: Boolean,
    backgroundColor: Color,
    gradientBrush: Brush,
    onSealClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier
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
                onClick = onSealClick,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                enabled = !isSealing
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
                                if (isSealed) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp), tint = colorScheme.onPrimary)
                                }
                                Text(
                                    text = if (isSealed) "Your Walk is Sealed" else "Seal Today's Walk",
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
