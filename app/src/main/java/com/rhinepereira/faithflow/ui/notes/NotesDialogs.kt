package com.rhinepereira.faithflow.ui.notes

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.rhinepereira.faithflow.data.PersonalNoteCategory
import kotlinx.coroutines.isActive

/**
 * Dialog prompting user to input a name for a new category.
 */
@Composable
fun AddCategoryDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Category", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Category Name") },
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
                onClick = { if (name.isNotBlank()) onConfirm(name.trim()) },
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
 * Dialog enabling drag-and-drop reordering, renaming, and deletion of categories.
 */
@Composable
fun ReorderCategoriesDialog(
    categories: List<PersonalNoteCategory>,
    onDismiss: () -> Unit,
    onConfirm: (List<PersonalNoteCategory>) -> Unit,
    onDelete: (PersonalNoteCategory) -> Unit,
    onRename: (PersonalNoteCategory) -> Unit
) {
    val editableCategories = remember { mutableStateListOf<PersonalNoteCategory>().apply { addAll(categories) } }
    var draggedCategoryId by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableStateOf(0f) }
    var currentPointerYInContainer by remember { mutableStateOf<Float?>(null) }
    var containerCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val itemCoordinatesMap = remember { mutableMapOf<String, LayoutCoordinates>() }
    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    val itemHeight = 64.dp
    val spacingHeight = 8.dp
    val totalItemHeightPx = with(density) { (itemHeight + spacingHeight).toPx() }

    fun reorderDraggedItem(catId: String) {
        val currentIndex = editableCategories.indexOfFirst { it.id == catId }
        if (currentIndex != -1 && totalItemHeightPx > 0f) {
            val displacement = (dragOffset / totalItemHeightPx).toInt()
            val targetIndex = (currentIndex + displacement).coerceIn(0, editableCategories.lastIndex)
            if (targetIndex != currentIndex) {
                val item = editableCategories.removeAt(currentIndex)
                editableCategories.add(targetIndex, item)
                dragOffset -= (targetIndex - currentIndex) * totalItemHeightPx
            }
        }
    }

    fun handleDrag(catId: String, change: PointerInputChange, dragAmountY: Float) {
        change.consume()
        dragOffset += dragAmountY

        val itemCoords = itemCoordinatesMap[catId]
        val containerCoords = containerCoordinates
        if (itemCoords != null && itemCoords.isAttached && containerCoords != null && containerCoords.isAttached) {
            val localTouch = containerCoords.localPositionOf(itemCoords, change.position)
            currentPointerYInContainer = localTouch.y
        }

        reorderDraggedItem(catId)
    }

    LaunchedEffect(draggedCategoryId) {
        val activeId = draggedCategoryId ?: run {
            currentPointerYInContainer = null
            return@LaunchedEffect
        }
        val edgeThresholdPx = with(density) { 56.dp.toPx() }
        val maxScrollPerFrame = with(density) { 14.dp.toPx() }

        while (isActive && draggedCategoryId == activeId) {
            val pointerY = currentPointerYInContainer
            val containerHeight = containerCoordinates?.size?.height?.toFloat() ?: 0f

            if (pointerY != null && containerHeight > 0f) {
                val scrollDelta = when {
                    pointerY < edgeThresholdPx && scrollState.value > 0 -> {
                        val factor = ((edgeThresholdPx - pointerY) / edgeThresholdPx).coerceIn(0f, 1f)
                        -((factor * maxScrollPerFrame).coerceAtLeast(1f))
                    }
                    pointerY > (containerHeight - edgeThresholdPx) && scrollState.value < scrollState.maxValue -> {
                        val factor = ((pointerY - (containerHeight - edgeThresholdPx)) / edgeThresholdPx).coerceIn(0f, 1f)
                        ((factor * maxScrollPerFrame).coerceAtLeast(1f))
                    }
                    else -> 0f
                }

                if (scrollDelta != 0f) {
                    val prevScroll = scrollState.value
                    scrollState.scrollBy(scrollDelta)
                    val consumedScroll = (scrollState.value - prevScroll).toFloat()
                    if (consumedScroll != 0f) {
                        dragOffset += consumedScroll
                        reorderDraggedItem(activeId)
                    }
                }
            }
            withFrameMillis { }
        }
    }

    LaunchedEffect(categories) {
        val currentIds = categories.map { it.id }.toSet()
        editableCategories.removeAll { it.id !in currentIds }
        // Update names for existing categories while preserving order of items in editableCategories
        categories.forEach { source ->
            val index = editableCategories.indexOfFirst { it.id == source.id }
            if (index != -1 && editableCategories[index].name != source.name) {
                editableCategories[index] = editableCategories[index].copy(name = source.name)
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.7f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Text(
                    "Reorder Categories",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Drag handle or long press to reorder",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (editableCategories.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No categories available", color = MaterialTheme.colorScheme.outline)
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .onGloballyPositioned { coords ->
                                containerCoordinates = coords
                            }
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(spacingHeight)
                    ) {
                        editableCategories.forEach { category ->
                            key(category.id) {
                                val isDragging = draggedCategoryId == category.id

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    tonalElevation = if (isDragging) 16.dp else 2.dp,
                                    shadowElevation = if (isDragging) 24.dp else 0.dp,
                                    color = if (isDragging)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .zIndex(if (isDragging) 10f else 0f)
                                        .graphicsLayer {
                                            if (isDragging) {
                                                translationY = dragOffset
                                                scaleX = 1.03f
                                                scaleY = 1.03f
                                            }
                                        }
                                        .onGloballyPositioned { coords ->
                                            itemCoordinatesMap[category.id] = coords
                                        }
                                        .pointerInput(category.id) {
                                            detectDragGesturesAfterLongPress(
                                                onDragStart = {
                                                    draggedCategoryId = category.id
                                                    dragOffset = 0f
                                                },
                                                onDragEnd = {
                                                    draggedCategoryId = null
                                                    dragOffset = 0f
                                                    currentPointerYInContainer = null
                                                },
                                                onDragCancel = {
                                                    draggedCategoryId = null
                                                    dragOffset = 0f
                                                    currentPointerYInContainer = null
                                                },
                                                onDrag = { change, dragAmount ->
                                                    handleDrag(category.id, change, dragAmount.y)
                                                }
                                            )
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(itemHeight)
                                            .padding(horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.DragHandle,
                                            contentDescription = "Drag to reorder",
                                            tint = if (isDragging)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            modifier = Modifier
                                                .size(32.dp)
                                                .padding(4.dp)
                                                .pointerInput(category.id) {
                                                    detectDragGestures(
                                                        onDragStart = {
                                                            draggedCategoryId = category.id
                                                            dragOffset = 0f
                                                        },
                                                        onDragEnd = {
                                                            draggedCategoryId = null
                                                            dragOffset = 0f
                                                            currentPointerYInContainer = null
                                                        },
                                                        onDragCancel = {
                                                            draggedCategoryId = null
                                                            dragOffset = 0f
                                                            currentPointerYInContainer = null
                                                        },
                                                        onDrag = { change, dragAmount ->
                                                            handleDrag(category.id, change, dragAmount.y)
                                                        }
                                                    )
                                                }
                                        )
                                        Text(
                                            text = category.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = if (isDragging) FontWeight.Bold else FontWeight.Normal,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = { if (draggedCategoryId == null) onRename(category) },
                                            enabled = draggedCategoryId == null,
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Rename Category",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { if (draggedCategoryId == null) onDelete(category) },
                                            enabled = draggedCategoryId == null,
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete Category",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MaterialTheme.colorScheme.outline)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(editableCategories.toList()) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
