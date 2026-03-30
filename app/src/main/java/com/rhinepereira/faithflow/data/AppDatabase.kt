package com.rhinepereira.faithflow.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Note::class, Verse::class, PersonalNote::class, PersonalNoteCategory::class, DailyRecord::class], version = 9, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun verseDao(): VerseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE notes ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE verses ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE personal_note_categories ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE personal_notes ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE notes ADD COLUMN userId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE verses ADD COLUMN userId TEXT NOT NULL DEFAULT ''")
                // db.execSQL("ALTER TABLE daily_records ADD COLUMN userId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE personal_note_categories ADD COLUMN userId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE personal_notes ADD COLUMN userId TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Recreate daily_records table to make prophecy and whatRead nullable
                /*
                db.execSQL("""
                    CREATE TABLE daily_records_new (
                        id TEXT NOT NULL PRIMARY KEY,
                        date INTEGER NOT NULL,
                        read_today INTEGER NOT NULL,
                        what_read TEXT,
                        total_read_time_minutes INTEGER NOT NULL,
                        prayed_today INTEGER NOT NULL,
                        total_prayer_time_minutes INTEGER NOT NULL,
                        prophecy TEXT,
                        user_id TEXT NOT NULL,
                        created_at INTEGER NOT NULL,
                        is_synced INTEGER NOT NULL
                    )
                """.trimIndent())
                
                // Insert data, using defaults for columns that might not exist in the old schema
                db.execSQL("""
                    INSERT INTO daily_records_new (id, date, read_today, what_read, total_read_time_minutes, 
                                                   prayed_today, total_prayer_time_minutes, prophecy, 
                                                   user_id, created_at, is_synced) 
                    SELECT id, date, 0, NULL, total_read_time_minutes, 
                           prayed_today, total_prayer_time_minutes, prophecy, 
                           user_id, created_at, is_synced 
                    FROM daily_records
                """.trimIndent())
                
                db.execSQL("DROP TABLE daily_records")
                db.execSQL("ALTER TABLE daily_records_new RENAME TO daily_records")
                */
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "verse_database"
                )
                .addMigrations(MIGRATION_4_5, MIGRATION_6_7)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
