package io.github.elmergj.cinny.data.local.database.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import java.time.Instant

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val createdAt: Instant
)