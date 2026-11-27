
package io.github.elmergj.cinny.data

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.elmergj.cinny.data.local.database.AppDatabase
import io.github.elmergj.cinny.data.local.database.entity.FolderEntity
import io.github.elmergj.cinny.data.local.database.entity.NoteTagCrossRef
import io.github.elmergj.cinny.data.local.database.entity.TagEntity
import io.github.elmergj.cinny.data.repository.NoteRepositoryImpl
import io.github.elmergj.cinny.data.repository.ReminderRepositoryImpl
import io.github.elmergj.cinny.domain.model.Note
import io.github.elmergj.cinny.domain.model.RecurrenceType
import io.github.elmergj.cinny.domain.model.Reminder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class NoteRepositoryIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: NoteRepositoryImpl

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

        repository = NoteRepositoryImpl(database.noteDao())
    }

    @After
    fun cleanup() {
        database.close()
    }


    private suspend fun createNote(
        title: String = "Test note",
        content: String = "Test content",
        folderId: Long? = null,
        isPinned: Boolean = false,
        updatedAt: Instant = Instant.now()
    ): Long {
        return repository.createNote(
            Note(
                id = 0L,
                title = title,
                content = content,
                createdAt = updatedAt,
                updatedAt = updatedAt,
                folderId = folderId,
                isPinned = isPinned
            )
        )
    }

    private suspend fun createFolder(name: String): Long {
        val now = Instant.now()

        return database.folderDao().insert(
            FolderEntity(
                id = 0L,
                name = name,
                createdAt = now,
                updatedAt = now
            )
        )
    }

    private suspend fun createTag(name: String): Long {
        return database.tagDao().insert(
            TagEntity(
                id = 0L,
                name = name,
                createdAt = Instant.now()
            )
        )
    }

    @Test
    fun `should create read update and delete a note`() = runBlocking {
        val now = Instant.now()

        // 1. CREATE
        val note = Note(
            id = 0L,
            title = "Test note",
            content = "Testing Room 3",
            createdAt = now,
            updatedAt = now,
            folderId = null,
            isPinned = false
        )

        val generatedId = repository.createNote(note)

        assertTrue(generatedId > 0L)

        // 2. READ
        val saved = repository.getNoteById(generatedId)

        assertNotNull(saved)
        assertEquals(generatedId, saved?.id)
        assertEquals("Test note", saved?.title)
        assertEquals("Testing Room 3", saved?.content)

        // 3. UPDATE
        val updated = note.copy(
            id = generatedId,
            title = "Updated note",
            updatedAt = now.plusSeconds(60)
        )

        repository.updateNote(updated)

        val result = repository.getNoteById(generatedId)

        assertNotNull(result)
        assertEquals("Updated note", result?.title)
        assertEquals(
            updated.updatedAt.toEpochMilli(),
            result?.updatedAt?.toEpochMilli()
        )
        assertEquals(1, repository.getAllNotes().first().size)

        // 4. DELETE
        repository.deleteNote(generatedId)

        val deleted = repository.getNoteById(generatedId)

        assertNull(deleted)
        assertTrue(repository.getAllNotes().first().isEmpty())
    }

    @Test
    fun `should retrieve only notes belonging to the specified folder`() = runBlocking {
        val folderA = createFolder("Work")
        val folderB = createFolder("Personal")

        val noteA = createNote(folderId = folderA)
        val noteB = createNote(folderId = folderA)

        createNote(folderId = folderB)
        createNote()

        val result = repository.getNotesByFolderId(folderA).first()

        assertEquals(2, result.size)
        assertEquals(
            setOf(noteA, noteB),
            result.map { it.id }.toSet()
        )
    }

    @Test
    fun `should retrieve only notes without a folder`() = runBlocking {
        val folderId = createFolder("Work")

        val noteA = createNote()
        val noteB = createNote()

        createNote(folderId = folderId)

        val result = repository.getNotesByFolderId(null).first()

        assertEquals(2, result.size)
        assertEquals(
            setOf(noteA, noteB),
            result.map { it.id }.toSet()
        )
        assertTrue(result.all { it.folderId == null })
    }

    @Test
    fun `should retrieve only notes associated with the specified tag`() = runBlocking {
        val tagA = createTag("Work")
        val tagB = createTag("Personal")

        val noteA = createNote()
        val noteB = createNote()
        val noteC = createNote()

        createNote()

        database.tagDao().addTagToNote(
            NoteTagCrossRef(noteA, tagA)
        )

        database.tagDao().addTagToNote(
            NoteTagCrossRef(noteB, tagA)
        )

        database.tagDao().addTagToNote(
            NoteTagCrossRef(noteC, tagB)
        )

        database.tagDao().addTagToNote(
            NoteTagCrossRef(noteA, tagB)
        )

        val result = repository.getNotesByTagId(tagA).first()

        assertEquals(2, result.size)
        assertEquals(
            setOf(noteA, noteB),
            result.map { it.id }.toSet()
        )
    }

    @Test
    fun `should search notes by title or content`() = runBlocking {
        val noteA = createNote(
            title = "Kotlin project",
            content = "Development"
        )

        val noteB = createNote(
            title = "Learning notes",
            content = "Practicing Kotlin"
        )

        createNote(
            title = "Shopping list",
            content = "Buy groceries"
        )

        val result = repository.searchNotes("Kotlin").first()

        assertEquals(2, result.size)
        assertEquals(
            setOf(noteA, noteB),
            result.map { it.id }.toSet()
        )
    }

    @Test
    fun `should return an empty list when search has no matches`() = runBlocking {
        createNote(
            title = "Kotlin project",
            content = "Development"
        )

        val result = repository.searchNotes("Python").first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `should order pinned notes first and then by last update`() = runBlocking {
        val now = Instant.now()

        val unpinnedOld = createNote(
            updatedAt = now.minusSeconds(120)
        )

        val pinnedOld = createNote(
            isPinned = true,
            updatedAt = now.minusSeconds(180)
        )

        val unpinnedRecent = createNote(
            updatedAt = now
        )

        val pinnedRecent = createNote(
            isPinned = true,
            updatedAt = now.minusSeconds(60)
        )

        val result = repository.getAllNotes().first()

        assertEquals(
            listOf(
                pinnedRecent,
                pinnedOld,
                unpinnedRecent,
                unpinnedOld
            ),
            result.map { it.id }
        )
    }

    @Test
    fun `should pin and unpin a note without changing updatedAt`() = runBlocking {
        val noteId = createNote()
        val original = requireNotNull(repository.getNoteById(noteId))

        repository.setNotePinned(noteId, true)

        val pinned = requireNotNull(repository.getNoteById(noteId))

        assertTrue(pinned.isPinned)
        assertEquals(original.updatedAt, pinned.updatedAt)

        repository.setNotePinned(noteId, false)

        val unpinned = requireNotNull(repository.getNoteById(noteId))

        assertFalse(unpinned.isPinned)
        assertEquals(original.updatedAt, unpinned.updatedAt)
    }

    @Test
    fun `should move a note between folders and root without changing updatedAt`() = runBlocking {
        val folderA = createFolder("Work")
        val folderB = createFolder("Personal")

        val noteId = createNote()
        val original = requireNotNull(repository.getNoteById(noteId))

        // Move from root to the first folder.
        repository.moveNote(noteId, folderA)

        val movedToA = requireNotNull(repository.getNoteById(noteId))

        assertEquals(folderA, movedToA.folderId)
        assertEquals(original.updatedAt, movedToA.updatedAt)

        // Move between folders.
        repository.moveNote(noteId, folderB)

        val movedToB = requireNotNull(repository.getNoteById(noteId))

        assertEquals(folderB, movedToB.folderId)
        assertEquals(original.updatedAt, movedToB.updatedAt)

        // Move back to root.
        repository.moveNote(noteId, null)

        val movedToRoot = requireNotNull(repository.getNoteById(noteId))

        assertNull(movedToRoot.folderId)
        assertEquals(original.updatedAt, movedToRoot.updatedAt)
    }

    @Test
    fun `should delete a note while preserving tags and unlinking reminders`() = runBlocking {
        val noteId = createNote()
        val preservedNoteId = createNote()
        val tagId = createTag("Work")

        val tagDao = database.tagDao()
        val reminderRepository = ReminderRepositoryImpl(database.reminderDao())

        tagDao.addTagToNote(
            NoteTagCrossRef(noteId, tagId)
        )

        tagDao.addTagToNote(
            NoteTagCrossRef(preservedNoteId, tagId)
        )

        val now = Instant.now()

        val reminderId = reminderRepository.createReminder(
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
                linkedNoteId = noteId
            )
        )

        repository.deleteNote(noteId)

        // The note is deleted.
        assertNull(repository.getNoteById(noteId))

        // The tag is preserved.
        assertNotNull(tagDao.getById(tagId))

        // Associations belonging to the deleted note are removed.
        assertTrue(tagDao.getTagsForNote(noteId).first().isEmpty())

        // Other notes and their associations are preserved.
        assertNotNull(repository.getNoteById(preservedNoteId))

        assertEquals(
            listOf(preservedNoteId),
            repository.getNotesByTagId(tagId).first().map { it.id }
        )

        // The reminder is preserved and unlinked.
        val reminder = requireNotNull(
            reminderRepository.getReminderById(reminderId)
        )

        assertNull(reminder.linkedNoteId)
    }

    @Test
    fun `should order notes by priority when filtering by folder`() = runBlocking {
        val folderId = createFolder("Work")
        val now = Instant.now()

        val unpinned = createNote(
            folderId = folderId,
            updatedAt = now
        )

        val pinnedOld = createNote(
            folderId = folderId,
            isPinned = true,
            updatedAt = now.minusSeconds(120)
        )

        val pinnedRecent = createNote(
            folderId = folderId,
            isPinned = true,
            updatedAt = now.minusSeconds(60)
        )

        val result = repository.getNotesByFolderId(folderId).first()

        assertEquals(
            listOf(pinnedRecent, pinnedOld, unpinned),
            result.map { it.id }
        )
    }

    @Test
    fun `should order notes by priority when filtering by tag`() = runBlocking {
        val tagId = createTag("Work")
        val now = Instant.now()

        val unpinned = createNote(
            updatedAt = now
        )

        val pinnedOld = createNote(
            isPinned = true,
            updatedAt = now.minusSeconds(120)
        )

        val pinnedRecent = createNote(
            isPinned = true,
            updatedAt = now.minusSeconds(60)
        )

        val tagDao = database.tagDao()

        listOf(unpinned, pinnedOld, pinnedRecent).forEach { noteId ->
            tagDao.addTagToNote(
                NoteTagCrossRef(noteId, tagId)
            )
        }

        val result = repository.getNotesByTagId(tagId).first()

        assertEquals(
            listOf(pinnedRecent, pinnedOld, unpinned),
            result.map { it.id }
        )
    }

    @Test
    fun `should order search results by priority`() = runBlocking {
        val now = Instant.now()

        val unpinned = createNote(
            title = "Kotlin",
            updatedAt = now
        )

        val pinnedOld = createNote(
            title = "Kotlin",
            isPinned = true,
            updatedAt = now.minusSeconds(120)
        )

        val pinnedRecent = createNote(
            title = "Kotlin",
            isPinned = true,
            updatedAt = now.minusSeconds(60)
        )

        val result = repository.searchNotes("Kotlin").first()

        assertEquals(
            listOf(pinnedRecent, pinnedOld, unpinned),
            result.map { it.id }
        )
    }

    @Test
    fun `should order notes by descending ID when update dates are equal`() = runBlocking {
        val now = Instant.now()

        val noteA = createNote(updatedAt = now)
        val noteB = createNote(updatedAt = now)
        val noteC = createNote(updatedAt = now)

        val result = repository.getAllNotes().first()

        assertEquals(
            listOf(noteC, noteB, noteA),
            result.map { it.id }
        )
    }
}
