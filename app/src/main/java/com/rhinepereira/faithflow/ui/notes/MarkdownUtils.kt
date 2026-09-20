package com.rhinepereira.faithflow.ui.notes

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import com.rhinepereira.faithflow.data.BibleData
import com.rhinepereira.faithflow.data.PersonalNote

/** Gold highlight color for recognized Bible references in markdown text. */
val BIBLE_REF_HIGHLIGHT_COLOR: Color = Color(0xFFFFD700)

/**
 * Lightweight display model for notes rendered in the notes list/grid.
 */
data class NoteListItem(
    val note: PersonalNote,
    val dateLabel: String,
    val preview: String
)

/**
 * Fast plain-text preview for grid cards — full markdown parsing runs only in the editor.
 */
fun plainTextPreview(content: String): String {
    if (content.isBlank()) return ""
    return content
        .replace("**", "")
        .replace("_", "")
        .lineSequence()
        .take(8)
        .joinToString("\n")
        .take(400)
}

/**
 * Visual transformation that highlights markdown formatting (**bold**, _italic_) and recognized Bible references.
 */
class MarkdownVisualTransformation(private val boldColor: Color) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        // Identity mapping requires original and transformed lengths to be identical.
        return TransformedText(
            text = parseMarkdown(text.text, hideMarkers = false, highlightColor = boldColor),
            offsetMapping = OffsetMapping.Identity
        )
    }
}

/**
 * Parses markdown inline styles (**bold**, _italic_) and Bible reference highlights into an [AnnotatedString].
 */
fun parseMarkdown(text: String, hideMarkers: Boolean, highlightColor: Color): AnnotatedString = buildAnnotatedString {
    if (text.isEmpty()) return@buildAnnotatedString

    val markerStyle = SpanStyle(color = Color.Gray.copy(alpha = 0.2f))

    // Pre-calculate all Bible matches once for the entire text
    val bibleMatches = BibleData.bibleRefRegex.findAll(text).toList()

    var i = 0
    while (i < text.length) {
        // Check if current index is start of a Bible reference
        val bibleMatch = bibleMatches.find { it.range.first == i }

        when {
            bibleMatch != null -> {
                withStyle(SpanStyle(color = BIBLE_REF_HIGHLIGHT_COLOR, fontWeight = FontWeight.Medium)) {
                    append(bibleMatch.value)
                }
                i += bibleMatch.value.length
            }
            text.startsWith("**", i) -> {
                val end = text.indexOf("**", i + 2)
                if (end != -1) {
                    if (hideMarkers) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = highlightColor)) {
                            append(text.substring(i + 2, end))
                        }
                    } else {
                        withStyle(markerStyle) { append("**") }
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = highlightColor)) {
                            append(text.substring(i + 2, end))
                        }
                        withStyle(markerStyle) { append("**") }
                    }
                    i = end + 2
                } else {
                    append(text[i])
                    i++
                }
            }
            text.startsWith("_", i) -> {
                val end = text.indexOf("_", i + 1)
                if (end != -1) {
                    if (hideMarkers) {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(text.substring(i + 1, end))
                        }
                    } else {
                        withStyle(markerStyle) { append("_") }
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(text.substring(i + 1, end))
                        }
                        withStyle(markerStyle) { append("_") }
                    }
                    i = end + 1
                } else {
                    append(text[i])
                    i++
                }
            }
            else -> {
                append(text[i])
                i++
            }
        }
    }
}

/**
 * Handles automatic continuation of ordered lists, bullet lists, and checklists when user presses Enter.
 */
fun handleAutoList(oldValue: TextFieldValue, newValue: TextFieldValue): TextFieldValue {
    if (newValue.text.length != oldValue.text.length + 1) return newValue
    if (newValue.text[newValue.selection.start - 1] != '\n') return newValue

    val textBeforeNewline = newValue.text.take(newValue.selection.start - 1)
    val lastLineStart = textBeforeNewline.lastIndexOf('\n') + 1
    val lastLine = textBeforeNewline.substring(lastLineStart)

    val orderedListRegex = Regex("""^(\d+)\.\s+(.*)$""")
    val orderedMatch = orderedListRegex.find(lastLine)
    if (orderedMatch != null) {
        val number = orderedMatch.groupValues[1].toInt()
        val content = orderedMatch.groupValues[2]

        if (content.isEmpty()) {
            val newText = newValue.text.take(lastLineStart) + newValue.text.substring(newValue.selection.start)
            return newValue.copy(text = newText, selection = TextRange(lastLineStart))
        }

        val prefix = "${number + 1}. "
        val newText = newValue.text.take(newValue.selection.start) + prefix + newValue.text.substring(newValue.selection.start)
        return newValue.copy(text = newText, selection = TextRange(newValue.selection.start + prefix.length))
    }

    val bulletListRegex = Regex("""^([-*])\s+(.*)$""")
    val bulletMatch = bulletListRegex.find(lastLine)
    if (bulletMatch != null) {
        val bullet = bulletMatch.groupValues[1]
        val content = bulletMatch.groupValues[2]

        if (content.isEmpty()) {
            val newText = newValue.text.take(lastLineStart) + newValue.text.substring(newValue.selection.start)
            return newValue.copy(text = newText, selection = TextRange(lastLineStart))
        }

        val prefix = "$bullet "
        val newText = newValue.text.take(newValue.selection.start) + prefix + newValue.text.substring(newValue.selection.start)
        return newValue.copy(text = newText, selection = TextRange(newValue.selection.start + prefix.length))
    }

    val checklistRegex = Regex("""^(-\s\[\s]\s)(.*)$""")
    val checklistMatch = checklistRegex.find(lastLine)
    if (checklistMatch != null) {
        val prefix = checklistMatch.groupValues[1]
        val content = checklistMatch.groupValues[2]

        if (content.isEmpty()) {
            val newText = newValue.text.take(lastLineStart) + newValue.text.substring(newValue.selection.start)
            return newValue.copy(text = newText, selection = TextRange(lastLineStart))
        }

        val newText = newValue.text.take(newValue.selection.start) + prefix + newValue.text.substring(newValue.selection.start)
        return newValue.copy(text = newText, selection = TextRange(newValue.selection.start + prefix.length))
    }

    return newValue
}

/**
 * Wraps the current selection with the given formatting symbol (e.g. `**` or `_`),
 * or inserts the symbols around the cursor if no text is selected.
 */
fun applyFormat(value: TextFieldValue, symbol: String): TextFieldValue {
    val selection = value.selection
    val text = value.text

    val formatted = if (selection.collapsed) {
        text.take(selection.start) + symbol + symbol + text.substring(selection.end)
    } else {
        text.take(selection.start) + symbol + text.substring(selection.start, selection.end) + symbol + text.substring(selection.end)
    }

    val newCursorPos = if (selection.collapsed) selection.start + symbol.length else selection.end + symbol.length * 2
    return value.copy(text = formatted, selection = TextRange(newCursorPos))
}
