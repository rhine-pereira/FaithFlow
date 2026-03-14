package com.rhinepereira.versetrack.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rhinepereira.versetrack.data.PersonalNote
import com.rhinepereira.versetrack.data.PersonalNoteCategory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(viewModel: NotesViewModel = viewModel()) {
    val categories by viewModel.categories.collectAsState()
    var noteToEdit by remember { mutableStateOf<PersonalNote?>(null) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<PersonalNote?>(null) }

    val pagerState = rememberPagerState(pageCount = { categories.size })
    val coroutineScope = rememberCoroutineScope()

    if (noteToEdit != null) {
        Dialog(
            onDismissRequest = { 
                noteToEdit = null 
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            FullScreenNoteEditor(
                note = noteToEdit!!,
                onDismiss = { noteToEdit = null },
                onSave = { title, content ->
                    viewModel.updateNote(noteToEdit!!.copy(title = title, content = content))
                }
            )
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (categories.isNotEmpty()) {
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                edgePadding = 16.dp,
                divider = {}
            ) {
                categories.forEachIndexed { index, category ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(category.name) }
                    )
                }
                Tab(
                    selected = false,
                    onClick = { showAddCategoryDialog = true },
                    text = { Icon(Icons.Default.Add, contentDescription = "Add Category") }
                )
            }
        } else {
            // Initial state if categories haven't loaded yet
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        Box(modifier = Modifier.weight(1f)) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { pageIndex ->
                val category = categories[pageIndex]
                val notes by viewModel.getNotesForCategory(category.id).collectAsState(initial = emptyList())
                
                if (notes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No notes in ${category.name} yet.", color = MaterialTheme.colorScheme.outline)
                    }
                } else {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalItemSpacing = 12.dp
                    ) {
                        items(notes, key = { it.id }) { note ->
                            KeepNoteItem(
                                note = note,
                                onClick = { noteToEdit = note },
                                onDelete = { noteToDelete = note }
                            )
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = { 
                    val calendar = Calendar.getInstance()
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    
                    val currentCategoryId = categories.getOrNull(pagerState.currentPage)?.id ?: ""
                    
                    noteToEdit = PersonalNote(
                        categoryId = currentCategoryId, 
                        title = "", 
                        content = "",
                        date = calendar.timeInMillis
                    ) 
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Note")
            }
        }
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onConfirm = { name ->
                viewModel.addCategory(name)
                showAddCategoryDialog = false
            }
        )
    }

    noteToDelete?.let { note ->
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = { Text("Delete Note") },
            text = { Text("Are you sure you want to delete this note?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteNote(note)
                        noteToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun KeepNoteItem(note: PersonalNote, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                if (note.title.isNotBlank()) {
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                }
            }
            if (note.title.isNotBlank()) Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 8,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            val df = SimpleDateFormat("dd MMM", Locale.getDefault())
            Text(
                text = df.format(Date(note.date)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenNoteEditor(
    note: PersonalNote,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var title by remember { mutableStateOf(note.title) }
    var contentValue by remember { mutableStateOf(TextFieldValue(note.content)) }
    val scrollState = rememberScrollState()

    // Auto-save logic: Debounced save to Room
    LaunchedEffect(title, contentValue.text) {
        if (title != note.title || contentValue.text != note.content) {
            delay(1000) // Wait for 1 second of inactivity
            onSave(title, contentValue.text)
        }
    }

    // Final save on exit
    val dismissAndSave = {
        onSave(title, contentValue.text)
        onDismiss()
    }

    BackHandler { dismissAndSave() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = dismissAndSave) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.ime)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = { contentValue = applyFormat(contentValue, "**") }) {
                        Icon(Icons.Default.FormatBold, contentDescription = "Bold")
                    }
                    IconButton(onClick = { contentValue = applyFormat(contentValue, "_") }) {
                        Icon(Icons.Default.FormatItalic, contentDescription = "Italic")
                    }
                    IconButton(onClick = { 
                        val newText = if (contentValue.text.endsWith("\n") || contentValue.text.isEmpty()) {
                            contentValue.text + "1. "
                        } else {
                            contentValue.text + "\n1. "
                        }
                        contentValue = contentValue.copy(text = newText, selection = TextRange(newText.length))
                    }) {
                        Icon(Icons.Default.FormatListNumbered, contentDescription = "Numbered List")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Title", style = MaterialTheme.typography.headlineSmall) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = MaterialTheme.typography.headlineSmall,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )
            TextField(
                value = contentValue,
                onValueChange = { newValue ->
                    contentValue = handleAutoList(contentValue, newValue)
                },
                placeholder = { Text("Note") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

fun handleAutoList(oldValue: TextFieldValue, newValue: TextFieldValue): TextFieldValue {
    // Only trigger if a single character was added and it's a newline
    if (newValue.text.length != oldValue.text.length + 1) return newValue
    if (newValue.text[newValue.selection.start - 1] != '\n') return newValue

    val textBeforeNewline = newValue.text.substring(0, newValue.selection.start - 1)
    val lastLineStart = textBeforeNewline.lastIndexOf('\n') + 1
    val lastLine = textBeforeNewline.substring(lastLineStart)

    // Check for ordered list: "1. "
    val orderedListRegex = Regex("""^(\d+)\.\s+(.*)$""")
    val orderedMatch = orderedListRegex.find(lastLine)
    if (orderedMatch != null) {
        val number = orderedMatch.groupValues[1].toInt()
        val content = orderedMatch.groupValues[2]
        
        if (content.isEmpty()) {
            // If the line was just "1. ", pressing enter removes it
            val newText = newValue.text.substring(0, lastLineStart) + newValue.text.substring(newValue.selection.start)
            return newValue.copy(text = newText, selection = TextRange(lastLineStart))
        }
        
        val prefix = "${number + 1}. "
        val newText = newValue.text.substring(0, newValue.selection.start) + prefix + newValue.text.substring(newValue.selection.start)
        return newValue.copy(text = newText, selection = TextRange(newValue.selection.start + prefix.length))
    }

    // Check for bullet list: "- " or "* "
    val bulletListRegex = Regex("""^([-*])\s+(.*)$""")
    val bulletMatch = bulletListRegex.find(lastLine)
    if (bulletMatch != null) {
        val bullet = bulletMatch.groupValues[1]
        val content = bulletMatch.groupValues[2]
        
        if (content.isEmpty()) {
            val newText = newValue.text.substring(0, lastLineStart) + newValue.text.substring(newValue.selection.start)
            return newValue.copy(text = newText, selection = TextRange(lastLineStart))
        }
        
        val prefix = "$bullet "
        val newText = newValue.text.substring(0, newValue.selection.start) + prefix + newValue.text.substring(newValue.selection.start)
        return newValue.copy(text = newText, selection = TextRange(newValue.selection.start + prefix.length))
    }
    
    // Check for checklist: "- [ ] "
    val checklistRegex = Regex("""^(-\s\[\s\]\s)(.*)$""")
    val checklistMatch = checklistRegex.find(lastLine)
    if (checklistMatch != null) {
        val prefix = checklistMatch.groupValues[1]
        val content = checklistMatch.groupValues[2]
        
        if (content.isEmpty()) {
            val newText = newValue.text.substring(0, lastLineStart) + newValue.text.substring(newValue.selection.start)
            return newValue.copy(text = newText, selection = TextRange(lastLineStart))
        }
        
        val newText = newValue.text.substring(0, newValue.selection.start) + prefix + newValue.text.substring(newValue.selection.start)
        return newValue.copy(text = newText, selection = TextRange(newValue.selection.start + prefix.length))
    }

    return newValue
}

fun applyFormat(value: TextFieldValue, symbol: String): TextFieldValue {
    val selection = value.selection
    val text = value.text
    
    val formatted = if (selection.collapsed) {
        text.substring(0, selection.start) + symbol + symbol + text.substring(selection.end)
    } else {
        text.substring(0, selection.start) + symbol + text.substring(selection.start, selection.end) + symbol + text.substring(selection.end)
    }
    
    val newCursorPos = if (selection.collapsed) selection.start + symbol.length else selection.end + symbol.length * 2
    return value.copy(text = formatted, selection = TextRange(newCursorPos))
}

@Composable
fun AddCategoryDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Category") },
        text = {
            TextField(
                value = name, 
                onValueChange = { name = it }, 
                label = { Text("Category Name") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onConfirm(name) }) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
