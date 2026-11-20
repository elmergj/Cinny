package io.github.elmergj.cinny.data.mapper

import io.github.elmergj.cinny.data.local.database.entity.ReminderEntity
import io.github.elmergj.cinny.domain.model.Reminder

fun ReminderEntity.toDomain(): Reminder = Reminder(
    id = id,
    title = title,
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt,
    scheduledAt = scheduledAt,
    allDay = allDay,
    recurrenceType = recurrenceType,
    recurrenceInterval = recurrenceInterval,
    notificationOffsetMinutes = notificationOffsetMinutes,
    linkedNoteId = linkedNoteId
)

fun Reminder.toEntity(): ReminderEntity = ReminderEntity(
    id = id,
    title = title,
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt,
    scheduledAt = scheduledAt,
    allDay = allDay,
    recurrenceType = recurrenceType,
    recurrenceInterval = recurrenceInterval,
    notificationOffsetMinutes = notificationOffsetMinutes,
    linkedNoteId = linkedNoteId
)