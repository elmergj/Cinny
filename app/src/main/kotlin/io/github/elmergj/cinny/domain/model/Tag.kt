package io.github.elmergj.cinny.domain.model

import java.time.Instant

data class Tag(
    val id: Long,
    val name: String,
    val createdAt: Instant,
){
    init {
        require(id >= 0L) {
            "Tag ID cannot be negative"
        }

        require(name.isNotBlank()) {
            "Tag name cannot be blank"
        }
    }
}
