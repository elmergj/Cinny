package io.github.elmergj.cinny.data.repository

import io.github.elmergj.cinny.data.local.dao.FolderDao
import io.github.elmergj.cinny.data.mapper.toDomain
import io.github.elmergj.cinny.data.mapper.toEntity
import io.github.elmergj.cinny.domain.model.Folder
import io.github.elmergj.cinny.domain.repository.FolderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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

    override suspend fun saveFolder(folder: Folder) {
        folderDao.save(folder.toEntity())
    }

    override suspend fun deleteFolder(id: Long) {
        folderDao.deleteById(id)
    }
}