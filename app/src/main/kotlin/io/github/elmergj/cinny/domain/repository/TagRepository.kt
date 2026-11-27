package io.github.elmergj.cinny.domain.repository

import io.github.elmergj.cinny.domain.model.Tag
import kotlinx.coroutines.flow.Flow

interface TagRepository {

    suspend fun getTagById(id: Long): Tag?
    fun getAllTags(): Flow<List<Tag>>
    fun getTagsForNote(noteId: Long): Flow<List<Tag>>

    suspend fun createTag(tag: Tag): Long
    suspend fun renameTag(id: Long, name: String)
    suspend fun deleteTag(id: Long)

    suspend fun addTagToNote(noteId: Long, tagId: Long)
    suspend fun removeTagFromNote(noteId: Long, tagId: Long)

}