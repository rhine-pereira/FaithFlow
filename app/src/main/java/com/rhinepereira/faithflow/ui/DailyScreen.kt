package com.rhinepereira.versetrack.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun DailyScreen(viewModel: DailyViewModel = viewModel()) {
    val todayRecord by viewModel.todayRecord.collectAsState()
    val scrollState = rememberScrollState()

    // Local states to make typing feel snappy
    var whatRead by remember { mutableStateOf("") }
    var readTime by remember { mutableStateOf("") }
    var prayerTime by remember { mutableStateOf("") }
    var prophecy by remember { mutableStateOf("") }

    // Sync local state when the database record changes (e.g., on first load)
    // We only update if the user isn't currently typing (basic check)
    LaunchedEffect(todayRecord) {
        todayRecord?.let { record ->
            whatRead = record.whatRead
            readTime = if (record.totalReadTimeMinutes > 0) record.totalReadTimeMinutes.toString() else ""
            prayerTime = if (record.totalPrayerTimeMinutes > 0) record.totalPrayerTimeMinutes.toString() else ""
            prophecy = record.prophecy
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding() // Ensures UI moves up when keyboard opens
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Daily Devotion", style = MaterialTheme.typography.headlineMedium)

        // Bible Reading Section
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Bible Reading", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = todayRecord?.readToday ?: false,
                        onCheckedChange = { viewModel.updateDailyRecord(readToday = it) }
                    )
                    Text("Read today?")
                }

                OutlinedTextField(
                    value = whatRead,
                    onValueChange = { 
                        whatRead = it
                        viewModel.updateDailyRecord(whatRead = it)
                    },
                    label = { Text("What did you read?") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = readTime,
                    onValueChange = { 
                        if (it.all { char -> char.isDigit() }) {
                            readTime = it
                            viewModel.updateDailyRecord(readTime = it.toIntOrNull() ?: 0)
                        }
                    },
                    label = { Text("Total read time (minutes)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }

        // Prayer Section
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Personal Prayer", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = todayRecord?.prayedToday ?: false,
                        onCheckedChange = { viewModel.updateDailyRecord(prayedToday = it) }
                    )
                    Text("Did you pray?")
                }

                OutlinedTextField(
                    value = prayerTime,
                    onValueChange = { 
                        if (it.all { char -> char.isDigit() }) {
                            prayerTime = it
                            viewModel.updateDailyRecord(prayerTime = it.toIntOrNull() ?: 0)
                        }
                    },
                    label = { Text("Total prayer time (minutes)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = prophecy,
                    onValueChange = { 
                        prophecy = it
                        viewModel.updateDailyRecord(prophecy = it)
                    },
                    label = { Text("Prophecy / Words from God") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        }
        
        // Spacer to ensure content isn't hidden behind the bottom bar when keyboard is open
        Spacer(modifier = Modifier.height(80.dp))
    }
}
