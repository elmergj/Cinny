package io.github.elmergj.cinny.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import io.github.elmergj.cinny.data.local.database.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getById(id: Long): NoteEntity?

    @Query("SELECT * FROM notes ORDER BY isPinned DESC, updatedAt DESC, id DESC")
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

    @Query("""
    SELECT * FROM notes
    WHERE folderId IS :folderId
    ORDER BY isPinned DESC, updatedAt DESC
    """)
    fun getByFolderId(folderId: Long?): Flow<List<NoteEntity>>

    @Query("""
    SELECT notes.* FROM notes
    INNER JOIN note_tags ON notes.id = note_tags.noteId
    WHERE note_tags.tagId = :tagId
    ORDER BY isPinned DESC, updatedAt DESC
    """)
    fun getByTagId(tagId: Long): Flow<List<NoteEntity>>

    @Query("UPDATE notes SET isPinned = :isPinned WHERE id = :id")
    suspend fun setPinned(id: Long, isPinned: Boolean)

    @Query("UPDATE notes SET folderId = :folderId WHERE id = :id")
    suspend fun moveToFolder(id: Long, folderId: Long?)
}