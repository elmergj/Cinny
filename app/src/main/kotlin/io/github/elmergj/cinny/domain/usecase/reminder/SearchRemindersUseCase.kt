package io.github.elmergj.cinny.domain.usecase.reminder

import io.github.elmergj.cinny.domain.model.Reminder
import io.github.elmergj.cinny.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchRemindersUseCase @Inject constructor(
    private val reminderRepository: ReminderRepository
) {
    operator fun invoke(query: String): Flow<List<Reminder>> {
        return reminderRepository.searchReminders(query)
    }
}