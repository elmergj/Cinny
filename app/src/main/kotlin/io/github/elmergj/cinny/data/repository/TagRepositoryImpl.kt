package io.github.elmergj.cinny.data.repository

import io.github.elmergj.cinny.data.local.dao.TagDao
import io.github.elmergj.cinny.data.local.database.entity.NoteTagCrossRef
import io.github.elmergj.cinny.data.mapper.toDomain
import io.github.elmergj.cinny.data.mapper.toEntity
import io.github.elmergj.cinny.domain.model.Tag
import io.github.elmergj.cinny.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TagRepositoryImpl @Inject constructor(
    private val tagDao: TagDao
) : TagRepository {

    override suspend fun getTagById(id: Long): Tag? {
        return tagDao.getById(id)?.toDomain()
    }

    override fun getAllTags(): Flow<List<Tag>> {
        return tagDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTagsForNote(noteId: Long): Flow<List<Tag>> {
        return tagDao.getTagsForNote(noteId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun createTag(tag: Tag): Long {
        return tagDao.insert(
            tag.copy(
                id = 0L,
                name = tag.name.trim()
            ).toEntity()
        )
    }

    override suspend fun deleteTag(id: Long) {
        tagDao.deleteById(id)
    }

    override suspend fun renameTag(id: Long, name: String) {
        val normalizedName = name.trim()

        require(normalizedName.isNotBlank()) {
            "Tag name cannot be blank"
        }

        tagDao.renameTag(id, normalizedName)
    }

    override suspend fun addTagToNote(noteId: Long, tagId: Long) {
        tagDao.addTagToNote(
            NoteTagCrossRef(
                noteId = noteId,
                tagId = tagId
            )
        )
    }

    override suspend fun removeTagFromNote(noteId: Long, tagId: Long) {
        tagDao.removeTagFromNote(noteId, tagId)
    }
}