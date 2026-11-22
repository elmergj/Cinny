
package io.github.elmergj.cinny.domain.usecase.notes

import io.github.elmergj.cinny.domain.model.Note
import io.github.elmergj.cinny.domain.repository.NoteRepository
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import kotlin.time.Duration.Companion.milliseconds

class NoteAutosaveCoordinator @Inject constructor(
    private val noteRepository: NoteRepository
) {

    private val mutex = Mutex()
    private var debounceJob: Job? = null

    private var currentNote: Note? = null
    private var pendingTitle = ""
    private var pendingContent = ""

    private val _isSaving = MutableStateFlow(false)
    val isSaving = _isSaving.asStateFlow()

    private val _saveError = MutableStateFlow(false)
    val saveError = _saveError.asStateFlow()

    private companion object {
        val AUTOSAVE_DELAY = 600.milliseconds
    }

    suspend fun setNote(note: Note) {
        mutex.withLock {
            check(currentNote == null) {
                "Close the current note before opening another"
            }

            debounceJob?.cancel()
            debounceJob = null

            currentNote = note
            pendingTitle = note.title
            pendingContent = note.content
            _saveError.value = false
        }
    }

    fun onEdited(
        title: String,
        content: String,
        scope: CoroutineScope
    ) {
        if (currentNote == null) return

        pendingTitle = title
        pendingContent = content

        debounceJob?.cancel()

        // Only the timer is cancelled, never an active database write.
        debounceJob = scope.launch {
            delay(AUTOSAVE_DELAY)

            // Run the write independently from the debounce timer.
            scope.launch {
                flush()
            }
        }
    }

    suspend fun flush(): Boolean {
        debounceJob?.cancel()
        debounceJob = null

        return mutex.withLock {
            val note = currentNote ?: return@withLock true

            if (
                note.title == pendingTitle &&
                note.content == pendingContent
            ) {
                return@withLock true
            }

            _isSaving.value = true

            try {
                // Persist changes sequentially until the latest
                // editor snapshot has been saved.
                var savedNote = note

                while (
                    savedNote.title != pendingTitle ||
                    savedNote.content != pendingContent
                ) {
                    val titleSnapshot = pendingTitle
                    val contentSnapshot = pendingContent

                    val updatedNote = savedNote.copy(
                        title = titleSnapshot,
                        content = contentSnapshot,
                        updatedAt = Instant.now()
                    )

                    noteRepository.updateNote(updatedNote)

                    savedNote = updatedNote
                    currentNote = savedNote
                }

                _saveError.value = false
                true

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _saveError.value = true
                false
            } finally {
                _isSaving.value = false
            }
        }
    }

    suspend fun clear() {
        mutex.withLock {
            val note = currentNote

            check(
                note == null ||
                        (note.title == pendingTitle &&
                                note.content == pendingContent)
            ) {
                "Cannot clear a note with unsaved changes"
            }

            debounceJob?.cancel()
            debounceJob = null

            currentNote = null
            pendingTitle = ""
            pendingContent = ""
            _saveError.value = false
        }
    }
}
