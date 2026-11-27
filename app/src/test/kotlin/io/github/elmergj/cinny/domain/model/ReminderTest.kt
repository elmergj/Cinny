package io.github.elmergj.cinny.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class ReminderTest {

    private val now = Instant.parse("2026-11-26T12:00:00Z")

    private fun buildReminder(
        id: Long = 0L,
        title: String = "Test reminder",
        description: String? = null,
        createdAt: Instant = now,
        updatedAt: Instant = now,
        scheduledAt: Instant = now.plusSeconds(3600),
        recurrenceType: RecurrenceType = RecurrenceType.NONE,
        recurrenceInterval: Int = 1,
        notificationOffsetMinutes: Int = 0,
        linkedNoteId: Long? = null
    ): Reminder {
        return Reminder(
            id = id,
            title = title,
            description = description,
            createdAt = createdAt,
            updatedAt = updatedAt,
            scheduledAt = scheduledAt,
            allDay = false,
            recurrenceType = recurrenceType,
            recurrenceInterval = recurrenceInterval,
            notificationOffsetMinutes = notificationOffsetMinutes,
            linkedNoteId = linkedNoteId
        )
    }

    // Valid cases

    @Test
    fun `should allow a new reminder with zero ID`() {
        val reminder = buildReminder(id = 0L)

        assertEquals(0L, reminder.id)
    }

    @Test
    fun `should allow a reminder with a positive ID`() {
        val reminder = buildReminder(id = 1L)

        assertEquals(1L, reminder.id)
    }

    @Test
    fun `should allow a reminder without a description`() {
        val reminder = buildReminder(description = null)

        assertNull(reminder.description)
    }

    @Test
    fun `should allow a scheduled date in the past`() {
        val scheduledAt = now.minusSeconds(3600)

        val reminder = buildReminder(scheduledAt = scheduledAt)

        assertEquals(scheduledAt, reminder.scheduledAt)
    }

    @Test
    fun `should allow a non-recurring reminder with interval one`() {
        val reminder = buildReminder(
            recurrenceType = RecurrenceType.NONE,
            recurrenceInterval = 1
        )

        assertEquals(RecurrenceType.NONE, reminder.recurrenceType)
        assertEquals(1, reminder.recurrenceInterval)
    }

    @Test
    fun `should allow recurring reminders with positive intervals`() {
        val recurrenceTypes = RecurrenceType.entries.filter {
            it != RecurrenceType.NONE
        }

        recurrenceTypes.forEach { type ->
            val reminder = buildReminder(
                recurrenceType = type,
                recurrenceInterval = 2
            )

            assertEquals(type, reminder.recurrenceType)
            assertEquals(2, reminder.recurrenceInterval)
        }
    }

    @Test
    fun `should allow zero and positive notification offsets`() {
        val immediate = buildReminder(
            notificationOffsetMinutes = 0
        )

        val advance = buildReminder(
            notificationOffsetMinutes = 15
        )

        assertEquals(0, immediate.notificationOffsetMinutes)
        assertEquals(15, advance.notificationOffsetMinutes)
    }

    @Test
    fun `should allow null and positive linked note IDs`() {
        val standalone = buildReminder(linkedNoteId = null)
        val linked = buildReminder(linkedNoteId = 1L)

        assertNull(standalone.linkedNoteId)
        assertEquals(1L, linked.linkedNoteId)
    }

    // Invalid cases

    @Test(expected = IllegalArgumentException::class)
    fun `should reject a negative ID`() {
        buildReminder(id = -1L)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `should reject an empty title`() {
        buildReminder(title = "")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `should reject a blank title`() {
        buildReminder(title = "   ")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `should reject updatedAt before createdAt`() {
        buildReminder(updatedAt = now.minusSeconds(1))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `should reject a non-positive recurrence interval`() {
        buildReminder(
            recurrenceType = RecurrenceType.DAILY,
            recurrenceInterval = 0
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `should reject an invalid interval for non-recurring reminders`() {
        buildReminder(
            recurrenceType = RecurrenceType.NONE,
            recurrenceInterval = 2
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `should reject a negative notification offset`() {
        buildReminder(notificationOffsetMinutes = -1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `should reject a non-positive linked note ID`() {
        buildReminder(linkedNoteId = 0L)
    }
}