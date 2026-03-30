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

    private val authRepository = AuthRepository()
    
    val categories: StateFlow<List<PersonalNoteCategory>>

    init {
        val database = AppDatabase.getDatabase(application)
        dao = database.verseDao()
        repository = PersonalNoteRepository(application, dao)
        verseRepository = VerseRepository(application, dao)

        val authStatus = authRepository.authStatusFlow()

        categories = authStatus.flatMapLatest { status ->
            when (status) {
                is AuthStatus.Authenticated -> {
                    repository.getAllCategories(status.userId)
                }
                else -> flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Note: VerseViewModel already handles fetchFromSupabase on auth,
        // so we don't need to duplicate it here to avoid redundant sync calls
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
            val userId = try {
                authRepository.authStatusFlow().first()
                when (val status = authRepository.authStatusFlow().first()) {
                    is AuthStatus.Authenticated -> status.userId
                    else -> return@launch
                }
            } catch (e: Exception) {
                return@launch
            }
            repository.syncFromCloud(userId)
        }
    }
}
