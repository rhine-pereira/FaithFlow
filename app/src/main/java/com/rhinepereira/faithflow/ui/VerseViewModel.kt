package com.rhinepereira.faithflow.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rhinepereira.faithflow.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.launch

class VerseViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: VerseRepository
    private val authRepository = AuthRepository()
    val allNotesWithVerses: StateFlow<List<NoteWithVerses>>

    init {
        val verseDao = AppDatabase.getDatabase(application).verseDao()
        repository = VerseRepository(application, verseDao)
        
        allNotesWithVerses = authRepository.authStatusFlow()
            .flatMapLatest { status ->
                when (status) {
                    is AuthStatus.Authenticated -> {
                        repository.getAllNotesWithVerses(status.userId)
                    }
                    else -> flowOf(emptyList())
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
        
        // Initial fetch from cloud after authentication
        viewModelScope.launch {
            authRepository.authStatusFlow().collect { status ->
                if (status is AuthStatus.Authenticated) {
                    repository.fetchFromSupabase(status.userId)
                }
            }
        }
    }

    fun getVersesForNote(noteId: String): Flow<List<Verse>> = repository.getVersesForNote(noteId)

    fun addNote(theme: String) {
        viewModelScope.launch {
            repository.insertNote(Note(theme = theme))
        }
    }

    fun addVerse(noteId: String, reference: String, content: String) {
        viewModelScope.launch {
            repository.insertVerse(Verse(noteId = noteId, reference = reference, content = content))
        }
    }

    fun addNoteAndVerse(themeName: String, reference: String, content: String) {
        viewModelScope.launch {
            val newNote = Note(theme = themeName)
            repository.insertNote(newNote)
            repository.insertVerse(Verse(noteId = newNote.id, reference = reference, content = content))
        }
    }

    fun updateVerse(verse: Verse) {
        viewModelScope.launch {
            repository.updateVerse(verse)
        }
    }

    fun renameNote(note: Note, newTheme: String) {
        viewModelScope.launch {
            repository.updateNote(note.copy(theme = newTheme))
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun deleteVerse(verse: Verse) {
        viewModelScope.launch {
            repository.deleteVerse(verse)
        }
    }
}
