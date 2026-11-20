
package io.github.elmergj.cinny.data

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.elmergj.cinny.data.local.database.AppDatabase
import io.github.elmergj.cinny.data.repository.NoteRepositoryImpl
import io.github.elmergj.cinny.domain.model.Note
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
}
