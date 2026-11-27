package io.github.elmergj.cinny.domain.model

import java.time.Instant

data class Folder(
    val id: Long,
    val name: String,
    val createdAt: Instant,
    val updatedAt: Instant
){
    init {
        require(id >= 0L) {
            "Folder ID cannot be negative"
        }

        require(name.isNotBlank()) {
            "Folder name cannot be blank"
        }

        require(!updatedAt.isBefore(createdAt)) {
            "Updated date cannot precede creation date"
        }
    }
}
