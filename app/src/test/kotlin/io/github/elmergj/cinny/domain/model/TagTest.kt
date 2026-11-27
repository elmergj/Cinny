package io.github.elmergj.cinny.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class TagTest {

    private val now = Instant.parse("2026-11-26T12:00:00Z")

    private fun buildTag(
        id: Long = 0L,
        name: String = "Work"
    ): Tag {
        return Tag(
            id = id,
            name = name,
            createdAt = now
        )
    }

    // Valid cases

    @Test
    fun `should allow a new tag with zero ID`() {
        val tag = buildTag(id = 0L)

        assertEquals(0L, tag.id)
    }

    @Test
    fun `should allow a tag with a positive ID`() {
        val tag = buildTag(id = 1L)

        assertEquals(1L, tag.id)
    }

    // Invalid cases

    @Test(expected = IllegalArgumentException::class)
    fun `should reject a negative ID`() {
        buildTag(id = -1L)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `should reject an empty name`() {
        buildTag(name = "")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `should reject a blank name`() {
        buildTag(name = "   ")
    }
}