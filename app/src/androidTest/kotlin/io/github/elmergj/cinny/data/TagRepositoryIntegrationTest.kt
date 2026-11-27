package io.github.elmergj.cinny.data

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.elmergj.cinny.data.local.database.AppDatabase
import io.github.elmergj.cinny.data.repository.NoteRepositoryImpl
import io.github.elmergj.cinny.data.repository.TagRepositoryImpl
import io.github.elmergj.cinny.domain.model.Note
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
class TagRepositoryIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: TagRepositoryImpl
    private lateinit var noteRepository: NoteRepositoryImpl

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

        repository = TagRepositoryImpl(database.tagDao())
        noteRepository = NoteRepositoryImpl(database.noteDao())
    }

    @After
    fun cleanup() {
        database.close()
    }

    private suspend fun createTag(name: String = "Work"): Long {
        return repository.createTag(
            Tag(
                id = 0L,
                name = name,
                createdAt = Instant.now()
            )
        )
    }

    private suspend fun createNote(): Long {
        val now = Instant.now()

        return noteRepository.createNote(
            Note(
                id = 0L,
                title = "Test note",
                content = "Test content",
                createdAt = now,
                updatedAt = now,
                folderId = null,
                isPinned = false
            )
        )
    }

    @Test
    fun `should create and retrieve a tag`() = runBlocking {
        val tagId = createTag(name = "Work")

        val saved = repository.getTagById(tagId)

        assertNotNull(saved)
        assertEquals(tagId, saved?.id)
        assertEquals("Work", saved?.name)
    }

    @Test
    fun `should retrieve all tags ordered by name`() = runBlocking {
        createTag("Work")
        createTag("Personal")
        createTag("Ideas")

        val result = repository.getAllTags().first()

        assertEquals(
            listOf("Ideas", "Personal", "Work"),
            result.map { it.name }
        )
    }

    @Test
    fun `should rename a tag without changing its creation date or associations`() = runBlocking {
        val tagId = createTag(name = "Work")
        val noteId = createNote()

        repository.addTagToNote(noteId, tagId)

        val original = requireNotNull(
            repository.getTagById(tagId)
        )

        repository.renameTag(tagId, "  Projects  ")

        val renamed = requireNotNull(
            repository.getTagById(tagId)
        )

        assertEquals("Projects", renamed.name)
        assertEquals(original.id, renamed.id)
        assertEquals(original.createdAt, renamed.createdAt)

        val associatedTags = repository
            .getTagsForNote(noteId)
            .first()

        assertEquals(listOf(renamed), associatedTags)
    }

    @Test
    fun `should associate multiple tags with a note`() = runBlocking {
        val noteId = createNote()
        val tagA = createTag("Work")
        val tagB = createTag("Personal")

        createTag("Unrelated")

        repository.addTagToNote(noteId, tagA)
        repository.addTagToNote(noteId, tagB)

        val result = repository.getTagsForNote(noteId).first()

        assertEquals(2, result.size)
        assertEquals(
            setOf(tagA, tagB),
            result.map { it.id }.toSet()
        )
    }

    @Test
    fun `should ignore duplicate tag associations`() = runBlocking {
        val noteId = createNote()
        val tagId = createTag()

        repository.addTagToNote(noteId, tagId)
        repository.addTagToNote(noteId, tagId)

        val result = repository.getTagsForNote(noteId).first()

        assertEquals(1, result.size)
        assertEquals(tagId, result.first().id)
    }

    @Test
    fun `should remove a tag association without deleting the tag or note`() = runBlocking {
        val noteId = createNote()
        val tagA = createTag("Work")
        val tagB = createTag("Personal")

        repository.addTagToNote(noteId, tagA)
        repository.addTagToNote(noteId, tagB)

        repository.removeTagFromNote(noteId, tagA)

        val result = repository.getTagsForNote(noteId).first()

        assertEquals(listOf(tagB), result.map { it.id })
        assertNotNull(repository.getTagById(tagA))
        assertNotNull(noteRepository.getNoteById(noteId))
    }

    @Test
    fun `should delete a tag without deleting associated notes`() = runBlocking {
        val tagId = createTag("Work")
        val remainingTagId = createTag("Personal")

        val noteA = createNote()
        val noteB = createNote()

        repository.addTagToNote(noteA, tagId)
        repository.addTagToNote(noteB, tagId)
        repository.addTagToNote(noteA, remainingTagId)

        repository.deleteTag(tagId)

        // The tag is deleted.
        assertNull(repository.getTagById(tagId))

        // Associated notes are preserved.
        assertNotNull(noteRepository.getNoteById(noteA))
        assertNotNull(noteRepository.getNoteById(noteB))

        // Only associations belonging to the deleted tag are removed.
        assertEquals(
            listOf(remainingTagId),
            repository.getTagsForNote(noteA).first().map { it.id }
        )

        assertTrue(
            repository.getTagsForNote(noteB).first().isEmpty()
        )

        assertTrue(
            noteRepository.getNotesByTagId(tagId).first().isEmpty()
        )
    }

    @Test
    fun `should trim tag name when creating a tag`() = runBlocking {
        val tagId = createTag("  Work  ")

        val tag = requireNotNull(repository.getTagById(tagId))

        assertEquals("Work", tag.name)
    }

    @Test
    fun `should reject duplicate tag names`() = runBlocking {
        createTag("Work")

        val result = runCatching {
            createTag("Work")
        }

        assertTrue(result.isFailure)
        assertEquals(1, repository.getAllTags().first().size)
    }

    @Test
    fun `should reject duplicate tag names regardless of case and surrounding spaces`() = runBlocking {
        createTag("Work")

        val result = runCatching {
            createTag("  work  ")
        }

        assertTrue(result.isFailure)
        assertEquals(1, repository.getAllTags().first().size)
    }

    @Test
    fun `should reject renaming a tag to an existing name`() = runBlocking {
        createTag("Work")
        val tagId = createTag("Personal")

        val original = requireNotNull(repository.getTagById(tagId))

        val result = runCatching {
            repository.renameTag(tagId, "  work  ")
        }

        assertTrue(result.isFailure)
        assertEquals(original, repository.getTagById(tagId))
        assertEquals(2, repository.getAllTags().first().size)
    }
}