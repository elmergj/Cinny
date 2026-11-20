package io.github.elmergj.cinny.domain.model

import java.time.Instant

data class Tag(
    val id: Long,
    val name: String,
    val createdAt: Instant,
)
