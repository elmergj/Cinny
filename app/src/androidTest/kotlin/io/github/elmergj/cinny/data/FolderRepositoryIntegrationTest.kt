package io.github.elmergj.cinny.data

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.elmergj.cinny.data.local.database.AppDatabase
import io.github.elmergj.cinny.data.repository.FolderRepositoryImpl
import io.github.elmergj.cinny.data.repository.NoteRepositoryImpl
import io.github.elmergj.cinny.data.repository.ReminderRepositoryImpl
import io.github.elmergj.cinny.data.repository.TagRepositoryImpl
import io.github.elmergj.cinny.domain.model.Folder
import io.github.elmergj.cinny.domain.model.Note
import io.github.elmergj.cinny.domain.model.RecurrenceType
import io.github.elmergj.cinny.domain.model.Reminder
import io.github.elmergj.cinny.domain.model.Tag
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
class FolderRepositoryIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: FolderRepositoryImpl
    private lateinit var noteRepository: NoteRepositoryImpl
    private lateinit var tagRepository: TagRepositoryImpl
    private lateinit var reminderRepository: ReminderRepositoryImpl

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

        repository = FolderRepositoryImpl(database.folderDao())
        noteRepository = NoteRepositoryImpl(database.noteDao())
        tagRepository = TagRepositoryImpl(database.tagDao())
        reminderRepository = ReminderRepositoryImpl(database.reminderDao())
    }

    @After
    fun cleanup() {
        database.close()
    }

    private suspend fun createFolder(name: String): Long {
        val now = Instant.now()

        return repository.createFolder(
            Folder(
                id = 0L,
                name = name,
                createdAt = now,
                updatedAt = now
            )
        )
    }

    private suspend fun createNote(
        folderId: Long? = null
    ): Long {
        val now = Instant.now()

        return noteRepository.createNote(
            Note(
                id = 0L,
                title = "Test note",
                content = "Test content",
                createdAt = now,
                updatedAt = now,
                folderId = folderId,
                isPinned = false
            )
        )
    }

    private suspend fun createTag(name: String = "Work"): Long {
        return tagRepository.createTag(
            Tag(
                id = 0L,
                name = name,
                createdAt = Instant.now()
            )
        )
    }

    private suspend fun createReminder(
        linkedNoteId: Long
    ): Long {
        val now = Instant.now()

        return reminderRepository.createReminder(
            Reminder(
                id = 0L,
                title = "Test reminder",
                description = null,
                createdAt = now,
                updatedAt = now,
                scheduledAt = now.plusSeconds(3600),
                allDay = false,
                recurrenceType = RecurrenceType.NONE,
                recurrenceInterval = 1,
                notificationOffsetMinutes = 0,
                linkedNoteId = linkedNoteId
            )
        )
    }

    @Test
    fun `should delete a folder and all its notes`() = runBlocking {
        val folderId = createFolder("Work")

        val noteA = createNote(folderId)
        val noteB = createNote(folderId)

        repository.deleteFolder(folderId)

        assertNull(repository.getFolderById(folderId))
        assertNull(noteRepository.getNoteById(noteA))
        assertNull(noteRepository.getNoteById(noteB))

        assertTrue(
            noteRepository.getNotesByFolderId(folderId)
                .first()
                .isEmpty()
        )
    }

    @Test
    fun `should preserve notes outside the deleted folder`() = runBlocking {
        val folderA = createFolder("Work")
        val folderB = createFolder("Personal")

        val deletedNote = createNote(folderA)
        val preservedNote = createNote(folderB)
        val rootNote = createNote()

        repository.deleteFolder(folderA)

        assertNull(noteRepository.getNoteById(deletedNote))
        assertNotNull(repository.getFolderById(folderB))
        assertNotNull(noteRepository.getNoteById(preservedNote))
        assertNotNull(noteRepository.getNoteById(rootNote))

        assertEquals(
            setOf(preservedNote, rootNote),
            noteRepository.getAllNotes().first()
                .map { it.id }
                .toSet()
        )
    }

    @Test
    fun `should remove note associations while preserving tags`() = runBlocking {
        val folderId = createFolder("Work")
        val tagId = createTag()

        val deletedNote = createNote(folderId)
        val preservedNote = createNote()

        tagRepository.addTagToNote(deletedNote, tagId)
        tagRepository.addTagToNote(preservedNote, tagId)

        repository.deleteFolder(folderId)

        assertNull(noteRepository.getNoteById(deletedNote))
        assertNotNull(tagRepository.getTagById(tagId))

        assertTrue(
            tagRepository.getTagsForNote(deletedNote)
                .first()
                .isEmpty()
        )

        assertEquals(
            listOf(preservedNote),
            noteRepository.getNotesByTagId(tagId)
                .first()
                .map { it.id }
        )
    }

    @Test
    fun `should preserve and unlink reminders when deleting a folder`() = runBlocking {
        val folderId = createFolder("Work")

        val deletedNote = createNote(folderId)
        val preservedNote = createNote()

        val reminderA = createReminder(deletedNote)
        val reminderB = createReminder(preservedNote)

        repository.deleteFolder(folderId)

        assertNull(noteRepository.getNoteById(deletedNote))

        val unlinkedReminder = requireNotNull(
            reminderRepository.getReminderById(reminderA)
        )

        val preservedReminder = requireNotNull(
            reminderRepository.getReminderById(reminderB)
        )

        assertNull(unlinkedReminder.linkedNoteId)
        assertEquals(preservedNote, preservedReminder.linkedNoteId)
    }

    @Test
    fun `should trim folder name when creating a folder`() = runBlocking {
        val folderId = createFolder("  Work  ")

        val folder = requireNotNull(
            repository.getFolderById(folderId)
        )

        assertEquals("Work", folder.name)
    }

    @Test
    fun `should reject duplicate folder names`() = runBlocking {
        createFolder("Work")

        val result = runCatching {
            createFolder("Work")
        }

        assertTrue(result.isFailure)
        assertEquals(1, repository.getAllFolders().first().size)
    }

    @Test
    fun `should reject duplicate folder names regardless of case and surrounding spaces`() = runBlocking {
        createFolder("Work")

        val result = runCatching {
            createFolder("  work  ")
        }

        assertTrue(result.isFailure)
        assertEquals(1, repository.getAllFolders().first().size)
    }

    @Test
    fun `should rename a folder and update its modification date`() = runBlocking {
        val createdAt = Instant.now().minusSeconds(60)

        val folderId = repository.createFolder(
            Folder(
                id = 0L,
                name = "Work",
                createdAt = createdAt,
                updatedAt = createdAt
            )
        )

        val original = requireNotNull(
            repository.getFolderById(folderId)
        )

        repository.renameFolder(folderId, "  Projects  ")

        val renamed = requireNotNull(
            repository.getFolderById(folderId)
        )

        assertEquals("Projects", renamed.name)
        assertEquals(original.id, renamed.id)
        assertEquals(original.createdAt, renamed.createdAt)
        assertTrue(renamed.updatedAt.isAfter(original.updatedAt))
    }

    @Test
    fun `should preserve updatedAt when folder name remains unchanged`() = runBlocking {
        val folderId = createFolder("Work")

        val original = requireNotNull(
            repository.getFolderById(folderId)
        )

        repository.renameFolder(folderId, "  Work  ")

        val result = requireNotNull(
            repository.getFolderById(folderId)
        )

        assertEquals(original, result)
    }
}