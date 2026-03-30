package com.rhinepereira.faithflow.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DailyScreen(viewModel: DailyViewModel = viewModel()) {
    val currentRecord by viewModel.currentRecord.collectAsState()
    val targetDate by viewModel.targetDate.collectAsState()
    val isSealing by viewModel.isSealing.collectAsState()
    val scrollState = rememberScrollState()

    // Local states for UI stability
    var whatRead by remember { mutableStateOf("") }
    var prayerTime by remember { mutableStateOf(0) }
    var prophecy by remember { mutableStateOf("") }
    
    var isReadingChecked by remember { mutableStateOf(false) }
    var isPrayerChecked by remember { mutableStateOf(false) }
    
    var isCustomTimeVisible by remember { mutableStateOf(false) }

    // Update local state when the underlying record changes (e.g. date change or toggle clear)
    LaunchedEffect(currentRecord?.id, targetDate, currentRecord?.readToday, currentRecord?.prayedToday) {
        currentRecord?.let { record ->
            // Only update toggles and durations from DB
            isReadingChecked = record.readToday
            isPrayerChecked = record.prayedToday
            
            // For text fields, only sync if the DB is "clearer" than local (e.g. just toggled off)
            // or if the record ID/Date actually changed.
            if (!record.readToday || whatRead.isEmpty()) {
                whatRead = record.whatRead ?: ""
            }
            if (!record.prayedToday || prayerTime == 0) {
                prayerTime = record.totalPrayerTimeMinutes
            }
            
            prophecy = record.prophecy ?: ""
        } ?: run {
            // Reset for new/missing record
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .imePadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(40.dp)
    ) {
        // Header with Date Navigation
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val isToday = getStartOfDay(System.currentTimeMillis()) == targetDate
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.moveDate(-1) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Day", tint = primaryColor)
                }
                
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

                IconButton(
                    onClick = { viewModel.moveDate(1) },
                    enabled = !isToday
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward, 
                        contentDescription = "Next Day", 
                        tint = if (isToday) primaryColor.copy(alpha = 0.2f) else primaryColor
                    )
                }
            }
            
            if (!isToday) {
                Text(
                    text = "Editing Past Journey",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = onSurfaceVariantColor.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.sp
                )
            }
        }

        // Tracker Canvas
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Reading Tracker
            TrackerSection(
                title = "Have you read the Word?",
                subtitle = "Divine nourishment for the soul.",
                isChecked = isReadingChecked,
                onCheckedChange = { 
                    isReadingChecked = it
                    viewModel.updateDailyRecord(readToday = it) 
                },
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
                        onValueChange = { whatRead = it },
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
                    isPrayerChecked = it
                    viewModel.updateDailyRecord(prayedToday = it) 
                },
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
                            onClick = { 
                                prayerTime = 15
                                viewModel.updateDailyRecord(prayerTime = 15)
                            },
                            primaryColor = primaryColor
                        )
                        DurationButton(
                            "30 min", 
                            isSelected = prayerTime == 30,
                            onClick = { 
                                prayerTime = 30
                                viewModel.updateDailyRecord(prayerTime = 30)
                            },
                            primaryColor = primaryColor
                        )
                        DurationButton(
                            "1 hr", 
                            isSelected = prayerTime == 60,
                            onClick = { 
                                prayerTime = 60
                                viewModel.updateDailyRecord(prayerTime = 60)
                            },
                            primaryColor = primaryColor
                        )
                        DurationButton(
                            "Custom", 
                            isSelected = isCustomTimeVisible,
                            onClick = { isCustomTimeVisible = !isCustomTimeVisible },
                            primaryColor = primaryColor
                        )
                    }

                    if (isCustomTimeVisible) {
                        OutlinedTextField(
                            value = if (prayerTime > 0) prayerTime.toString() else "",
                            onValueChange = {
                                if (it.all { char -> char.isDigit() }) {
                                    val time = it.toIntOrNull() ?: 0
                                    prayerTime = time
                                    viewModel.updateDailyRecord(prayerTime = time)
                                }
                            },
                            placeholder = { 
                                Text("Enter duration (min)", color = onSurfaceVariantColor.copy(alpha = 0.4f)) 
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
            }

            // Prophetic Word Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = primaryColor
                    )
                    Text(
                        "Prophetic Word & Insights",
                        style = MaterialTheme.typography.headlineSmall,
                        color = onSurfaceColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(surfaceContainerColor)
                        .padding(24.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        BasicTextField(
                            value = prophecy,
                            onValueChange = { prophecy = it },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = onSurfaceColor,
                                lineHeight = 28.sp
                            ),
                            decorationBox = { innerTextField ->
                                if (prophecy.isEmpty()) {
                                    Text(
                                        "What is the Spirit whispering to your heart in this season?",
                                        color = onSurfaceVariantColor.copy(alpha = 0.3f),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                                innerTextField()
                            }
                        )
                        Text(
                            "SACRED JOURNAL • AUTO-SAVING",
                            style = MaterialTheme.typography.labelSmall,
                            color = onSurfaceVariantColor.copy(alpha = 0.4f),
                            modifier = Modifier.align(Alignment.End),
                            fontSize = 10.sp,
                            letterSpacing = 0.sp
                        )
                    }
                }
            }

            // Final Action
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Button(
                    onClick = { viewModel.sealTodayWalk() },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(4.dp)),
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
                        AnimatedContent(
                            targetState = isSealing,
                            transitionSpec = {
                                fadeIn() + scaleIn() togetherWith fadeOut() + scaleOut()
                            }, label = "SealFeedback"
                        ) { sealing ->
                            if (sealing) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = if (androidx.compose.foundation.isSystemInDarkTheme()) Color(0xFF3C2F00) else Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Text(
                                        "Sealing...",
                                        color = if (androidx.compose.foundation.isSystemInDarkTheme()) Color(0xFF3C2F00) else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                            } else {
                                Text(
                                    if (currentRecord?.isSealed == true) "Re-Seal Today's Walk" else "Seal Today's Walk",
                                    color = if (androidx.compose.foundation.isSystemInDarkTheme()) Color(0xFF3C2F00) else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }
                }
                Text(
                    text = if (targetDate == getStartOfDay(System.currentTimeMillis())) 
                        "\"Be still, and know...\"" 
                    else 
                        "Reflecting on God's faithfulness...",
                    color = onSurfaceVariantColor,
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Light
                )
            }
        }
        
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun TrackerSection(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    primaryColor: Color,
    onSurfaceColor: Color,
    onSurfaceVariantColor: Color,
    content: @Composable () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
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
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = primaryColor.copy(alpha = 0.6f),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = colorScheme.surfaceVariant
                )
            )
        }
        
        AnimatedVisibility(
            visible = isChecked,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            content()
        }
    }
}

@Composable
fun DurationButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    primaryColor: Color
) {
    val colorScheme = MaterialTheme.colorScheme
    val backgroundColor = if (isSelected) colorScheme.primaryContainer else colorScheme.surfaceVariant.copy(alpha = 0.2f)
    val borderColor = if (isSelected) primaryColor.copy(alpha = 0.6f) else colorScheme.onSurface.copy(alpha = 0.1f)
    val textColor = if (isSelected) primaryColor else colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .height(48.dp)
            .width(80.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(2.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelSmall
        )
    }
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
