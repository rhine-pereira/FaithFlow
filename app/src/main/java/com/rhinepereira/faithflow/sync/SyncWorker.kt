package com.rhinepereira.faithflow.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rhinepereira.faithflow.data.AppDatabase
import com.rhinepereira.faithflow.data.AuthRepository
import com.rhinepereira.faithflow.data.SupabaseConfig
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * WorkManager worker that synchronizes unsynced local data with Supabase.
 */
class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val database = AppDatabase.getDatabase(applicationContext)
        val dao = database.verseDao()

        // Get current user ID - skip sync if not authenticated
        val userId = AuthRepository.currentUserId
            ?: return@withContext Result.retry()

        try {
            // 1. Sync Notes (Themes)
            syncEntities(
                tableName = "notes",
                entities = dao.getUnsyncedNotes(),
                getId = { it.id },
                isDeleted = { it.isDeleted },
                withUserAndSynced = { item, uid -> item.copy(userId = uid, isSynced = true) },
                deleteLocal = { dao.deleteNote(it) },
                saveLocal = { dao.updateNote(it) },
                userId = userId
            )

            // 2. Sync Verses
            syncEntities(
                tableName = "verses",
                entities = dao.getUnsyncedVerses(),
                getId = { it.id },
                isDeleted = { it.isDeleted },
                withUserAndSynced = { item, uid -> item.copy(userId = uid, isSynced = true) },
                deleteLocal = { dao.deleteVerse(it) },
                saveLocal = { dao.updateVerse(it) },
                userId = userId
            )

            // 3. Sync Personal Note Categories
            syncEntities(
                tableName = "personal_note_categories",
                entities = dao.getUnsyncedCategories(),
                getId = { it.id },
                isDeleted = { it.isDeleted },
                withUserAndSynced = { item, uid -> item.copy(userId = uid, isSynced = true) },
                deleteLocal = { dao.deleteCategory(it) },
                saveLocal = { dao.insertCategory(it) },
                userId = userId
            )

            // 4. Sync Personal Notes
            syncEntities(
                tableName = "personal_notes",
                entities = dao.getUnsyncedPersonalNotes(),
                getId = { it.id },
                isDeleted = { it.isDeleted },
                withUserAndSynced = { item, uid -> item.copy(userId = uid, isSynced = true) },
                deleteLocal = { dao.deletePersonalNote(it) },
                saveLocal = { dao.insertPersonalNote(it) },
                userId = userId
            )

            // 5. Sync Daily Records
            val unsyncedDaily = dao.getUnsyncedDailyRecords()
            unsyncedDaily.forEach { record ->
                val synced = record.copy(userId = userId, isSynced = true)
                SupabaseConfig.client.postgrest["daily_records"].upsert(synced)
                dao.updateDailyRecord(synced)
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private suspend inline fun <reified T : Any> syncEntities(
        tableName: String,
        entities: List<T>,
        crossinline getId: (T) -> String,
        crossinline isDeleted: (T) -> Boolean,
        crossinline withUserAndSynced: (T, String) -> T,
        crossinline deleteLocal: suspend (T) -> Unit,
        crossinline saveLocal: suspend (T) -> Unit,
        userId: String
    ) {
        for (entity in entities) {
            if (isDeleted(entity)) {
                SupabaseConfig.client.postgrest[tableName].delete {
                    filter { eq("id", getId(entity)) }
                }
                deleteLocal(entity)
            } else {
                val prepared = withUserAndSynced(entity, userId)
                SupabaseConfig.client.postgrest[tableName].upsert(prepared)
                saveLocal(prepared)
            }
        }
    }
}
