package io.github.elmergj.cinny.data.mapper

import io.github.elmergj.cinny.data.local.database.entity.TagEntity
import io.github.elmergj.cinny.domain.model.Tag

fun TagEntity.toDomain(): Tag = Tag(
    id = id,
    name = name,
    createdAt = createdAt
)

fun Tag.toEntity(): TagEntity = TagEntity(
    id = id,
    name = name,
    createdAt = createdAt
)