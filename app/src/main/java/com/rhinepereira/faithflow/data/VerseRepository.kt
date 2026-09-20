package com.rhinepereira.faithflow.data

import android.content.Context
import android.util.Log
import com.rhinepereira.faithflow.sync.SyncScheduler
import com.rhinepereira.faithflow.util.DateUtils
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * In-memory gate preventing redundant fetch calls within [MIN_INTERVAL_MS].
 */
object CloudSyncGate {
    private val mutex = Mutex()
    private var lastFetchAt = 0L
    private var lastUserId: String? = null

    const val MIN_INTERVAL_MS = 15 * 60 * 1000L

    fun invalidate() {
        lastFetchAt = 0L
        lastUserId = null
    }

    suspend fun runIfNeeded(userId: String, force: Boolean, fetch: suspend () -> Unit) {
        if (!force) {
            val now = System.currentTimeMillis()
            if (userId == lastUserId && now - lastFetchAt < MIN_INTERVAL_MS) {
                Log.d("FaithFlowSync", "Fetch skipped (cached, ${(now - lastFetchAt) / 1000}s ago)")
                return
            }
        }
        mutex.withLock {
            if (!force) {
                val now = System.currentTimeMillis()
                if (userId == lastUserId && now - lastFetchAt < MIN_INTERVAL_MS) {
                    Log.d("FaithFlowSync", "Fetch skipped (cached after lock)")
                    return
                }
            }
            fetch()
            lastFetchAt = System.currentTimeMillis()
            lastUserId = userId
        }
    }
}

/**
 * Repository for Bible verses, themes, and daily records.
 */
class VerseRepository(private val context: Context, private val verseDao: VerseDao) {

    fun getAllNotesWithVerses(userId: String): Flow<List<NoteWithVerses>> = verseDao.getNotesWithVerses(userId)

    fun getVersesForNote(noteId: String): Flow<List<Verse>> = verseDao.getVersesForNote(noteId)

    private fun getCurrentUserId(): String = AuthRepository.currentUserId ?: ""

    suspend fun insertNote(note: Note) {
        verseDao.insertNote(note.copy(isSynced = false, userId = getCurrentUserId()))
        scheduleSync()
    }

    suspend fun insertVerse(verse: Verse) {
        verseDao.insertVerse(verse.copy(isSynced = false, userId = getCurrentUserId()))
        scheduleSync()
    }

    suspend fun updateNote(note: Note) {
        verseDao.updateNote(note.copy(isSynced = false, userId = getCurrentUserId()))
        scheduleSync()
    }

    suspend fun updateVerse(verse: Verse) {
        verseDao.updateVerse(verse.copy(isSynced = false, userId = getCurrentUserId()))
        scheduleSync()
    }

    suspend fun deleteNote(note: Note) {
        val userId = getCurrentUserId()
        // Soft-delete the note locally.
        verseDao.updateNote(note.copy(isDeleted = true, isSynced = false, userId = userId))

        // Soft-delete all its verses.
        val verses = verseDao.getVersesForNoteSync(note.id)
        verses.forEach { verse ->
            verseDao.updateVerse(verse.copy(isDeleted = true, isSynced = false, userId = userId))
        }

        scheduleSync()
    }

    suspend fun deleteVerse(verse: Verse) {
        val userId = getCurrentUserId()
        // Soft-delete locally.
        verseDao.updateVerse(verse.copy(isDeleted = true, isSynced = false, userId = userId))
        scheduleSync()
    }

    suspend fun fetchFromSupabaseIfNeeded(userId: String, force: Boolean = false) {
        CloudSyncGate.runIfNeeded(userId, force) {
            fetchFromSupabase(userId)
        }
    }

    suspend fun fetchFromSupabase(userId: String) = withContext(Dispatchers.IO) {
        val tag = "FaithFlowSync"
        try {
            if (userId.isBlank()) {
                Log.d(tag, "Fetch skipped: blank userId")
                return@withContext
            }
            Log.d(tag, "Starting fetch for userId: $userId")

            // Fetch Notes
            fetchAndUpsert<Note>(
                tableName = "notes",
                userId = userId,
                tag = tag,
                getId = { it.id },
                getLocal = { verseDao.getNoteById(it) },
                isSynced = { it.isSynced },
                insertLocal = { verseDao.insertNote(it) },
                withUserAndSynced = { item, uid -> item.copy(isSynced = true, userId = uid) }
            )

            // Fetch Verses
            fetchAndUpsert<Verse>(
                tableName = "verses",
                userId = userId,
                tag = tag,
                getId = { it.id },
                getLocal = { verseDao.getVerseById(it) },
                isSynced = { it.isSynced },
                insertLocal = { verseDao.insertVerse(it) },
                withUserAndSynced = { item, uid -> item.copy(isSynced = true, userId = uid) }
            )

            // Fetch Categories
            fetchAndUpsert<PersonalNoteCategory>(
                tableName = "personal_note_categories",
                userId = userId,
                tag = tag,
                getId = { it.id },
                getLocal = { verseDao.getCategoryById(it) },
                isSynced = { it.isSynced },
                insertLocal = { verseDao.insertCategory(it) },
                withUserAndSynced = { item, uid -> item.copy(isSynced = true, userId = uid) }
            )

            // Fetch Personal Notes
            fetchAndUpsert<PersonalNote>(
                tableName = "personal_notes",
                userId = userId,
                tag = tag,
                getId = { it.id },
                getLocal = { verseDao.getPersonalNoteById(it) },
                isSynced = { it.isSynced },
                insertLocal = { verseDao.insertPersonalNote(it) },
                withUserAndSynced = { item, uid -> item.copy(isSynced = true, userId = uid) }
            )

            // Fetch Daily Records
            val dailyRecords = SupabaseConfig.client.postgrest["daily_records"].select {
                filter { eq("user_id", userId) }
            }.decodeList<DailyRecord>()
            Log.d(tag, "Fetched ${dailyRecords.size} daily records")
            dailyRecords.forEach { record ->
                val startOfDay = record.date
                val endOfDay = startOfDay + DateUtils.MILLIS_PER_DAY
                val local = verseDao.getRecordForDateSync(userId, startOfDay, endOfDay)
                if (local == null || local.isSynced) {
                    verseDao.insertDailyRecord(record.copy(isSynced = true, userId = userId))
                }
            }
            Log.d(tag, "Fetch completed successfully")
        } catch (e: Exception) {
            Log.e(tag, "Fetch failed", e)
            e.printStackTrace()
        }
    }

    private suspend inline fun <reified T : Any> fetchAndUpsert(
        tableName: String,
        userId: String,
        tag: String,
        crossinline getId: (T) -> String,
        crossinline getLocal: suspend (String) -> T?,
        crossinline isSynced: (T) -> Boolean,
        crossinline insertLocal: suspend (T) -> Unit,
        crossinline withUserAndSynced: (T, String) -> T
    ) {
        val items = SupabaseConfig.client.postgrest[tableName].select {
            filter { eq("user_id", userId) }
        }.decodeList<T>()
        Log.d(tag, "Fetched ${items.size} $tableName")
        items.forEach { item ->
            val local = getLocal(getId(item))
            if (local == null || isSynced(local)) {
                insertLocal(withUserAndSynced(item, userId))
            }
        }
    }

    fun scheduleSync() {
        SyncScheduler.scheduleSync(context)
    }
}
