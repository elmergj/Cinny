package io.github.elmergj.cinny.data.mapper

import io.github.elmergj.cinny.data.local.database.entity.FolderEntity
import io.github.elmergj.cinny.domain.model.Folder

fun FolderEntity.toDomain(): Folder = Folder(
    id = id,
    name = name,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Folder.toEntity(): FolderEntity = FolderEntity(
    id = id,
    name = name,
    createdAt = createdAt,
    updatedAt = updatedAt
)