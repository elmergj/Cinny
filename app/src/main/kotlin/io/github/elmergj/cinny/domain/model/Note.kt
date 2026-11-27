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
){
    init {
        require(id >= 0L) {
            "Note ID cannot be negative"
        }

        require(folderId == null || folderId > 0L) {
            "Folder ID must be positive"
        }

        require(!updatedAt.isBefore(createdAt)) {
            "Updated date cannot precede creation date"
        }
    }
}
