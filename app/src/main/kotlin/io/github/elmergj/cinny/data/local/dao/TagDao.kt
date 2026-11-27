package io.github.elmergj.cinny.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import io.github.elmergj.cinny.data.local.database.entity.NoteTagCrossRef
import io.github.elmergj.cinny.data.local.database.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Query("SELECT * FROM tags WHERE id = :id")
    suspend fun getById(id: Long): TagEntity?

    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAll(): Flow<List<TagEntity>>

    @Insert
    suspend fun insert(tag: TagEntity): Long

    @Query("UPDATE tags SET name = :name WHERE id = :id")
    suspend fun renameTag(id: Long, name: String)

    @Query("DELETE FROM tags WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTagToNote(crossRef: NoteTagCrossRef)

    @Query("""
        DELETE FROM note_tags
        WHERE noteId = :noteId AND tagId = :tagId
    """)
    suspend fun removeTagFromNote(noteId: Long, tagId: Long)

    @Query("""
    SELECT tags.* FROM tags
    INNER JOIN note_tags ON tags.id = note_tags.tagId
    WHERE note_tags.noteId = :noteId
""")
    fun getTagsForNote(noteId: Long): Flow<List<TagEntity>>
}