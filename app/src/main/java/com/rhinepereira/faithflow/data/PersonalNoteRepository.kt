package com.rhinepereira.faithflow.data

import android.content.Context
import androidx.work.*
import com.rhinepereira.faithflow.sync.SyncWorker
import com.google.firebase.auth.FirebaseAuth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PersonalNoteRepository(private val context: Context, private val verseDao: VerseDao) {

    fun getAllCategories(userId: String): Flow<List<PersonalNoteCategory>> = verseDao.getAllCategories(userId)

    fun getNotesForCategory(categoryId: String): Flow<List<PersonalNote>> = verseDao.getNotesForCategory(categoryId)

    private fun getCurrentUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }

    suspend fun insertCategory(category: PersonalNoteCategory) {
        verseDao.insertCategory(category.copy(isSynced = false, userId = getCurrentUserId()))
        scheduleSync()
    }

    suspend fun insertNote(note: PersonalNote) {
        verseDao.insertPersonalNote(note.copy(isSynced = false, userId = getCurrentUserId()))
        scheduleSync()
    }

    suspend fun updateNote(note: PersonalNote) {
        verseDao.insertPersonalNote(note.copy(isSynced = false, userId = getCurrentUserId()))
        scheduleSync()
    }

    suspend fun deleteNote(note: PersonalNote) {
        val userId = getCurrentUserId()
        // Soft delete locally.
        verseDao.updatePersonalNote(note.copy(isDeleted = true, isSynced = false, userId = userId))
        scheduleSync()
    }

    suspend fun renameCategory(category: PersonalNoteCategory, newName: String) {
        val userId = getCurrentUserId()
        verseDao.updateCategory(category.copy(name = newName, isSynced = false, userId = userId))
        scheduleSync()
    }

    suspend fun deleteCategory(category: PersonalNoteCategory) {
        val userId = getCurrentUserId()
        
        // Soft-delete all notes in this category first.
        val notesInCategory = verseDao.getNotesForCategorySync(category.id)
        notesInCategory.forEach { note ->
            verseDao.updatePersonalNote(note.copy(isDeleted = true, isSynced = false, userId = userId))
        }

        // Soft-delete the category locally.
        verseDao.updateCategory(category.copy(isDeleted = true, isSynced = false, userId = userId))
        
        // Let SyncWorker handle remote deletion
        scheduleSync()
    }

    suspend fun syncFromCloud(userId: String) = withContext(Dispatchers.IO) {
        try {
            // Fetch Personal Note Categories
            val categories = SupabaseConfig.client.postgrest["personal_note_categories"].select {
                filter { eq("user_id", userId) }
            }.decodeList<PersonalNoteCategory>()
            categories.forEach { category ->
                val local = verseDao.getCategoryById(category.id)
                if (local == null || local.isSynced) {
                    verseDao.insertCategory(category.copy(isSynced = true, userId = userId))
                }
            }

            // Fetch Personal Notes
            val personalNotes = SupabaseConfig.client.postgrest["personal_notes"].select {
                filter { eq("user_id", userId) }
            }.decodeList<PersonalNote>()
            personalNotes.forEach { note ->
                val local = verseDao.getPersonalNoteById(note.id)
                if (local == null || local.isSynced) {
                    verseDao.insertPersonalNote(note.copy(isSynced = true, userId = userId))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun reorderCategories(orderedCategories: List<PersonalNoteCategory>) {
        val userId = getCurrentUserId()
        if (userId.isBlank()) return

        val baseTime = System.currentTimeMillis()
        orderedCategories.forEachIndexed { index, category ->
            verseDao.insertCategory(
                category.copy(
                    userId = userId,
                    createdAt = baseTime + index,
                    isSynced = false
                )
            )
        }

        scheduleSync()
    }

    fun scheduleSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                java.util.concurrent.TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "supabase_sync",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }
}
