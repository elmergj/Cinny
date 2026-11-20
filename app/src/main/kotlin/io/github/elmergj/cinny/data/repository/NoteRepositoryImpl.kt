package io.github.elmergj.cinny.data.repository

import io.github.elmergj.cinny.data.local.dao.NoteDao
import io.github.elmergj.cinny.data.mapper.toDomain
import io.github.elmergj.cinny.data.mapper.toEntity
import io.github.elmergj.cinny.domain.model.Note
import io.github.elmergj.cinny.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao
) : NoteRepository {

    override suspend fun getNoteById(id: Long): Note? {
        return noteDao.getById(id)?.toDomain()
    }

    override fun getAllNotes(): Flow<List<Note>> {
        return noteDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun createNote(note: Note): Long =
        noteDao.insert(note.copy(id = 0L).toEntity())

    override suspend fun updateNote(note: Note) {
        require(note.id > 0L) {
            "Cannot update a note without a valid ID"
        }

        noteDao.update(note.toEntity())
    }


    override suspend fun deleteNote(id: Long) {
        noteDao.deleteById(id)
    }
}