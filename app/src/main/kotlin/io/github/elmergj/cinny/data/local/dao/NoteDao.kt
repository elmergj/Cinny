package io.github.elmergj.cinny.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import androidx.room3.Upsert
import io.github.elmergj.cinny.data.local.database.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getById(id: Long): NoteEntity?

    @Query("SELECT * FROM notes ORDER BY isPinned DESC, updatedAt DESC")
    fun getAll(): Flow<List<NoteEntity>>

    @Query("""
        SELECT * FROM notes
        WHERE title LIKE '%' || :query || '%'
        OR content LIKE '%' || :query || '%'
        ORDER BY isPinned DESC, updatedAt DESC
    """)
    fun search(query: String): Flow<List<NoteEntity>>

    @Insert
    suspend fun insert(note: NoteEntity): Long

    @Update
    suspend fun update(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: Long)
}