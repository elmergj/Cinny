package io.github.elmergj.cinny.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class NoteTest {

    private val now = Instant.parse("2026-11-26T12:00:00Z")

    private fun buildNote(
        id: Long = 0L,
        title: String = "Test note",
        content: String = "Test content",
        folderId: Long? = null,
        createdAt: Instant = now,
        updatedAt: Instant = now
    ): Note {
        return Note(
            id = id,
            title = title,
            content = content,
            createdAt = createdAt,
            updatedAt = updatedAt,
            folderId = folderId,
            isPinned = false
        )
    }

    // Valid cases

    @Test
    fun `should allow a new note with zero ID`() {
        val note = buildNote(id = 0L)

        assertEquals(0L, note.id)
    }

    @Test
    fun `should allow a note with a positive ID`() {
        val note = buildNote(id = 1L)

        assertEquals(1L, note.id)
    }

    @Test
    fun `should allow empty title and content`() {
        val note = buildNote(
            title = "",
            content = ""
        )

        assertEquals("", note.title)
        assertEquals("", note.content)
    }

    @Test
    fun `should allow a note without a folder`() {
        val note = buildNote(folderId = null)

        assertNull(note.folderId)
    }

    @Test
    fun `should allow a positive folder ID`() {
        val note = buildNote(folderId = 1L)

        assertEquals(1L, note.folderId)
    }

    // Invalid cases

    @Test(expected = IllegalArgumentException::class)
    fun `should reject a negative ID`() {
        buildNote(id = -1L)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `should reject a non-positive folder ID`() {
        buildNote(folderId = 0L)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `should reject updatedAt before createdAt`() {
        buildNote(
            createdAt = now,
            updatedAt = now.minusSeconds(1)
        )
    }
}