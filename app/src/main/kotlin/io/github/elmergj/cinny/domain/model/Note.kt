package io.github.elmergj.cinny.domain.model

import java.time.Instant

data class Note(
    val id: Long,
    val title: String,
    val content: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val folderId: Long?,
    val isPinned: Boolean
)
