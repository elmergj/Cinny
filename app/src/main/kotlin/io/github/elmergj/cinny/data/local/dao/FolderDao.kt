package io.github.elmergj.cinny.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import io.github.elmergj.cinny.data.local.database.entity.FolderEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface FolderDao {

    @Query("SELECT * FROM folders WHERE id = :id")
    suspend fun getById(id: Long): FolderEntity?

    @Query("SELECT * FROM folders ORDER BY name ASC")
    fun getAll(): Flow<List<FolderEntity>>

    @Insert
    suspend fun insert(folder: FolderEntity): Long

    @Query("DELETE FROM folders WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("""
    UPDATE folders
    SET name = :name, updatedAt = :updatedAt
    WHERE id = :id AND name COLLATE BINARY != :name
    """)
    suspend fun rename(
        id: Long,
        name: String,
        updatedAt: Instant
    )
}