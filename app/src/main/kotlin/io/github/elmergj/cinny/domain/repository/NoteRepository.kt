package io.github.elmergj.cinny.domain.repository

import io.github.elmergj.cinny.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    suspend fun getNoteById(id: Long): Note?
    fun getAllNotes(): Flow<List<Note>>
    suspend fun createNote(note: Note): Long
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(id: Long)
}