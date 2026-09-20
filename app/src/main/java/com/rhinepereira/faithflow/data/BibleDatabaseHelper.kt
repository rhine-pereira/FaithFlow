package com.rhinepereira.faithflow.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.FileOutputStream

/**
 * Helper for reading verses from the bundled Catholic Bible SQLite database assets (`bible.db`).
 */
class BibleDatabaseHelper(private val context: Context) {

    private val dbName = "bible.db"

    /**
     * Fetches a single verse or a range of verses.
     */
    fun getVerseRange(bookName: String, chapter: Int, startVerse: Int, endVerse: Int?): String? {
        return getVerses(bookName, chapter, listOf(startVerse to endVerse))
    }

    /**
     * Fetches verses based on multiple ranges/spots.
     * Each pair is (startVerse, endVerse). If endVerse is null, it's a single verse.
     */
    fun getVerses(bookName: String, chapter: Int, verseRanges: List<Pair<Int, Int?>>): String? {
        val db = openDatabase()
        val results = mutableListOf<String>()

        try {
            // Try given name, then alternate (singular/plural)
            val searchNames = if (bookName.equals("Psalms", ignoreCase = true) || bookName.equals("Psalm", ignoreCase = true)) {
                listOf("Psalms", "Psalm")
            } else {
                listOf(bookName)
            }

            if (verseRanges.isEmpty()) {
                // Fetch whole chapter
                for (name in searchNames) {
                    val query = """
                        SELECT v.verse, v.text 
                        FROM verses v 
                        JOIN books b ON v.book_id = b.id 
                        WHERE b.name = ? AND v.chapter = ?
                        ORDER BY v.verse ASC
                    """.trimIndent()
                    val params = arrayOf(name, chapter.toString())
                    var found = false
                    db.rawQuery(query, params).use { cursor ->
                        while (cursor.moveToNext()) {
                            val verseNum = cursor.getInt(0)
                            val text = cursor.getString(1)
                            results.add("${toSuperscript(verseNum)} $text")
                            found = true
                        }
                    }
                    if (found) break
                }
            } else {
                for ((start, end) in verseRanges) {
                    var foundForThisRange = false
                    for (name in searchNames) {
                        val query: String
                        val params: Array<String>

                        if (end == null || end <= start) {
                            query = """
                                SELECT v.verse, v.text 
                                FROM verses v 
                                JOIN books b ON v.book_id = b.id 
                                WHERE b.name = ? AND v.chapter = ? AND v.verse = ?
                            """.trimIndent()
                            params = arrayOf(name, chapter.toString(), start.toString())
                        } else {
                            query = """
                                SELECT v.verse, v.text 
                                FROM verses v 
                                JOIN books b ON v.book_id = b.id 
                                WHERE b.name = ? AND v.chapter = ? AND v.verse >= ? AND v.verse <= ? 
                                ORDER BY v.verse ASC
                            """.trimIndent()
                            params = arrayOf(name, chapter.toString(), start.toString(), end.toString())
                        }

                        db.rawQuery(query, params).use { cursor ->
                            while (cursor.moveToNext()) {
                                val verseNum = cursor.getInt(0)
                                val text = cursor.getString(1)
                                results.add("${toSuperscript(verseNum)} $text")
                                foundForThisRange = true
                            }
                        }
                        if (foundForThisRange) break
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }

        return if (results.isNotEmpty()) results.joinToString("\n") else null
    }

    private fun toSuperscript(num: Int): String {
        val map = mapOf(
            '0' to '⁰', '1' to '¹', '2' to '²', '3' to '³', '4' to '⁴',
            '5' to '⁵', '6' to '⁶', '7' to '⁷', '8' to '⁸', '9' to '⁹'
        )
        return num.toString().map { map[it] ?: it }.joinToString("")
    }

    private fun openDatabase(): SQLiteDatabase {
        val dbFile = context.getDatabasePath(dbName)
        if (!dbFile.exists()) {
            dbFile.parentFile?.mkdirs()
            copyDatabase()
        }
        return SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY)
    }

    private fun copyDatabase() {
        val outFileName = context.getDatabasePath(dbName).path
        context.assets.open(dbName).use { input ->
            FileOutputStream(outFileName).use { output ->
                input.copyTo(output)
            }
        }
    }
}
