package com.rhinepereira.faithflow.ui.notes

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material3.VerticalDivider
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import com.rhinepereira.faithflow.data.BibleDatabaseHelper
import com.rhinepereira.faithflow.data.PersonalNote
import com.rhinepereira.faithflow.ui.TutorialStep
import com.rhinepereira.faithflow.ui.tutorialTarget
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Full-screen markdown-capable editor for creating and editing notes.
 * Automatically detects Bible references, supports auto-saving, and provides formatting actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenNoteEditor(
    note: PersonalNote,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    val undoManager = rememberNoteUndoRedoManager(
        initialTitle = note.title,
        initialContent = TextFieldValue(note.content)
    )
    val context = LocalContext.current
    val bibleHelper = remember { BibleDatabaseHelper(context) }
    val boldColor = MaterialTheme.colorScheme.primary

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var detectedReference by remember { mutableStateOf<BibleRef?>(null) }

    LaunchedEffect(undoManager.title, undoManager.content.text) {
        if (undoManager.title != note.title || undoManager.content.text != note.content) {
            delay(500)  // Reduced from 5000ms to 500ms to minimize data loss risk
            onSave(undoManager.title, undoManager.content.text)
        }
    }

    LaunchedEffect(undoManager.content) {
        val text = undoManager.content.text
        val selection = undoManager.content.selection
        if (selection.collapsed && text.isNotEmpty()) {
            val textBeforeCursor = text.take(selection.start)
            val lastLine = textBeforeCursor.split("\n").lastOrNull() ?: ""

            if (lastLine.isNotBlank() && !lastLine.contains(" - ") && !lastLine.contains("**")) {
                detectedReference = findBibleReference(lastLine)
            } else {
                detectedReference = null
            }
        } else {
            detectedReference = null
        }
    }

    val dismissAndSave = {
        onSave(undoManager.title, undoManager.content.text)
        onDismiss()
    }

    BackHandler { dismissAndSave() }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown && keyEvent.isCtrlPressed) {
                    when {
                        keyEvent.key == Key.Z && keyEvent.isShiftPressed -> {
                            if (undoManager.canRedo) {
                                undoManager.redo()
                                true
                            } else false
                        }
                        keyEvent.key == Key.Z -> {
                            if (undoManager.canUndo) {
                                undoManager.undo()
                                true
                            } else false
                        }
                        keyEvent.key == Key.Y -> {
                            if (undoManager.canRedo) {
                                undoManager.redo()
                                true
                            } else false
                        }
                        else -> false
                    }
                } else false
            },
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Column {
                BibleReferenceBar(
                    reference = detectedReference,
                    onIgnore = { detectedReference = null },
                    onAddVerse = { ref ->
                        val fetched = bibleHelper.getVerses(ref.book, ref.chapter, ref.verses)
                        if (fetched != null) {
                            val newContent = insertBibleVerse(undoManager.content, ref, fetched)
                            undoManager.recordExplicitContentChange(newContent)

                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "Verse added",
                                    actionLabel = "Undo",
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    undoManager.undo()
                                }
                            }
                        }
                        detectedReference = null
                    }
                )

                FormattingToolbar(
                    canUndo = undoManager.canUndo,
                    canRedo = undoManager.canRedo,
                    onUndoClick = { undoManager.undo() },
                    onRedoClick = { undoManager.redo() },
                    onBoldClick = { undoManager.recordExplicitContentChange(applyFormat(undoManager.content, "**")) },
                    onItalicClick = { undoManager.recordExplicitContentChange(applyFormat(undoManager.content, "_")) },
                    onNumberedListClick = {
                        val currentText = undoManager.content.text
                        val newText = if (currentText.endsWith("\n") || currentText.isEmpty()) {
                            currentText + "1. "
                        } else {
                            currentText + "\n1. "
                        }
                        val newContent = undoManager.content.copy(
                            text = newText,
                            selection = TextRange(newText.length)
                        )
                        undoManager.recordExplicitContentChange(newContent)
                    }
                )
            }
        },
        contentWindowInsets = WindowInsets.statusBars
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            TextField(
                value = undoManager.title,
                onValueChange = { undoManager.onTitleChange(it) },
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
                value = undoManager.content,
                onValueChange = { newValue ->
                    val processed = handleAutoList(undoManager.content, newValue)
                    undoManager.onContentChange(processed)
                },
                placeholder = { Text("Note") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .tutorialTarget(TutorialStep.RICH_TEXT_EDITOR),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                visualTransformation = MarkdownVisualTransformation(boldColor)
            )
        }
    }
}

/**
 * Animated banner suggesting to insert a detected Bible reference into the editor.
 */
@Composable
fun BibleReferenceBar(
    reference: BibleRef?,
    onIgnore: () -> Unit,
    onAddVerse: (BibleRef) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = reference != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        reference?.let { ref ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add ${ref.originalText}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row {
                        TextButton(onClick = onIgnore) {
                            Text("Ignore")
                        }
                        Button(onClick = { onAddVerse(ref) }) {
                            Text("Add")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Bottom action toolbar containing markdown quick-actions and undo/redo buttons.
 */
@Composable
fun FormattingToolbar(
    canUndo: Boolean = false,
    canRedo: Boolean = false,
    onUndoClick: () -> Unit = {},
    onRedoClick: () -> Unit = {},
    onBoldClick: () -> Unit,
    onItalicClick: () -> Unit,
    onNumberedListClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.ime)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onUndoClick,
                enabled = canUndo
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "Undo"
                )
            }
            IconButton(
                onClick = onRedoClick,
                enabled = canRedo
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Redo,
                    contentDescription = "Redo"
                )
            }
            VerticalDivider(
                modifier = Modifier
                    .height(24.dp)
                    .padding(horizontal = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            IconButton(onClick = onBoldClick) {
                Icon(Icons.Default.FormatBold, contentDescription = "Bold")
            }
            IconButton(onClick = onItalicClick) {
                Icon(Icons.Default.FormatItalic, contentDescription = "Italic")
            }
            IconButton(onClick = onNumberedListClick) {
                Icon(Icons.Default.FormatListNumbered, contentDescription = "Numbered List")
            }
        }
    }
}
