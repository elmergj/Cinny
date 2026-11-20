package io.github.elmergj.cinny.domain.model

import java.time.Instant

data class Reminder(
    val id: Long,
    val title: String,
    val description: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val scheduledAt: Instant,
    val allDay: Boolean,
    val recurrenceType: RecurrenceType,
    val recurrenceInterval: Int,
    val notificationOffsetMinutes: Int,
    val linkedNoteId: Long?
)
