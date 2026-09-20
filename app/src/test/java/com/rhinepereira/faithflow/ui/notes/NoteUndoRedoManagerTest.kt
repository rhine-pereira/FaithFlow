package com.rhinepereira.faithflow.ui.notes

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteUndoRedoManagerTest {

    @Test
    fun initialState_hasNoUndoOrRedo() {
        val manager = NoteUndoRedoManager(
            initialState = NoteSnapshot(title = "Initial Title", content = TextFieldValue("Initial body"))
        )

        assertFalse("canUndo should initially be false", manager.canUndo)
        assertFalse("canRedo should initially be false", manager.canRedo)
        assertEquals("Initial Title", manager.title)
        assertEquals("Initial body", manager.content.text)
    }

    @Test
    fun typingWord_andWordBoundary_enablesUndoAndRedo() {
        val manager = NoteUndoRedoManager(
            initialState = NoteSnapshot(title = "", content = TextFieldValue(""))
        )

        // Simulate typing "Hello " character by character
        val helloWord = "Hello "
        for (i in 1..helloWord.length) {
            manager.onContentChange(TextFieldValue(helloWord.take(i), selection = TextRange(i)))
        }

        // Simulate typing "world" character by character
        val fullText = "Hello world"
        for (i in (helloWord.length + 1)..fullText.length) {
            manager.onContentChange(TextFieldValue(fullText.take(i), selection = TextRange(i)))
        }

        assertTrue("canUndo should be true after typing", manager.canUndo)
        assertEquals("Hello world", manager.content.text)

        // First undo should revert back to "Hello "
        manager.undo()
        assertEquals("Hello ", manager.content.text)
        assertTrue("canRedo should be true after undo", manager.canRedo)

        // Second undo should revert back to initial ""
        manager.undo()
        assertEquals("", manager.content.text)
        assertFalse("canUndo should be false after undoing all changes", manager.canUndo)

        // Redo should restore "Hello "
        manager.redo()
        assertEquals("Hello ", manager.content.text)

        // Redo again should restore "Hello world"
        manager.redo()
        assertEquals("Hello world", manager.content.text)
        assertFalse("canRedo should be false after redoing all changes", manager.canRedo)
    }

    @Test
    fun cursorMovement_doesNotDirtyUndoHistory() {
        val manager = NoteUndoRedoManager(
            initialState = NoteSnapshot(title = "", content = TextFieldValue("Existing text", selection = TextRange(0)))
        )

        // Just moving cursor
        manager.onContentChange(TextFieldValue("Existing text", selection = TextRange(5)))
        manager.onContentChange(TextFieldValue("Existing text", selection = TextRange(13)))

        assertFalse("Cursor movements alone must not create undo steps", manager.canUndo)
        assertEquals(13, manager.content.selection.start)
    }

    @Test
    fun explicitChange_recordsImmediateCheckpoint() {
        val manager = NoteUndoRedoManager(
            initialState = NoteSnapshot(title = "Note", content = TextFieldValue("Plain text"))
        )

        val formatted = TextFieldValue("**Plain text**", selection = TextRange(14))
        manager.recordExplicitContentChange(formatted)

        assertTrue("canUndo should be true after explicit format", manager.canUndo)
        assertEquals("**Plain text**", manager.content.text)

        manager.undo()
        assertEquals("Plain text", manager.content.text)
        assertTrue(manager.canRedo)

        manager.redo()
        assertEquals("**Plain text**", manager.content.text)
    }

    @Test
    fun newEdit_clearsRedoStack() {
        val manager = NoteUndoRedoManager(
            initialState = NoteSnapshot(title = "", content = TextFieldValue("Hello"))
        )

        manager.recordExplicitContentChange(TextFieldValue("Hello World"))
        manager.undo()
        assertEquals("Hello", manager.content.text)
        assertTrue("canRedo should be true after undo", manager.canRedo)

        // Now user makes a new edit
        manager.recordExplicitContentChange(TextFieldValue("Hello Universe"))
        assertFalse("New edit must invalidate/clear redo stack", manager.canRedo)
        assertEquals("Hello Universe", manager.content.text)
    }

    @Test
    fun titleEditing_supportsUndoAndRedo() {
        val manager = NoteUndoRedoManager(
            initialState = NoteSnapshot(title = "Old Title", content = TextFieldValue("Content"))
        )

        manager.onTitleChange("New Title")
        assertEquals("New Title", manager.title)
        assertTrue(manager.canUndo)

        manager.undo()
        assertEquals("Old Title", manager.title)
        assertTrue(manager.canRedo)

        manager.redo()
        assertEquals("New Title", manager.title)
    }
}
