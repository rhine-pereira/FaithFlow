package com.rhinepereira.faithflow.ui.notes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue

/**
 * Snapshot of note editing state capturing both title and note content (including selection/cursor).
 */
data class NoteSnapshot(
    val title: String,
    val content: TextFieldValue
)

/**
 * Manages Undo and Redo operations for the note editor.
 *
 * Provides smart grouping of typing keystrokes (by pause duration, word boundary, or edit direction)
 * so that single keystrokes do not pollute the history, while discrete actions (markdown formatting,
 * verse insertion, lists) are recorded as immediate checkpoints.
 */
class NoteUndoRedoManager(
    initialState: NoteSnapshot,
    private val maxHistory: Int = 100
) {
    var currentState by mutableStateOf(initialState)
        private set

    val title: String get() = currentState.title
    val content: TextFieldValue get() = currentState.content

    private val undoStack = ArrayDeque<NoteSnapshot>()
    private val redoStack = ArrayDeque<NoteSnapshot>()

    var canUndo by mutableStateOf(false)
        private set

    var canRedo by mutableStateOf(false)
        private set

    private var baselineState: NoteSnapshot = initialState
    private var isBatchInProgress = false
    private var lastEditTimeMs = 0L
    private var lastEditType = EditType.NONE

    private enum class EditType {
        NONE, INSERT, DELETE, OTHER
    }

    private fun updateFlags() {
        canUndo = undoStack.isNotEmpty()
        canRedo = redoStack.isNotEmpty()
    }

    /**
     * Records changes to the note title, batching typing increments.
     */
    fun onTitleChange(newTitle: String) {
        if (newTitle == currentState.title) return

        val now = System.currentTimeMillis()
        val textDiff = newTitle.length - currentState.title.length
        val editType = when {
            textDiff == 1 -> EditType.INSERT
            textDiff == -1 -> EditType.DELETE
            else -> EditType.OTHER
        }

        val shouldStartNewBatch = !isBatchInProgress ||
            (now - lastEditTimeMs > 800L) ||
            (editType != lastEditType) ||
            (editType == EditType.OTHER)

        if (shouldStartNewBatch) {
            pushToUndo(baselineState)
            baselineState = currentState
            isBatchInProgress = true
        }

        lastEditTimeMs = now
        lastEditType = editType
        currentState = currentState.copy(title = newTitle)

        if (newTitle.endsWith(" ") || newTitle.endsWith("\n")) {
            isBatchInProgress = false
            baselineState = currentState
        }
    }

    /**
     * Records changes to the note content, batching text increments while ignoring pure cursor moves.
     */
    fun onContentChange(newContent: TextFieldValue) {
        // If text content is identical (selection / cursor movement only), preserve selection without dirtying undo
        if (newContent.text == currentState.content.text) {
            currentState = currentState.copy(content = newContent)
            return
        }

        val now = System.currentTimeMillis()
        val oldText = currentState.content.text
        val newText = newContent.text
        val textDiff = newText.length - oldText.length

        val editType = when {
            textDiff == 1 -> EditType.INSERT
            textDiff == -1 -> EditType.DELETE
            else -> EditType.OTHER
        }

        val shouldStartNewBatch = !isBatchInProgress ||
            (now - lastEditTimeMs > 800L) ||
            (editType != lastEditType) ||
            (editType == EditType.OTHER)

        if (shouldStartNewBatch) {
            pushToUndo(baselineState)
            baselineState = currentState
            isBatchInProgress = true
        }

        lastEditTimeMs = now
        lastEditType = editType
        currentState = currentState.copy(content = newContent)

        // Word boundary: space or newline marks completion of a word batch
        if (textDiff == 1 && newContent.selection.start > 0) {
            val charTyped = newText[newContent.selection.start - 1]
            if (charTyped == ' ' || charTyped == '\n') {
                isBatchInProgress = false
                baselineState = currentState
            }
        }
    }

    /**
     * Used for programmatic edits such as Bold, Italic, Numbered List, or Verse insertion.
     */
    fun recordExplicitContentChange(newContent: TextFieldValue) {
        isBatchInProgress = false
        pushToUndo(currentState)
        currentState = currentState.copy(content = newContent)
        baselineState = currentState
    }

    private fun pushToUndo(snapshot: NoteSnapshot) {
        undoStack.addLast(snapshot)
        if (undoStack.size > maxHistory) {
            undoStack.removeFirst()
        }
        redoStack.clear()
        updateFlags()
    }

    /**
     * Reverts to the previous snapshot in the undo history.
     */
    fun undo() {
        if (undoStack.isEmpty()) return
        isBatchInProgress = false
        val previous = undoStack.removeLast()
        redoStack.addLast(currentState)
        if (redoStack.size > maxHistory) {
            redoStack.removeFirst()
        }
        currentState = previous
        baselineState = previous
        updateFlags()
    }

    /**
     * Re-applies the next snapshot in the redo history.
     */
    fun redo() {
        if (redoStack.isEmpty()) return
        isBatchInProgress = false
        val next = redoStack.removeLast()
        undoStack.addLast(currentState)
        if (undoStack.size > maxHistory) {
            undoStack.removeFirst()
        }
        currentState = next
        baselineState = next
        updateFlags()
    }
}

/**
 * Creates and remembers a [NoteUndoRedoManager] across recompositions.
 */
@Composable
fun rememberNoteUndoRedoManager(
    initialTitle: String,
    initialContent: TextFieldValue
): NoteUndoRedoManager {
    return remember {
        NoteUndoRedoManager(
            initialState = NoteSnapshot(
                title = initialTitle,
                content = initialContent
            )
        )
    }
}
