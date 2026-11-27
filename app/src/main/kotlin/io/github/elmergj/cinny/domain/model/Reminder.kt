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
){
    init {
        require(id >= 0L) {
            "Reminder ID cannot be negative"
        }

        require(title.isNotBlank()) {
            "Reminder title cannot be blank"
        }

        require(!updatedAt.isBefore(createdAt)) {
            "Updated date cannot precede creation date"
        }

        require(recurrenceInterval >= 1) {
            "Recurrence interval must be at least one"
        }

        require(recurrenceType != RecurrenceType.NONE || recurrenceInterval == 1) {
            "Non-recurring reminders must have a recurrence interval of one"
        }

        require(notificationOffsetMinutes >= 0) {
            "Notification offset cannot be negative"
        }

        require(linkedNoteId == null || linkedNoteId > 0L) {
            "Linked note ID must be positive"
        }
    }
}
