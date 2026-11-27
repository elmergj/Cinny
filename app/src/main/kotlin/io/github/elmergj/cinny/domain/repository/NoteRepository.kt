package io.github.elmergj.cinny.domain.repository

import io.github.elmergj.cinny.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {

    suspend fun getNoteById(id: Long): Note?
    fun getAllNotes(): Flow<List<Note>>
    fun getNotesByFolderId(folderId: Long?): Flow<List<Note>>
    fun getNotesByTagId(tagId: Long): Flow<List<Note>>
    fun searchNotes(query: String): Flow<List<Note>>

    suspend fun createNote(note: Note): Long
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(id: Long)

    suspend fun setNotePinned(id: Long, isPinned: Boolean)
    suspend fun moveNote(id: Long, folderId: Long?)
}