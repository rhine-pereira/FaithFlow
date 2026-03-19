package com.rhinepereira.faithflow.data

import android.content.Context
import androidx.work.*
import com.rhinepereira.faithflow.sync.SyncWorker
import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VerseRepository(private val context: Context, private val verseDao: VerseDao) {

    fun getAllNotesWithVerses(userId: String): Flow<List<NoteWithVerses>> = verseDao.getNotesWithVerses(userId)

    fun getVersesForNote(noteId: String): Flow<List<Verse>> = verseDao.getVersesForNote(noteId)

    private fun getCurrentUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }

    suspend fun insertNote(note: Note) {
        verseDao.insertNote(note.copy(isSynced = false, userId = getCurrentUserId()))
        scheduleSync()
    }

    suspend fun insertVerse(verse: Verse) {
        verseDao.insertVerse(verse.copy(isSynced = false, userId = getCurrentUserId()))
        scheduleSync()
    }

    suspend fun updateVerse(verse: Verse) {
        verseDao.updateVerse(verse.copy(isSynced = false, userId = getCurrentUserId()))
        scheduleSync()
    }

    suspend fun deleteNote(note: Note) {
        verseDao.deleteNote(note)
        withContext(Dispatchers.IO) {
            try {
                SupabaseConfig.client.postgrest["notes"].delete {
                    filter { eq("id", note.id) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun deleteVerse(verse: Verse) {
        verseDao.deleteVerse(verse)
        withContext(Dispatchers.IO) {
            try {
                SupabaseConfig.client.postgrest["verses"].delete {
                    filter { eq("id", verse.id) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun fetchFromSupabase(userId: String) = withContext(Dispatchers.IO) {
        val tag = "FaithFlowSync"
        try {
            if (userId.isBlank()) {
                Log.d(tag, "Fetch skipped: blind userId")
                return@withContext
            }
            Log.d(tag, "Starting fetch for userId: $userId")

            // Fetch Notes
            val notes = SupabaseConfig.client.postgrest["notes"].select {
                filter { eq("user_id", userId) }
            }.decodeList<Note>()
            Log.d(tag, "Fetched ${notes.size} notes (themes)")
            notes.forEach { note ->
                verseDao.insertNote(note.copy(isSynced = true, userId = userId))
            }

            // Fetch Verses
            val verses = SupabaseConfig.client.postgrest["verses"].select {
                filter { eq("user_id", userId) }
            }.decodeList<Verse>()
            Log.d(tag, "Fetched ${verses.size} verses")
            verses.forEach { verse ->
                verseDao.insertVerse(verse.copy(isSynced = true, userId = userId))
            }
            
            // Fetch Daily Records
            /*
            val records = SupabaseConfig.client.postgrest["daily_records"].select {
                filter { eq("user_id", userId) }
            }.decodeList<DailyRecord>()
            Log.d(tag, "Fetched ${records.size} daily records")
            records.forEach { record ->
                verseDao.insertDailyRecord(record.copy(isSynced = true, userId = userId))
            }
            */

            // Fetch Categories
            val categoriesData = SupabaseConfig.client.postgrest["personal_note_categories"].select {
                filter { eq("user_id", userId) }
            }.decodeList<PersonalNoteCategory>()
            Log.d(tag, "Fetched ${categoriesData.size} categories")
            categoriesData.forEach { category ->
                verseDao.insertCategory(category.copy(isSynced = true, userId = userId))
            }

            // Fetch Personal Notes
            val personalNotes = SupabaseConfig.client.postgrest["personal_notes"].select {
                filter { eq("user_id", userId) }
            }.decodeList<PersonalNote>()
            Log.d(tag, "Fetched ${personalNotes.size} personal notes")
            personalNotes.forEach { personalNote ->
                verseDao.insertPersonalNote(personalNote.copy(isSynced = true, userId = userId))
            }
            Log.d(tag, "Fetch completed successfully")
        } catch (e: Exception) {
            Log.e(tag, "Fetch failed spectacularly", e)
            e.printStackTrace()
        }
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
