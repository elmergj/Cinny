package io.github.elmergj.cinny.domain.repository

import io.github.elmergj.cinny.domain.model.Folder
import kotlinx.coroutines.flow.Flow

interface FolderRepository {
    suspend fun getFolderById(id: Long): Folder?
    fun getAllFolders(): Flow<List<Folder>>
    suspend fun saveFolder(folder: Folder)
    suspend fun deleteFolder(id: Long)
}