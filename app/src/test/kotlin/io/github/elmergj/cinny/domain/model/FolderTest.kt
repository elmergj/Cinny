package io.github.elmergj.cinny.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class FolderTest {

    private val now = Instant.parse("2026-11-26T12:00:00Z")

    private fun buildFolder(
        id: Long = 0L,
        name: String = "Work",
        createdAt: Instant = now,
        updatedAt: Instant = now
    ): Folder {
        return Folder(
            id = id,
            name = name,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    // Valid cases

    @Test
    fun `should allow a new folder with zero ID`() {
        val folder = buildFolder(id = 0L)

        assertEquals(0L, folder.id)
    }

    @Test
    fun `should allow a folder with a positive ID`() {
        val folder = buildFolder(id = 1L)

        assertEquals(1L, folder.id)
    }

    @Test
    fun `should allow updatedAt after createdAt`() {
        val updatedAt = now.plusSeconds(60)

        val folder = buildFolder(updatedAt = updatedAt)

        assertEquals(updatedAt, folder.updatedAt)
    }

    // Invalid cases

    @Test(expected = IllegalArgumentException::class)
    fun `should reject a negative ID`() {
        buildFolder(id = -1L)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `should reject an empty name`() {
        buildFolder(name = "")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `should reject a blank name`() {
        buildFolder(name = "   ")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `should reject updatedAt before createdAt`() {
        buildFolder(updatedAt = now.minusSeconds(1))
    }
}