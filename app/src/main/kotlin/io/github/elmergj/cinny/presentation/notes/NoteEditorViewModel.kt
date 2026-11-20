
package io.github.elmergj.cinny.presentation.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.elmergj.cinny.domain.model.Note
import io.github.elmergj.cinny.domain.repository.NoteRepository
import io.github.elmergj.cinny.domain.usecase.notes.NoteAutosaveCoordinator
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class NoteEditorViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val autosaveCoordinator: NoteAutosaveCoordinator
) : ViewModel() {

    private val _noteId = MutableStateFlow<Long?>(null)
    val noteId = _noteId.asStateFlow()

    private val _title = MutableStateFlow("")
    val title = _title.asStateFlow()

    private val _content = MutableStateFlow("")
    val content = _content.asStateFlow()

    private val _isCreating = MutableStateFlow(false)
    val isCreating = _isCreating.asStateFlow()

    val isSaving = autosaveCoordinator.isSaving

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private var isClosing = false

    init {
        viewModelScope.launch {
            autosaveCoordinator.saveError.collect { failed ->
                if (failed) {
                    _error.value = "Failed to save note"
                } else if (_error.value == "Failed to save note") {
                    _error.value = null
                }
            }
        }
    }

    fun createNote() {
        if (_isCreating.value || _noteId.value != null || isClosing) return

        _isCreating.value = true

        viewModelScope.launch {
            try {
                _error.value = null
                val now = Instant.now()

                val note = Note(
                    id = 0L,
                    title = "",
                    content = "",
                    createdAt = now,
                    updatedAt = now,
                    folderId = null,
                    isPinned = false
                )

                val id = noteRepository.createNote(note)
                loadNote(id)

            } catch (e: Exception) {
                _error.value = "Failed to create note"
            } finally {
                _isCreating.value = false
            }
        }
    }

    suspend fun loadNote(id: Long) {
        val note = noteRepository.getNoteById(id)
            ?: error("Note not found")

        autosaveCoordinator.setNote(note)

        _noteId.value = note.id
        _title.value = note.title
        _content.value = note.content
    }

    fun onTitleChanged(value: String) {
        if (_title.value == value || isClosing) return

        _title.value = value
        scheduleAutosave()
    }

    fun onContentChanged(value: String) {
        if (_content.value == value || isClosing) return

        _content.value = value
        scheduleAutosave()
    }

    private fun scheduleAutosave() {
        if (_noteId.value == null) return

        autosaveCoordinator.onEdited(
            title = _title.value,
            content = _content.value,
            scope = viewModelScope
        )
    }

    // TODO: Remove manual saving after autosave validation.
    fun saveNote() {
        if (_noteId.value == null || isClosing) return

        viewModelScope.launch {
            autosaveCoordinator.flush()
        }
    }

    fun openNote(id: Long) {
        if (
            id <= 0L ||
            _noteId.value != null ||
            _isCreating.value ||
            isClosing
        ) return

        viewModelScope.launch {
            try {
                _error.value = null
                loadNote(id)
            } catch (e: Exception) {
                _error.value = "Failed to load note"
            }
        }
    }

    // TODO: Replace temporary editor closing with Navigation Compose.
    fun closeNote() {
        if (_noteId.value == null || isClosing) return

        isClosing = true

        viewModelScope.launch {
            try {
                val saved = autosaveCoordinator.flush()

                if (saved) {
                    autosaveCoordinator.clear()

                    _noteId.value = null
                    _title.value = ""
                    _content.value = ""
                    _error.value = null
                }
                // On failure, keep the editor open with its current text.
            } finally {
                isClosing = false
            }
        }
    }
}
