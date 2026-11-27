package io.github.elmergj.cinny.data

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.elmergj.cinny.data.local.database.AppDatabase
import io.github.elmergj.cinny.data.repository.ReminderRepositoryImpl
import io.github.elmergj.cinny.domain.model.RecurrenceType
import io.github.elmergj.cinny.domain.model.Reminder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class ReminderRepositoryIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: ReminderRepositoryImpl

    @Before
    fun setup() {
        val context = InstrumentationRegistry
            .getInstrumentation()
            .targetContext

        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .setDriver(BundledSQLiteDriver())
            .build()

        repository = ReminderRepositoryImpl(database.reminderDao())
    }

    @After
    fun cleanup() {
        database.close()
    }

    private fun buildReminder(
        title: String = "Test reminder"
    ): Reminder {
        val now = Instant.ofEpochMilli(
            Instant.now().toEpochMilli()
        )

        return Reminder(
            id = 0L,
            title = title,
            description = "Test description",
            createdAt = now,
            updatedAt = now,
            scheduledAt = now.plusSeconds(3600),
            allDay = false,
            recurrenceType = RecurrenceType.NONE,
            recurrenceInterval = 1,
            notificationOffsetMinutes = 0,
            linkedNoteId = null
        )
    }

    private suspend fun createReminder(
        title: String = "Test reminder"
    ): Long {
        return repository.createReminder(
            buildReminder(title)
        )
    }

    // CRUD tests

    @Test
    fun `should create and retrieve a reminder with a generated ID`() = runBlocking {
        val reminder = buildReminder()

        val generatedId = repository.createReminder(reminder)

        assertTrue(generatedId > 0L)

        val saved = repository.getReminderById(generatedId)

        assertNotNull(saved)
        assertEquals(
            reminder.copy(id = generatedId),
            saved
        )
    }

    @Test
    fun `should retrieve all reminders`() = runBlocking {
        val reminderA = createReminder("Work")
        val reminderB = createReminder("Personal")
        val reminderC = createReminder("Study")

        val result = repository.getAllReminders().first()

        assertEquals(3, result.size)
        assertEquals(
            setOf(reminderA, reminderB, reminderC),
            result.map { it.id }.toSet()
        )
    }

    @Test
    fun `should update an existing reminder`() = runBlocking {
        val reminderId = createReminder()

        val original = requireNotNull(
            repository.getReminderById(reminderId)
        )

        val updated = original.copy(
            title = "Updated reminder",
            description = "Updated description",
            scheduledAt = original.scheduledAt.plusSeconds(3600),
            recurrenceType = RecurrenceType.WEEKLY,
            recurrenceInterval = 2,
            notificationOffsetMinutes = 15,
            updatedAt = original.updatedAt.plusSeconds(60)
        )

        repository.updateReminder(updated)

        val result = repository.getReminderById(reminderId)

        assertEquals(updated, result)
        assertEquals(1, repository.getAllReminders().first().size)
    }

    @Test
    fun `should reject updating a reminder without a valid ID`() = runBlocking {
        val reminder = buildReminder()

        val result = runCatching {
            repository.updateReminder(reminder)
        }

        assertTrue(
            result.exceptionOrNull() is IllegalArgumentException
        )

        assertTrue(
            repository.getAllReminders().first().isEmpty()
        )
    }

    @Test
    fun `should delete an existing reminder`() = runBlocking {
        val reminderId = createReminder()
        val preservedId = createReminder("Personal")

        repository.deleteReminder(reminderId)

        assertNull(repository.getReminderById(reminderId))
        assertNotNull(repository.getReminderById(preservedId))

        assertEquals(
            listOf(preservedId),
            repository.getAllReminders().first().map { it.id }
        )
    }

    @Test
    fun `should retrieve reminders ordered by scheduled date`() = runBlocking {
        val reminder = buildReminder()

        val late = repository.createReminder(
            reminder.copy(
                scheduledAt = reminder.scheduledAt.plusSeconds(7200)
            )
        )

        val early = repository.createReminder(
            reminder
        )

        val middle = repository.createReminder(
            reminder.copy(
                scheduledAt = reminder.scheduledAt.plusSeconds(3600)
            )
        )

        val result = repository.getAllReminders().first()

        assertEquals(
            listOf(early, middle, late),
            result.map { it.id }
        )
    }

    @Test
    fun `should search reminders by title and order results by scheduled date`() = runBlocking {
        val reminder = buildReminder()

        val late = repository.createReminder(
            reminder.copy(
                title = "Work meeting",
                scheduledAt = reminder.scheduledAt.plusSeconds(3600)
            )
        )

        val early = repository.createReminder(
            reminder.copy(
                title = "Finish work report"
            )
        )

        repository.createReminder(
            reminder.copy(
                title = "Personal",
                description = "Work related description"
            )
        )

        val result = repository.searchReminders("work").first()

        assertEquals(
            listOf(early, late),
            result.map { it.id }
        )
    }

    @Test
    fun `should return an empty list when reminder search has no matches`() = runBlocking {
        createReminder("Work meeting")

        val result = repository.searchReminders("Shopping").first()

        assertTrue(result.isEmpty())
    }
}