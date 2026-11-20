package io.github.elmergj.cinny.data.local.database.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import io.github.elmergj.cinny.domain.model.RecurrenceType
import java.time.Instant

@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["linkedNoteId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("linkedNoteId")]
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
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