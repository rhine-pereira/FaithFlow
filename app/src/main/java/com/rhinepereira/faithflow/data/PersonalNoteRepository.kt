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
        verseDao.deletePersonalNote(note)
        withContext(Dispatchers.IO) {
            try {
                SupabaseConfig.client.postgrest["personal_notes"].delete {
                    filter { eq("id", note.id) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun deleteCategory(category: PersonalNoteCategory) {
        // Delete all notes in this category first.
        val notesInCategory = verseDao.getNotesForCategorySync(category.id)
        notesInCategory.forEach { note ->
            verseDao.deletePersonalNote(note)
        }

        // Delete locally.
        verseDao.deleteCategory(category)

        // Delete remotely.
        withContext(Dispatchers.IO) {
            try {
                notesInCategory.forEach { note ->
                    SupabaseConfig.client.postgrest["personal_notes"].delete {
                        filter { eq("id", note.id) }
                    }
                }
                SupabaseConfig.client.postgrest["personal_note_categories"].delete {
                    filter { eq("id", category.id) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun syncFromCloud(userId: String) = withContext(Dispatchers.IO) {
        try {
            // Fetch Personal Note Categories
            val categories = SupabaseConfig.client.postgrest["personal_note_categories"].select {
                filter { eq("user_id", userId) }
            }.decodeList<PersonalNoteCategory>()
            categories.forEach { category ->
                verseDao.insertCategory(category.copy(isSynced = true, userId = userId))
            }

            // Fetch Personal Notes
            val notes = SupabaseConfig.client.postgrest["personal_notes"].select {
                filter { eq("user_id", userId) }
            }.decodeList<PersonalNote>()
            notes.forEach { note ->
                verseDao.insertPersonalNote(note.copy(isSynced = true, userId = userId))
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
