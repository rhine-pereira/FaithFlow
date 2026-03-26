package com.rhinepereira.faithflow.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rhinepereira.faithflow.data.AppDatabase
import com.rhinepereira.faithflow.data.SupabaseConfig
import com.google.firebase.auth.FirebaseAuth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val database = AppDatabase.getDatabase(applicationContext)
        val dao = database.verseDao()

        // Get current user ID - skip sync if not authenticated
        val userId = FirebaseAuth.getInstance().currentUser?.uid
            ?: return@withContext Result.retry()

        try {
            // 1. Sync Notes (Themes)
            val unsyncedNotes = dao.getUnsyncedNotes()
            unsyncedNotes.forEach { note ->
                if (note.isDeleted) {
                    SupabaseConfig.client.postgrest["notes"].delete {
                        filter { eq("id", note.id) }
                    }
                    dao.deleteNote(note)
                } else {
                    SupabaseConfig.client.postgrest["notes"].upsert(note.copy(userId = userId))
                    dao.updateNote(note.copy(isSynced = true, userId = userId))
                }
            }

            // 2. Sync Verses
            val unsyncedVerses = dao.getUnsyncedVerses()
            unsyncedVerses.forEach { verse ->
                if (verse.isDeleted) {
                    SupabaseConfig.client.postgrest["verses"].delete {
                        filter { eq("id", verse.id) }
                    }
                    dao.deleteVerse(verse)
                } else {
                    SupabaseConfig.client.postgrest["verses"].upsert(verse.copy(userId = userId))
                    dao.updateVerse(verse.copy(isSynced = true, userId = userId))
                }
            }

            // 4. Sync Personal Note Categories
            val unsyncedCategories = dao.getUnsyncedCategories()
            unsyncedCategories.forEach { category ->
                if (category.isDeleted) {
                    SupabaseConfig.client.postgrest["personal_note_categories"].delete {
                        filter { eq("id", category.id) }
                    }
                    dao.deleteCategory(category)
                } else {
                    SupabaseConfig.client.postgrest["personal_note_categories"].upsert(category.copy(userId = userId))
                    dao.insertCategory(category.copy(isSynced = true, userId = userId))
                }
            }

            // 5. Sync Personal Notes
            val unsyncedPersonalNotes = dao.getUnsyncedPersonalNotes()
            unsyncedPersonalNotes.forEach { note ->
                if (note.isDeleted) {
                    SupabaseConfig.client.postgrest["personal_notes"].delete {
                        filter { eq("id", note.id) }
                    }
                    dao.deletePersonalNote(note)
                } else {
                    SupabaseConfig.client.postgrest["personal_notes"].upsert(note.copy(userId = userId))
                    dao.insertPersonalNote(note.copy(isSynced = true, userId = userId))
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
