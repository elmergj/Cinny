package io.github.elmergj.cinny.data.local.database.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "folders",
    indices = [Index(value = ["name"], unique = true)]
)
data class FolderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    val name: String,
    val createdAt: Instant,
    val updatedAt: Instant
)