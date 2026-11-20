package io.github.elmergj.cinny.data.local.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import io.github.elmergj.cinny.data.local.database.entity.FolderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {

    @Query("SELECT * FROM folders WHERE id = :id")
    suspend fun getById(id: Long): FolderEntity?

    @Query("SELECT * FROM folders ORDER BY name ASC")
    fun getAll(): Flow<List<FolderEntity>>

    @Upsert
    suspend fun save(folder: FolderEntity)

    @Query("DELETE FROM folders WHERE id = :id")
    suspend fun deleteById(id: Long)
}