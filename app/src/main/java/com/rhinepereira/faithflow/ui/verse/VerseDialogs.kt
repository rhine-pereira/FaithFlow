package com.rhinepereira.faithflow.ui.verse

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.PopupProperties
import com.rhinepereira.faithflow.data.BibleData
import com.rhinepereira.faithflow.data.BibleDatabaseHelper
import com.rhinepereira.faithflow.data.Note
import com.rhinepereira.faithflow.data.Verse

/**
 * Dialog prompting the user to create a new theme.
 */
@Composable
fun AddNoteDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var theme by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Theme", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary) },
        text = {
            OutlinedTextField(
                value = theme,
                onValueChange = { theme = it },
                label = { Text("Theme (e.g. Faith, Hope)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        },
        confirmButton = {
            Button(
                onClick = { if (theme.isNotBlank()) onConfirm(theme.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Add", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.outline)
            }
        }
    )
}

/**
 * Dialog for selecting a Catholic Bible book, chapter, and verse range, with optional RSV Bible search.
 */
@Composable
fun AddVerseDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    val context = LocalContext.current
    val bibleHelper = remember { BibleDatabaseHelper(context) }

    var bookInput by remember { mutableStateOf("") }
    var filteredBooks by remember { mutableStateOf(emptyList<String>()) }
    var expanded by remember { mutableStateOf(false) }

    var chapter by remember { mutableStateOf("") }
    var verseStart by remember { mutableStateOf("") }
    var verseEnd by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    var showVerseFetchConfirmation by remember { mutableStateOf<String?>(null) }
    var showVerseNotFound by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.padding(16.dp),
        title = { Text("Add Bible Verse", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                // Book Selection with Search Suggestion
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = bookInput,
                        onValueChange = {
                            bookInput = it
                            filteredBooks = if (it.isEmpty()) {
                                emptyList()
                            } else {
                                BibleData.catholicBooks.filter { book ->
                                    book.contains(it, ignoreCase = true)
                                }
                            }
                            expanded = filteredBooks.isNotEmpty()
                        },
                        label = { Text("Book") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        properties = PopupProperties(focusable = false),
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        filteredBooks.take(5).forEach { book ->
                            DropdownMenuItem(
                                text = { Text(book) },
                                onClick = {
                                    bookInput = book
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = chapter,
                        onValueChange = { if (it.all { char -> char.isDigit() }) chapter = it },
                        label = { Text("Ch.") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                    )
                    OutlinedTextField(
                        value = verseStart,
                        onValueChange = { if (it.all { char -> char.isDigit() }) verseStart = it },
                        label = { Text("Ver.") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                    )
                    OutlinedTextField(
                        value = verseEnd,
                        onValueChange = { if (it.all { char -> char.isDigit() }) verseEnd = it },
                        label = { Text("End") },
                        placeholder = { Text("-") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                    )
                }

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Verse Content") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                )

                Button(
                    onClick = {
                        val c = chapter.toIntOrNull()
                        val v = verseStart.toIntOrNull()
                        val ve = verseEnd.toIntOrNull()
                        if (bookInput.isNotBlank() && c != null && v != null) {
                            val fetched = bibleHelper.getVerseRange(bookInput, c, v, ve)
                            if (fetched != null) {
                                showVerseFetchConfirmation = fetched
                            } else {
                                showVerseNotFound = true
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = bookInput.isNotBlank() && chapter.isNotBlank() && verseStart.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search Bible",
                            modifier = Modifier.size(18.dp)
                        )
                        Text("Fetch from Bible, RSV", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (bookInput.isNotBlank() && chapter.isNotBlank() && verseStart.isNotBlank() && content.isNotBlank()) {
                        val ref = if (verseEnd.isBlank()) {
                            "$bookInput $chapter:$verseStart"
                        } else {
                            "$bookInput $chapter:$verseStart-$verseEnd"
                        }
                        onConfirm(ref, content)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Add", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.outline)
            }
        }
    )

    showVerseFetchConfirmation?.let { fetchedContent ->
        AlertDialog(
            onDismissRequest = { showVerseFetchConfirmation = null },
            title = { Text("Use this verse content?", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary) },
            text = { Text(fetchedContent, style = MaterialTheme.typography.bodyLarge) },
            confirmButton = {
                Button(
                    onClick = {
                        content = fetchedContent
                        showVerseFetchConfirmation = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Yes, use it", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showVerseFetchConfirmation = null }) {
                    Text("No", color = MaterialTheme.colorScheme.outline)
                }
            }
        )
    }

    if (showVerseNotFound) {
        AlertDialog(
            onDismissRequest = { showVerseNotFound = false },
            title = {
                Text(
                    "Verse Not Found",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "We couldn't find the verse you're looking for.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        "If you think this is a mistake, please contact us.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showVerseNotFound = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

/**
 * Dialog allowing editing of a verse's reference or content.
 */
@Composable
fun EditVerseDialog(
    verse: Verse,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var reference by remember { mutableStateOf(verse.reference) }
    var content by remember { mutableStateOf(verse.content) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Verse", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = reference,
                    onValueChange = { reference = it },
                    label = { Text("Reference") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (reference.isNotBlank() && content.isNotBlank()) onConfirm(reference, content) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.outline)
            }
        }
    )
}

/**
 * Dialog for importing shared verse text into an existing or new theme.
 */
@Composable
fun SharedTextDialog(
    sharedText: String,
    themes: List<Note>,
    onDismiss: () -> Unit,
    onConfirm: (String?, String?, String, String) -> Unit
) {
    var selectedNoteId by remember { mutableStateOf<String?>(themes.firstOrNull()?.id) }
    var newThemeName by remember { mutableStateOf("") }
    var isNewTheme by remember { mutableStateOf(themes.isEmpty()) }

    // Parsing logic
    val lines = sharedText.lines().filter { it.isNotBlank() }
    var reference = lines.firstOrNull() ?: ""

    // Remove Bible version (e.g., "RSV-C", "NIV", "KJV") from the end of the first line
    reference = reference.replace(Regex("""\s+[A-Z0-9-]{2,}$"""), "").trim()

    val content = lines.drop(1).filter { !it.startsWith("http") }.joinToString("\n").trim()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import Shared Verse", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Reference: $reference", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(content, maxLines = 3, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                Text("Choose Theme", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)

                if (themes.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = !isNewTheme,
                            onClick = { isNewTheme = false },
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                        )
                        Text("Existing Theme", modifier = Modifier.clickable { isNewTheme = false }, style = MaterialTheme.typography.bodyLarge)
                    }

                    if (!isNewTheme) {
                        var expanded by remember { mutableStateOf(false) }
                        val selectedNote = themes.find { it.id == selectedNoteId }

                        Box {
                            OutlinedButton(
                                onClick = { expanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                            ) {
                                Text(selectedNote?.theme ?: "Select Theme", color = MaterialTheme.colorScheme.primary)
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                themes.forEach { note ->
                                    DropdownMenuItem(
                                        text = { Text(note.theme) },
                                        onClick = {
                                            selectedNoteId = note.id
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = isNewTheme,
                        onClick = { isNewTheme = true },
                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                    )
                    Text("Create New Theme", modifier = Modifier.clickable { isNewTheme = true }, style = MaterialTheme.typography.bodyLarge)
                }

                if (isNewTheme) {
                    OutlinedTextField(
                        value = newThemeName,
                        onValueChange = { newThemeName = it },
                        label = { Text("New Theme Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isNewTheme) {
                        if (newThemeName.isNotBlank()) onConfirm(null, newThemeName, reference, content)
                    } else {
                        onConfirm(selectedNoteId, null, reference, content)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Import", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.outline)
            }
        }
    )
}
