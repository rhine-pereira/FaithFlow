package com.rhinepereira.faithflow.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VerseDao {
    @Transaction
    @Query("SELECT * FROM notes WHERE userId = :userId AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getNotesWithVerses(userId: String): Flow<List<NoteWithVerses>>

    @Query("SELECT * FROM notes WHERE userId = :userId AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getAllNotes(userId: String): Flow<List<Note>>

    @Query("SELECT * FROM verses WHERE noteId = :noteId AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getVersesForNote(noteId: String): Flow<List<Verse>>

    @Query("SELECT * FROM verses WHERE noteId = :noteId AND isDeleted = 0")
    suspend fun getVersesForNoteSync(noteId: String): List<Verse>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerse(verse: Verse)

    @Update
    suspend fun updateNote(note: Note)

    @Update
    suspend fun updateVerse(verse: Verse)

    @Delete
    suspend fun deleteNote(note: Note)

    @Delete
    suspend fun deleteVerse(verse: Verse)

    @Query("SELECT * FROM verses WHERE isSynced = 0")
    suspend fun getUnsyncedVerses(): List<Verse>

    @Query("SELECT * FROM notes WHERE isSynced = 0")
    suspend fun getUnsyncedNotes(): List<Note>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: String): Note?

    @Query("SELECT * FROM verses WHERE id = :id LIMIT 1")
    suspend fun getVerseById(id: String): Verse?

    // Daily Records
    /*
    @Query("SELECT * FROM daily_records WHERE userId = :userId ORDER BY date DESC")
    fun getAllDailyRecords(userId: String): Flow<List<DailyRecord>>

    @Query("SELECT * FROM daily_records WHERE userId = :userId AND date >= :startOfDay AND date < :endOfDay LIMIT 1")
    fun getRecordForDate(userId: String, startOfDay: Long, endOfDay: Long): Flow<DailyRecord?>

    @Query("SELECT * FROM daily_records WHERE userId = :userId AND date >= :startOfDay AND date < :endOfDay LIMIT 1")
    suspend fun getRecordForDateSync(userId: String, startOfDay: Long, endOfDay: Long): DailyRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyRecord(record: DailyRecord)

    @Update
    suspend fun updateDailyRecord(record: DailyRecord)

    @Query("SELECT * FROM daily_records WHERE isSynced = 0")
    suspend fun getUnsyncedDailyRecords(): List<DailyRecord>
    */

    // Personal Notes
    @Query("SELECT * FROM personal_note_categories WHERE userId = :userId AND isDeleted = 0 ORDER BY createdAt ASC")
    fun getAllCategories(userId: String): Flow<List<PersonalNoteCategory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: PersonalNoteCategory)

    @Query("SELECT * FROM personal_notes WHERE categoryId = :categoryId AND isDeleted = 0 ORDER BY date DESC")
    fun getNotesForCategory(categoryId: String): Flow<List<PersonalNote>>

    @Query("SELECT * FROM personal_notes WHERE categoryId = :categoryId AND isDeleted = 0")
    suspend fun getNotesForCategorySync(categoryId: String): List<PersonalNote>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPersonalNote(note: PersonalNote)

    @Update
    suspend fun updatePersonalNote(note: PersonalNote)

    @Delete
    suspend fun deletePersonalNote(note: PersonalNote)

    @Query("SELECT * FROM personal_note_categories WHERE id = :id LIMIT 1")
    suspend fun getCategoryById(id: String): PersonalNoteCategory?

    @Query("SELECT * FROM personal_notes WHERE id = :id LIMIT 1")
    suspend fun getPersonalNoteById(id: String): PersonalNote?

    @Update
    suspend fun updateCategory(category: PersonalNoteCategory)

    @Delete
    suspend fun deleteCategory(category: PersonalNoteCategory)

    @Query("SELECT * FROM personal_note_categories WHERE userId = :userId AND name IN ('CYP Talks', 'CGS Talks', 'Prophecies', 'prophcy')")
    suspend fun getLegacyCategories(userId: String): List<PersonalNoteCategory>

    @Query("SELECT * FROM personal_note_categories WHERE isSynced = 0")
    suspend fun getUnsyncedCategories(): List<PersonalNoteCategory>

    @Query("SELECT * FROM personal_notes WHERE isSynced = 0")
    suspend fun getUnsyncedPersonalNotes(): List<PersonalNote>
}
