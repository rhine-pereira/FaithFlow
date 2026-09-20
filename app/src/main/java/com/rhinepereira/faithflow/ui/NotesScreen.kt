package com.rhinepereira.faithflow.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rhinepereira.faithflow.data.PersonalNote
import com.rhinepereira.faithflow.data.PersonalNoteCategory
import com.rhinepereira.faithflow.ui.components.DeleteConfirmationDialog
import com.rhinepereira.faithflow.ui.components.RenameCategoryDialog
import com.rhinepereira.faithflow.ui.notes.AddCategoryDialog
import com.rhinepereira.faithflow.ui.notes.FullScreenNoteEditor
import com.rhinepereira.faithflow.ui.notes.NoteListItem
import com.rhinepereira.faithflow.ui.notes.ReorderCategoriesDialog
import com.rhinepereira.faithflow.ui.notes.plainTextPreview
import com.rhinepereira.faithflow.util.DateUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Creates a blank [PersonalNote] for today in the given category.
 */
private fun createNewNoteForToday(categoryId: String): PersonalNote =
    PersonalNote(
        categoryId = categoryId,
        title = "",
        content = "",
        date = DateUtils.getStartOfDay()
    )

/**
 * Screen displaying categorized personal notes in a grid, with swipeable category tabs.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun NotesScreen(
    viewModel: NotesViewModel = viewModel(),
    isVisible: Boolean = true
) {
    val categories = viewModel.categories.collectAsStateWhenVisible(isVisible)
    val allPersonalNotes = viewModel.allPersonalNotes.collectAsStateWhenVisible(isVisible)
    val dateFormat = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
    val noteCardsByCategory = remember(allPersonalNotes, dateFormat) {
        allPersonalNotes.groupBy { it.categoryId }.mapValues { (_, notes) ->
            notes.map { note ->
                NoteListItem(
                    note = note,
                    dateLabel = dateFormat.format(Date(note.date)),
                    preview = plainTextPreview(note.content)
                )
            }
        }
    }
    var noteToEdit by remember { mutableStateOf<PersonalNote?>(null) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<PersonalNote?>(null) }
    var categoryToDelete by remember { mutableStateOf<PersonalNoteCategory?>(null) }
    var categoryToRename by remember { mutableStateOf<PersonalNoteCategory?>(null) }
    var showReorderDialog by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            coroutineScope.launch {
                viewModel.syncFromCloud()
                isRefreshing = false
            }
        }
    )

    val pagerState = rememberPagerState(pageCount = { categories.size.coerceAtLeast(0) })
    var tabIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(pagerState, categories.size) {
        snapshotFlow { pagerState.settledPage }.collect { page ->
            if (page in categories.indices) {
                tabIndex = page
            }
        }
    }

    LaunchedEffect(categories.size) {
        if (categories.isNotEmpty() && pagerState.currentPage >= categories.size) {
            pagerState.scrollToPage(categories.lastIndex)
        }
    }

    noteToEdit?.let { currentNote ->
        Dialog(
            onDismissRequest = { noteToEdit = null },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            FullScreenNoteEditor(
                note = currentNote,
                onDismiss = { noteToEdit = null },
                onSave = { title, content ->
                    viewModel.updateNote(currentNote.copy(title = title, content = content))
                }
            )
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { showReorderDialog = true }
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Manage Categories",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(
                onClick = { showAddCategoryDialog = true },
                modifier = Modifier.tutorialTarget(TutorialStep.ADD_CATEGORY_BTN)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Category",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (categories.isNotEmpty()) {
            ScrollableTabRow(
                selectedTabIndex = tabIndex.coerceIn(0, categories.lastIndex),
                edgePadding = 16.dp,
                divider = {},
                containerColor = Color.Transparent,
                indicator = { tabPositions ->
                    if (tabIndex in tabPositions.indices) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[tabIndex]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            ) {
                categories.forEachIndexed { index, category ->
                    Tab(
                        selected = tabIndex == index,
                        onClick = {
                            tabIndex = index
                            coroutineScope.launch { pagerState.scrollToPage(index) }
                        },
                        text = {
                            Text(
                                text = category.name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = if (tabIndex == index) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outline
                                },
                                fontWeight = if (tabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            if (categories.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No categories yet. Add one above!", color = MaterialTheme.colorScheme.outline)
                }
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    beyondViewportPageCount = 1,
                    key = { page -> categories.getOrNull(page)?.id ?: page }
                ) { page ->
                    val category = categories.getOrNull(page)
                    val notesForThisCat = if (category != null) {
                        noteCardsByCategory[category.id] ?: emptyList()
                    } else emptyList()

                    CategoryNotesPage(
                        categoryId = category?.id ?: "",
                        items = notesForThisCat,
                        onNoteClick = { noteToEdit = it },
                        onNoteDelete = { noteToDelete = it }
                    )
                }
            }

            if (categories.isNotEmpty()) {
                FloatingActionButton(
                    onClick = {
                        val currentCategoryId = categories.getOrNull(tabIndex)?.id ?: ""
                        noteToEdit = createNewNoteForToday(currentCategoryId)
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .tutorialTarget(TutorialStep.ADD_NOTE_FAB)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Note")
                }
            }

            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
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
        DeleteConfirmationDialog(
            title = "Delete Note",
            message = "Are you sure you want to delete this note?",
            onConfirm = {
                viewModel.deleteNote(note)
                noteToDelete = null
            },
            onDismiss = { noteToDelete = null }
        )
    }

    categoryToDelete?.let { category ->
        DeleteConfirmationDialog(
            title = "Delete Category",
            message = "Are you sure you want to delete \"${category.name}\" and all its notes?",
            onConfirm = {
                viewModel.deleteCategory(category)
                categoryToDelete = null
            },
            onDismiss = { categoryToDelete = null }
        )
    }

    if (showReorderDialog) {
        ReorderCategoriesDialog(
            categories = categories,
            onDismiss = { showReorderDialog = false },
            onConfirm = { reorderedCategories ->
                val currentId = categories.getOrNull(tabIndex)?.id
                viewModel.reorderCategories(reorderedCategories)
                showReorderDialog = false
                currentId?.let { selectedId ->
                    val targetIndex = reorderedCategories.indexOfFirst { it.id == selectedId }
                    if (targetIndex >= 0) {
                        tabIndex = targetIndex
                        coroutineScope.launch { pagerState.scrollToPage(targetIndex) }
                    }
                }
            },
            onDelete = { category -> categoryToDelete = category },
            onRename = { category -> categoryToRename = category }
        )
    }

    categoryToRename?.let { category ->
        RenameCategoryDialog(
            currentName = category.name,
            onDismiss = { categoryToRename = null },
            onConfirm = { newName ->
                viewModel.renameCategory(category, newName)
                categoryToRename = null
            }
        )
    }
}

/**
 * Page displaying notes in a 2-column grid for a specific category.
 */
@Composable
private fun CategoryNotesPage(
    categoryId: String,
    items: List<NoteListItem>,
    onNoteClick: (PersonalNote) -> Unit,
    onNoteDelete: (PersonalNote) -> Unit
) {
    val gridState = remember(categoryId) { LazyGridState() }

    if (items.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No notes here yet.", color = MaterialTheme.colorScheme.outline)
        }
    } else {
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items, key = { it.note.id }) { item ->
                KeepNoteItem(
                    note = item.note,
                    preview = item.preview,
                    dateLabel = item.dateLabel,
                    onClick = { onNoteClick(item.note) },
                    onDelete = { onNoteDelete(item.note) }
                )
            }
        }
    }
}

/**
 * Card representing a single note in the grid with title, preview, and date label.
 */
@Composable
fun KeepNoteItem(
    note: PersonalNote,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    preview: String = plainTextPreview(note.content),
    dateLabel: String? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .graphicsLayer {
                shape = RoundedCornerShape(12.dp)
                clip = true
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            if (note.title.isNotBlank()) Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = preview,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 8,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = dateLabel ?: SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(note.date)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            )
        }
    }
}
