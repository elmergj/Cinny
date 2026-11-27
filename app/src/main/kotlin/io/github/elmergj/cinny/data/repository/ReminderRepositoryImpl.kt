package io.github.elmergj.cinny.data.repository

import io.github.elmergj.cinny.data.local.dao.ReminderDao
import io.github.elmergj.cinny.data.mapper.toDomain
import io.github.elmergj.cinny.data.mapper.toEntity
import io.github.elmergj.cinny.domain.model.Reminder
import io.github.elmergj.cinny.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ReminderRepositoryImpl @Inject constructor(
    private val reminderDao: ReminderDao
) : ReminderRepository {

    override suspend fun getReminderById(id: Long): Reminder? {
        return reminderDao.getById(id)?.toDomain()
    }

    override fun getAllReminders(): Flow<List<Reminder>> {
        return reminderDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun createReminder(reminder: Reminder): Long {
        return reminderDao.insert(
            reminder.copy(id = 0L).toEntity()
        )
    }

    override suspend fun updateReminder(reminder: Reminder) {
        require(reminder.id > 0L) {
            "Cannot update a reminder without a valid ID"
        }

        reminderDao.update(reminder.toEntity())
    }

    override suspend fun deleteReminder(id: Long) {
        reminderDao.deleteById(id)
    }
}