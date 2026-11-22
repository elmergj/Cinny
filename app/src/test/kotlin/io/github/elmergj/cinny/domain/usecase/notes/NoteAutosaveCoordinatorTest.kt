
package io.github.elmergj.cinny.domain.usecase.notes

import io.github.elmergj.cinny.domain.model.Note
import io.github.elmergj.cinny.domain.repository.NoteRepository
import java.time.Instant
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class NoteAutosaveCoordinatorTest {

    private class FakeNoteRepository(
        initialNote: Note
    ) : NoteRepository {

        var storedNote: Note = initialNote
            private set

        val savedContents = mutableListOf<String>()

        val firstWriteStarted = CompletableDeferred<Unit>()
        val releaseFirstWrite = CompletableDeferred<Unit>()

        var activeWrites = 0
            private set

        var maxConcurrentWrites = 0
            private set

        var failNextWrite = false

        override suspend fun getNoteById(id: Long): Note? {
            return storedNote.takeIf { it.id == id }
        }

        override fun getAllNotes(): Flow<List<Note>> {
            return flowOf(listOf(storedNote))
        }

        override suspend fun createNote(note: Note): Long {
            storedNote = note.copy(id = 1L)
            return storedNote.id
        }

        override suspend fun updateNote(note: Note) {
            activeWrites++

            maxConcurrentWrites = maxOf(
                maxConcurrentWrites,
                activeWrites
            )

            try {
                // Simulate a database write failure.
                if (failNextWrite) {
                    failNextWrite = false
                    throw IllegalStateException("Simulated database failure")
                }

                // Simulate a slow first database write.
                if (savedContents.isEmpty()) {
                    firstWriteStarted.complete(Unit)
                    releaseFirstWrite.await()
                }

                storedNote = note
                savedContents.add(note.content)

            } finally {
                activeWrites--
            }
        }

        override suspend fun deleteNote(id: Long) {
            // Not required for this test.
        }
    }

    @Test
    fun `should persist latest changes when editing during save`() = runTest {
        val initialNote = Note(
            id = 1L,
            title = "Test note",
            content = "",
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            folderId = null,
            isPinned = false
        )

        val repository = FakeNoteRepository(initialNote)
        val coordinator = NoteAutosaveCoordinator(repository)

        coordinator.setNote(initialNote)

        // 1. Start the first autosave.
        coordinator.onEdited(
            title = "Test note",
            content = "Hello",
            scope = this        )

        advanceTimeBy(600.milliseconds)
        runCurrent()

        // The first database write is now suspended.
        assertTrue(repository.firstWriteStarted.isCompleted)
        assertEquals(1, repository.activeWrites)

        // 2. Edit the note while the first write is pending.
        coordinator.onEdited(
            title = "Test note",
            content = "Hello world",
            scope = this        )

        // 3. Allow the first write to complete.
        repository.releaseFirstWrite.complete(Unit)

        advanceUntilIdle()

        // 4. Verify the final persisted content.
        assertEquals("Hello world", repository.storedNote.content)

        // Verify both versions were persisted in order.
        assertEquals(
            listOf("Hello", "Hello world"),
            repository.savedContents
        )

        // Verify that writes never overlapped.
        assertEquals(1, repository.maxConcurrentWrites)

        // Verify that no unsaved changes remain.
        assertTrue(coordinator.flush())
        assertEquals(2, repository.savedContents.size)
    }

    @Test
    fun `should preserve pending changes and allow retry when save fails`() = runTest {
        val now = Instant.now()

        val initialNote = Note(
            id = 1L,
            title = "Test note",
            content = "",
            createdAt = now,
            updatedAt = now,
            folderId = null,
            isPinned = false
        )

        val repository = FakeNoteRepository(initialNote)
        val coordinator = NoteAutosaveCoordinator(repository)

        coordinator.setNote(initialNote)

        // Simulate a failure during the next database write.
        repository.failNextWrite = true

        coordinator.onEdited(
            title = "Test note",
            content = "Unsaved changes",
            scope = this
        )

        // Trigger an immediate save, skipping the debounce.
        val firstAttempt = coordinator.flush()

        // Verify the failure was detected.
        assertFalse(firstAttempt)
        assertTrue(coordinator.saveError.value)

        // Verify the stored note remains unchanged.
        assertEquals("", repository.storedNote.content)

        // Allow the successful retry to finish without blocking.
        repository.releaseFirstWrite.complete(Unit)

        // Retry without losing the pending changes.
        val secondAttempt = coordinator.flush()

        // Verify the retry succeeded.
        assertTrue(secondAttempt)
        assertFalse(coordinator.saveError.value)
        assertEquals("Unsaved changes", repository.storedNote.content)

        // Verify only the successful write was recorded.
        assertEquals(
            listOf("Unsaved changes"),
            repository.savedContents
        )
    }

    @Test
    fun `should save changes after debounce delay`() = runTest {
        val now = Instant.now()

        val note = Note(
            id = 1L,
            title = "Test note",
            content = "",
            createdAt = now,
            updatedAt = now,
            folderId = null,
            isPinned = false
        )

        val repository = FakeNoteRepository(note)
        val coordinator = NoteAutosaveCoordinator(repository)

        repository.releaseFirstWrite.complete(Unit)
        coordinator.setNote(note)

        coordinator.onEdited(
            title = note.title,
            content = "Hello",
            scope = this
        )

        // Verify that nothing is saved before 600 ms.
        advanceTimeBy(599.milliseconds)
        runCurrent()

        assertTrue(repository.savedContents.isEmpty())

        // Complete the debounce interval.
        advanceTimeBy(1.milliseconds)
        runCurrent()

        assertEquals("Hello", repository.storedNote.content)
        assertEquals(1, repository.savedContents.size)
    }

    @Test
    fun `should reset debounce when new edits arrive`() = runTest {

        val now = Instant.now()

        val note = Note(
            id = 1L,
            title = "Test note",
            content = "",
            createdAt = now,
            updatedAt = now,
            folderId = null,
            isPinned = false
        )

        val repository = FakeNoteRepository(note)
        val coordinator = NoteAutosaveCoordinator(repository)

        repository.releaseFirstWrite.complete(Unit)
        coordinator.setNote(note)

        // First edit at 0 ms.
        coordinator.onEdited(
            title = note.title,
            content = "H",
            scope = this
        )

        advanceTimeBy(300.milliseconds)

        // Second edit at 300 ms.
        coordinator.onEdited(
            title = note.title,
            content = "He",
            scope = this
        )

        advanceTimeBy(200.milliseconds)

        // Third edit at 500 ms.
        coordinator.onEdited(
            title = note.title,
            content = "Hello",
            scope = this
        )

        // At 1099 ms, the latest debounce has not completed.
        advanceTimeBy(599.milliseconds)
        runCurrent()

        assertTrue(repository.savedContents.isEmpty())

        // At 1100 ms, only the latest content is saved.
        advanceTimeBy(1.milliseconds)
        runCurrent()

        assertEquals("Hello", repository.storedNote.content)
        assertEquals(listOf("Hello"), repository.savedContents)
    }

    @Test
    fun `should flush pending changes before debounce completes`() = runTest {
        val now = Instant.now()

        val note = Note(
            id = 1L,
            title = "Test note",
            content = "",
            createdAt = now,
            updatedAt = now,
            folderId = null,
            isPinned = false
        )

        val repository = FakeNoteRepository(note)
        val coordinator = NoteAutosaveCoordinator(repository)

        repository.releaseFirstWrite.complete(Unit)
        coordinator.setNote(note)

        coordinator.onEdited(
            title = note.title,
            content = "Immediate save",
            scope = this
        )

        // Flush immediately without advancing virtual time.
        val saved = coordinator.flush()

        assertTrue(saved)
        assertEquals("Immediate save", repository.storedNote.content)
        assertEquals(1, repository.savedContents.size)

        // Verify that the canceled debounce does not save again.
        advanceUntilIdle()

        assertEquals(1, repository.savedContents.size)
    }

    @Test
    fun `should reject replacing an active note with pending changes`() = runTest {
        val now = Instant.now()

        val noteA = Note(
            id = 1L,
            title = "Note A",
            content = "",
            createdAt = now,
            updatedAt = now,
            folderId = null,
            isPinned = false
        )

        val noteB = noteA.copy(
            id = 2L,
            title = "Note B"
        )

        val repository = FakeNoteRepository(noteA)
        val coordinator = NoteAutosaveCoordinator(repository)

        repository.releaseFirstWrite.complete(Unit)

        coordinator.setNote(noteA)

        coordinator.onEdited(
            title = noteA.title,
            content = "Pending changes",
            scope = this
        )

        // Replacing an active note must fail.
        assertFailsWith<IllegalStateException> {
            coordinator.setNote(noteB)
        }

        // The original note and its pending changes remain intact.
        assertTrue(coordinator.flush())
        assertEquals(1L, repository.storedNote.id)
        assertEquals("Pending changes", repository.storedNote.content)
    }

    @Test
    fun `should reject clearing a note with unsaved changes`() = runTest {
        val now = Instant.now()

        val note = Note(
            id = 1L,
            title = "Test note",
            content = "",
            createdAt = now,
            updatedAt = now,
            folderId = null,
            isPinned = false
        )

        val repository = FakeNoteRepository(note)
        val coordinator = NoteAutosaveCoordinator(repository)

        repository.releaseFirstWrite.complete(Unit)

        coordinator.setNote(note)

        coordinator.onEdited(
            title = note.title,
            content = "Unsaved changes",
            scope = this
        )

        // Clearing while changes are pending must fail.
        assertFailsWith<IllegalStateException> {
            coordinator.clear()
        }

        // After flushing, clearing is safe.
        assertTrue(coordinator.flush())
        coordinator.clear()

        assertEquals("Unsaved changes", repository.storedNote.content)
        assertFalse(coordinator.saveError.value)
    }
}
