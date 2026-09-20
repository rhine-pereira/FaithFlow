package com.rhinepereira.faithflow.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rhinepereira.faithflow.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PersonalNoteRepository
    private val verseRepository: VerseRepository
    private val dao: VerseDao

    
    val categories: StateFlow<List<PersonalNoteCategory>>
    val allPersonalNotes: StateFlow<List<PersonalNote>>

    init {
        val database = AppDatabase.getDatabase(application)
        dao = database.verseDao()
        repository = PersonalNoteRepository(application, dao)
        verseRepository = VerseRepository(application, dao)

        val authStatus = AuthRepository.authStatus

        categories = authStatus.flatMapLatest { status ->
            when (status) {
                is AuthStatus.Authenticated -> {
                    repository.getAllCategories(status.userId)
                }
                else -> flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

        allPersonalNotes = authStatus.flatMapLatest { status ->
            when (status) {
                is AuthStatus.Authenticated -> repository.getAllNotes(status.userId)
                else -> flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )
    }

    fun getNotesForCategory(categoryId: String): Flow<List<PersonalNote>> = repository.getNotesForCategory(categoryId)

    fun addCategory(name: String) {
        viewModelScope.launch {
            repository.insertCategory(PersonalNoteCategory(name = name))
        }
    }

    fun addNote(categoryId: String, title: String, content: String, date: Long) {
        viewModelScope.launch {
            repository.insertNote(PersonalNote(categoryId = categoryId, title = title, content = content, date = date))
        }
    }

    fun updateNote(note: PersonalNote) {
        viewModelScope.launch {
            if (note.id.isBlank() || note.id == "0") { // Check if new
                 repository.insertNote(note.copy(id = java.util.UUID.randomUUID().toString()))
            } else {
                repository.updateNote(note)
            }
        }
    }

    fun deleteNote(note: PersonalNote) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun deleteCategory(category: PersonalNoteCategory) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    fun moveCategory(categories: List<PersonalNoteCategory>, fromIndex: Int, toIndex: Int) {
        if (fromIndex !in categories.indices || toIndex !in categories.indices || fromIndex == toIndex) {
            return
        }

        viewModelScope.launch {
            val reordered = categories.toMutableList().apply {
                val moved = removeAt(fromIndex)
                add(toIndex, moved)
            }
            repository.reorderCategories(reordered)
        }
    }

    fun renameCategory(category: PersonalNoteCategory, newName: String) {
        viewModelScope.launch {
            repository.renameCategory(category, newName)
        }
    }

    fun reorderCategories(categories: List<PersonalNoteCategory>) {
        viewModelScope.launch {
            repository.reorderCategories(categories)
        }
    }

    fun syncFromCloud() {
        viewModelScope.launch {
            val userId = when (val status = AuthRepository.authStatus.first()) {
                is AuthStatus.Authenticated -> status.userId
                else -> return@launch
            }
            verseRepository.fetchFromSupabaseIfNeeded(userId, force = true)
        }
    }
}
