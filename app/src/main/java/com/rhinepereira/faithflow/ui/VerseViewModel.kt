package com.rhinepereira.faithflow.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rhinepereira.faithflow.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel managing thematic collections of Bible verses and user-defined theme ordering.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class VerseViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: VerseRepository
    private val themeOrderPrefs = application.getSharedPreferences("theme_order_prefs", Context.MODE_PRIVATE)
    val allNotesWithVerses: StateFlow<List<NoteWithVerses>>

    init {
        val verseDao = AppDatabase.getDatabase(application).verseDao()
        repository = VerseRepository(application, verseDao)

        allNotesWithVerses = AuthRepository.authStatus
            .flatMapLatest { status ->
                when (status) {
                    is AuthStatus.Authenticated -> repository.getAllNotesWithVerses(status.userId)
                    else -> flowOf(emptyList())
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = emptyList()
            )

        // Background sync: Room is the cache; skip network if synced recently.
        viewModelScope.launch {
            AuthRepository.authStatus
                .filter { it is AuthStatus.Authenticated }
                .map { (it as AuthStatus.Authenticated).userId }
                .distinctUntilChanged()
                .collect { userId ->
                    repository.fetchFromSupabaseIfNeeded(userId)
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

    fun addNoteWithInitialVerse(themeName: String, reference: String, content: String) {
        addNoteAndVerse(themeName, reference, content)
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

    fun applySavedThemeOrder(notes: List<NoteWithVerses>): List<NoteWithVerses> {
        val savedOrder = getSavedThemeOrder()
        if (savedOrder.isEmpty()) return notes

        val byId = notes.associateBy { it.note.id }.toMutableMap()
        val ordered = savedOrder.mapNotNull { byId.remove(it) }
        val remaining = byId.values.sortedByDescending { it.note.createdAt }
        return remaining + ordered
    }

    fun persistThemeOrder(orderedThemeIds: List<String>) {
        themeOrderPrefs
            .edit()
            .putString(getThemeOrderKey(), orderedThemeIds.joinToString(","))
            .apply()
    }

    private fun getSavedThemeOrder(): List<String> {
        val raw = themeOrderPrefs.getString(getThemeOrderKey(), "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    private fun getThemeOrderKey(): String {
        val userId = AuthRepository.currentUserId ?: "local_user"
        return "theme_order_$userId"
    }
}
