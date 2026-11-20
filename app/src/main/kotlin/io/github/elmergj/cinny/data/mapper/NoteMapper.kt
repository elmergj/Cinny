package io.github.elmergj.cinny.data.mapper

import io.github.elmergj.cinny.data.local.database.entity.NoteEntity
import io.github.elmergj.cinny.domain.model.Note

fun NoteEntity.toDomain(): Note = Note(
    id = id,
    title = title,
    content = content,
    createdAt = createdAt,
    updatedAt = updatedAt,
    folderId = folderId,
    isPinned = isPinned
)

fun Note.toEntity(): NoteEntity = NoteEntity(
    id = id,
    title = title,
    content = content,
    createdAt = createdAt,
    updatedAt = updatedAt,
    folderId = folderId,
    isPinned = isPinned
)