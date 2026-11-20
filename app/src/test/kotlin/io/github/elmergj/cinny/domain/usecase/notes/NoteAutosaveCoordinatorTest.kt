
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
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class NoteAutosaveCoordinatorTest {

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
}
