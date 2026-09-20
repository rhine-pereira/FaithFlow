package com.rhinepereira.faithflow.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rhinepereira.faithflow.data.Note
import com.rhinepereira.faithflow.data.NoteWithVerses
import com.rhinepereira.faithflow.data.Verse
import com.rhinepereira.faithflow.ui.components.DeleteConfirmationDialog
import com.rhinepereira.faithflow.ui.components.RenameThemeDialog
import com.rhinepereira.faithflow.ui.verse.AddNoteDialog
import com.rhinepereira.faithflow.ui.verse.AddVerseDialog
import com.rhinepereira.faithflow.ui.verse.EditVerseDialog
import com.rhinepereira.faithflow.ui.verse.SharedTextDialog
import com.rhinepereira.faithflow.ui.verse.ThemeCard
import com.rhinepereira.faithflow.ui.verse.VerseItem
import com.rhinepereira.faithflow.ui.verse.move
import kotlinx.coroutines.launch

/**
 * Screen displaying thematic collections of Bible verses in a reorderable grid,
 * with navigation to full verse lists for each theme.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerseScreen(
    viewModel: VerseViewModel = viewModel(),
    sharedText: String? = null,
    onSharedTextConsumed: () -> Unit = {},
    isVisible: Boolean = true
) {
    var selectedNoteWithVerses by remember { mutableStateOf<NoteWithVerses?>(null) }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var showAddVerseDialog by remember { mutableStateOf(false) }
    var showSharedTextDialog by remember { mutableStateOf(false) }
    var verseToEdit by remember { mutableStateOf<Verse?>(null) }

    // Deletion confirmation states
    var noteToDelete by remember { mutableStateOf<Note?>(null) }
    var noteToRename by remember { mutableStateOf<Note?>(null) }
    var verseToDelete by remember { mutableStateOf<Verse?>(null) }

    val notesWithVerses = viewModel.allNotesWithVerses.collectAsStateWhenVisible(isVisible)
    var orderedNotesWithVerses by remember { mutableStateOf(emptyList<NoteWithVerses>()) }
    var draggedThemeId by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var accumulatedDrag by remember { mutableStateOf(Offset.Zero) }
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val gridState = rememberLazyGridState()

    LaunchedEffect(notesWithVerses, draggedThemeId) {
        if (draggedThemeId == null) {
            orderedNotesWithVerses = viewModel.applySavedThemeOrder(notesWithVerses)
        }
    }

    LaunchedEffect(sharedText) {
        if (sharedText != null) {
            showSharedTextDialog = true
        }
    }

    Scaffold(
        topBar = {
            selectedNoteWithVerses?.let { currentNoteWithVerses ->
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(currentNoteWithVerses.note.theme)
                            IconButton(onClick = { noteToRename = currentNoteWithVerses.note }) {
                                Icon(Icons.Default.Edit, contentDescription = "Rename Theme", modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { selectedNoteWithVerses = null }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedNoteWithVerses == null) showAddNoteDialog = true else showAddVerseDialog = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            val currentSelectedTheme = selectedNoteWithVerses
            if (currentSelectedTheme == null) {
                // Themes Grid
                val displayedThemes = if (orderedNotesWithVerses.isEmpty() && notesWithVerses.isNotEmpty()) {
                    viewModel.applySavedThemeOrder(notesWithVerses)
                } else {
                    orderedNotesWithVerses
                }

                if (displayedThemes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No themes yet. Add one to start!", color = MaterialTheme.colorScheme.outline)
                    }
                } else {
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val dragStepX = with(density) { (((maxWidth - 32.dp - 12.dp) / 2f) + 12.dp).toPx() }
                        val dragStepY = with(density) { (180.dp + 12.dp).toPx() }
                        val edgeThresholdPx = with(density) { 96.dp.toPx() }

                        LazyVerticalGrid(
                            state = gridState,
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            itemsIndexed(displayedThemes, key = { _, item -> item.note.id }) { _, noteWithVerses ->
                                val isDragging = draggedThemeId == noteWithVerses.note.id

                                ThemeCard(
                                    noteWithVerses = noteWithVerses,
                                    onClick = { selectedNoteWithVerses = noteWithVerses },
                                    onDelete = { noteToDelete = noteWithVerses.note },
                                    onRename = { noteToRename = noteWithVerses.note },
                                    isDragging = isDragging,
                                    modifier = Modifier
                                        .zIndex(if (isDragging) 2f else 0f)
                                        .graphicsLayer {
                                            if (isDragging) {
                                                translationX = dragOffset.x
                                                translationY = dragOffset.y
                                                scaleX = 1.035f
                                                scaleY = 1.035f
                                            }
                                        }
                                        .pointerInput(noteWithVerses.note.id) {
                                            detectDragGesturesAfterLongPress(
                                                onDragStart = {
                                                    draggedThemeId = noteWithVerses.note.id
                                                    dragOffset = Offset.Zero
                                                    accumulatedDrag = Offset.Zero
                                                },
                                                onDragEnd = {
                                                    if (draggedThemeId != null) {
                                                        viewModel.persistThemeOrder(orderedNotesWithVerses.map { it.note.id })
                                                    }
                                                    draggedThemeId = null
                                                    dragOffset = Offset.Zero
                                                    accumulatedDrag = Offset.Zero
                                                },
                                                onDragCancel = {
                                                    draggedThemeId = null
                                                    dragOffset = Offset.Zero
                                                    accumulatedDrag = Offset.Zero
                                                },
                                                onDrag = { change, dragAmount ->
                                                    change.consume()
                                                    if (draggedThemeId != noteWithVerses.note.id) return@detectDragGesturesAfterLongPress

                                                    dragOffset = Offset(
                                                        dragOffset.x + dragAmount.x,
                                                        dragOffset.y + dragAmount.y
                                                    )
                                                    accumulatedDrag = Offset(
                                                        accumulatedDrag.x + dragAmount.x,
                                                        accumulatedDrag.y + dragAmount.y
                                                    )

                                                    // Edge auto-scroll like Keep
                                                    val yInGrid = change.position.y
                                                    val nearTop = yInGrid < edgeThresholdPx
                                                    val nearBottom = yInGrid > (size.height - edgeThresholdPx)
                                                    if (nearTop || nearBottom) {
                                                        scope.launch {
                                                            gridState.scrollBy(if (nearTop) -32f else 32f)
                                                        }
                                                    }

                                                    val currentThemes = orderedNotesWithVerses
                                                    val currentIndex = currentThemes.indexOfFirst { it.note.id == noteWithVerses.note.id }
                                                    if (currentIndex < 0) return@detectDragGesturesAfterLongPress

                                                    val horizontalSteps = (accumulatedDrag.x / dragStepX).toInt()
                                                    val verticalSteps = (accumulatedDrag.y / dragStepY).toInt()
                                                    if (horizontalSteps == 0 && verticalSteps == 0) return@detectDragGesturesAfterLongPress

                                                    val targetIndex = (currentIndex + horizontalSteps + (verticalSteps * 2))
                                                        .coerceIn(0, currentThemes.lastIndex)
                                                    if (targetIndex != currentIndex) {
                                                        orderedNotesWithVerses = currentThemes.toMutableList().apply {
                                                            move(currentIndex, targetIndex)
                                                        }
                                                    }

                                                    accumulatedDrag = Offset(
                                                        accumulatedDrag.x - (horizontalSteps * dragStepX),
                                                        accumulatedDrag.y - (verticalSteps * dragStepY)
                                                    )
                                                }
                                            )
                                        }
                                )
                            }
                        }
                    }
                }
            } else {
                // Verses List for selected Theme
                val verses = viewModel.getVersesForNote(currentSelectedTheme.note.id)
                    .collectAsStateWhenVisible(isVisible, emptyList())
                BackHandler { selectedNoteWithVerses = null }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(verses, key = { it.id }) { verse ->
                        VerseItem(
                            verse = verse,
                            onDelete = { verseToDelete = verse },
                            onEdit = { verseToEdit = verse }
                        )
                    }
                }
            }
        }

        if (showAddNoteDialog) {
            AddNoteDialog(
                onDismiss = { showAddNoteDialog = false },
                onConfirm = { theme ->
                    viewModel.addNote(theme)
                    showAddNoteDialog = false
                }
            )
        }

        if (showAddVerseDialog && selectedNoteWithVerses != null) {
            selectedNoteWithVerses?.let { currentTheme ->
                AddVerseDialog(
                    onDismiss = { showAddVerseDialog = false },
                    onConfirm = { reference, content ->
                        viewModel.addVerse(currentTheme.note.id, reference, content)
                        showAddVerseDialog = false
                    }
                )
            }
        }

        verseToEdit?.let { currentVerse ->
            EditVerseDialog(
                verse = currentVerse,
                onDismiss = { verseToEdit = null },
                onConfirm = { updatedReference, updatedContent ->
                    viewModel.updateVerse(currentVerse.copy(reference = updatedReference, content = updatedContent))
                    verseToEdit = null
                }
            )
        }

        if (showSharedTextDialog && sharedText != null) {
            SharedTextDialog(
                sharedText = sharedText,
                themes = notesWithVerses.map { it.note },
                onDismiss = {
                    showSharedTextDialog = false
                    onSharedTextConsumed()
                },
                onConfirm = { noteId, themeName, reference, content ->
                    if (noteId != null) {
                        viewModel.addVerse(noteId, reference, content)
                    } else if (themeName != null) {
                        viewModel.addNoteWithInitialVerse(themeName, reference, content)
                    }
                    showSharedTextDialog = false
                    onSharedTextConsumed()
                }
            )
        }

        noteToRename?.let { note ->
            RenameThemeDialog(
                currentName = note.theme,
                onDismiss = { noteToRename = null },
                onConfirm = { newName ->
                    viewModel.renameNote(note, newName)
                    // If current open theme is the one being renamed, update the screen state as well
                    if (selectedNoteWithVerses?.note?.id == note.id) {
                        selectedNoteWithVerses = selectedNoteWithVerses?.copy(
                            note = note.copy(theme = newName)
                        )
                    }
                    noteToRename = null
                }
            )
        }

        // Delete Confirmation Dialogs
        noteToDelete?.let { note ->
            DeleteConfirmationDialog(
                title = "Delete Theme",
                message = "Are you sure you want to delete the theme \"${note.theme}\" and all its verses?",
                onConfirm = {
                    viewModel.deleteNote(note)
                    noteToDelete = null
                },
                onDismiss = { noteToDelete = null }
            )
        }

        verseToDelete?.let { verse ->
            DeleteConfirmationDialog(
                title = "Delete Verse",
                message = "Are you sure you want to delete this verse (${verse.reference})?",
                onConfirm = {
                    viewModel.deleteVerse(verse)
                    verseToDelete = null
                },
                onDismiss = { verseToDelete = null }
            )
        }
    }
}
