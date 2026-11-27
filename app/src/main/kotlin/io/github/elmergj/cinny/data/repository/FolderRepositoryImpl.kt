package io.github.elmergj.cinny.data.repository

import io.github.elmergj.cinny.data.local.dao.FolderDao
import io.github.elmergj.cinny.data.mapper.toDomain
import io.github.elmergj.cinny.data.mapper.toEntity
import io.github.elmergj.cinny.domain.model.Folder
import io.github.elmergj.cinny.domain.repository.FolderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

class FolderRepositoryImpl @Inject constructor(
    private val folderDao: FolderDao
) : FolderRepository {

    override suspend fun getFolderById(id: Long): Folder? {
        return folderDao.getById(id)?.toDomain()
    }

    override fun getAllFolders(): Flow<List<Folder>> {
        return folderDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun createFolder(folder: Folder): Long {
        return folderDao.insert(
            folder.copy(
                id = 0L,
                name = folder.name.trim()
            ).toEntity()
        )
    }

    override suspend fun deleteFolder(id: Long) {
        folderDao.deleteById(id)
    }

    override suspend fun renameFolder(id: Long, name: String) {
        val normalizedName = name.trim()

        require(normalizedName.isNotBlank()) {
            "Folder name cannot be blank"
        }

        folderDao.rename(id, normalizedName, Instant.now())
    }
}