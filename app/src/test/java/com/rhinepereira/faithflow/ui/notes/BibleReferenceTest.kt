package com.rhinepereira.faithflow.ui.notes

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class BibleReferenceTest {

    @Test
    fun findBibleReference_detectsStandardReference() {
        val ref = findBibleReference("Today we reflect on John 3:16 for comfort")
        assertNotNull(ref)
        assertEquals("John", ref?.book)
        assertEquals(3, ref?.chapter)
        assertEquals(listOf(16 to null), ref?.verses)
    }

    @Test
    fun findBibleReference_detectsRangeReference() {
        val ref = findBibleReference("Read 1 Corinthians 13:4-8 carefully")
        assertNotNull(ref)
        assertEquals("1 Corinthians", ref?.book)
        assertEquals(13, ref?.chapter)
        assertEquals(listOf(4 to 8), ref?.verses)
    }

    @Test
    fun findBibleReference_detectsAbbreviation() {
        val ref = findBibleReference("Meditation on Gen 1:1")
        assertNotNull(ref)
        assertEquals("Genesis", ref?.book)
        assertEquals(1, ref?.chapter)
        assertEquals(listOf(1 to null), ref?.verses)
    }

    @Test
    fun findBibleReference_returnsNullWhenNoReference() {
        val ref = findBibleReference("This is a simple note without any scripture citation")
        assertNull(ref)
    }

    @Test
    fun plainTextPreview_stripsMarkdownFormatting() {
        val raw = "**Important:** Check _faith_ and walk in peace."
        val preview = plainTextPreview(raw)
        assertEquals("Important: Check faith and walk in peace.", preview)
    }
}
