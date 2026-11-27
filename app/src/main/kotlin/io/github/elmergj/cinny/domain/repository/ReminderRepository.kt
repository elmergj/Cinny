package io.github.elmergj.cinny.domain.repository

import io.github.elmergj.cinny.domain.model.Reminder
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {

    suspend fun getReminderById(id: Long): Reminder?
    fun getAllReminders(): Flow<List<Reminder>>

    suspend fun createReminder(reminder: Reminder): Long
    suspend fun updateReminder(reminder: Reminder)
    suspend fun deleteReminder(id: Long)
}