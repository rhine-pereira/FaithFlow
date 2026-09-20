package com.rhinepereira.faithflow.ui.notes

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.rhinepereira.faithflow.data.BibleData

/**
 * Represents a parsed Bible reference found in user note text.
 */
data class BibleRef(
    val book: String,
    val chapter: Int,
    val verses: List<Pair<Int, Int?>>,
    val originalText: String
)

/**
 * Searches a line of text for a Catholic Bible reference and parses it into [BibleRef] if found.
 */
fun findBibleReference(line: String): BibleRef? {
    val match = BibleData.bibleRefRegex.find(line) ?: return null

    val matchedName = match.groupValues[1]
    val chapter = match.groupValues[2].toIntOrNull() ?: return null
    val versesStr = match.groupValues[3]

    // Map abbreviation back to full name if necessary
    val book = BibleData.abbreviations.entries.find { it.key.equals(matchedName, ignoreCase = true) }?.value
        ?: matchedName

    val verseRanges = if (versesStr.isEmpty()) {
        emptyList()
    } else {
        versesStr.split(",").mapNotNull { rangeStr ->
            val parts = rangeStr.trim().split("-")
            val start = parts.getOrNull(0)?.toIntOrNull() ?: return@mapNotNull null
            val end = if (parts.size > 1) parts[1].toIntOrNull() else null
            start to end
        }
    }

    return BibleRef(
        book = book,
        chapter = chapter,
        verses = verseRanges,
        originalText = match.value
    )
}

/**
 * Pure function that calculates the new [TextFieldValue] when inserting a fetched Bible verse into the editor.
 * Replaces the matched reference in the current line if found, or appends it otherwise.
 */
fun insertBibleVerse(
    contentValue: TextFieldValue,
    ref: BibleRef,
    fetchedVerseText: String
): TextFieldValue {
    val referenceText = ref.originalText
    val text = contentValue.text
    val selection = contentValue.selection
    val textBeforeCursor = text.take(selection.start)
    val textAfterCursor = text.substring(selection.end)
    val lineStart = textBeforeCursor.lastIndexOf('\n') + 1
    val currentLine = textBeforeCursor.substring(lineStart)

    val match = BibleData.bibleRefRegex.find(currentLine)

    val newText: String
    val newSelection: TextRange

    if (match != null) {
        val lineBeforeMatch = currentLine.take(match.range.first)
        val lineAfterMatch = currentLine.substring(match.range.last + 1)
        val replacement = "$referenceText\n$fetchedVerseText"
        val newLine = lineBeforeMatch + replacement + lineAfterMatch
        newText = text.take(lineStart) + newLine + "\n" + textAfterCursor
        newSelection = TextRange(lineStart + lineBeforeMatch.length + replacement.length + 1)
    } else {
        val appendText = "\n$referenceText\n$fetchedVerseText\n"
        newText = text.take(selection.start) + appendText + textAfterCursor
        newSelection = TextRange(selection.start + appendText.length)
    }

    return TextFieldValue(text = newText, selection = newSelection)
}
