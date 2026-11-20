package io.github.elmergj.cinny.domain.model

import java.time.Instant

data class Folder(
    val id: Long,
    val name: String,
    val createdAt: Instant,
    val updatedAt: Instant
)
