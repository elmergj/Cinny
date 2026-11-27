package io.github.elmergj.cinny.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.elmergj.cinny.data.repository.FolderRepositoryImpl
import io.github.elmergj.cinny.data.repository.NoteRepositoryImpl
import io.github.elmergj.cinny.data.repository.ReminderRepositoryImpl
import io.github.elmergj.cinny.data.repository.TagRepositoryImpl
import io.github.elmergj.cinny.domain.repository.FolderRepository
import io.github.elmergj.cinny.domain.repository.NoteRepository
import io.github.elmergj.cinny.domain.repository.ReminderRepository
import io.github.elmergj.cinny.domain.repository.TagRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindNoteRepository(
        impl: NoteRepositoryImpl
    ): NoteRepository

    @Binds
    abstract fun bindFolderRepository(
        impl: FolderRepositoryImpl
    ): FolderRepository

    @Binds
    abstract fun bindReminderRepository(
        impl: ReminderRepositoryImpl
    ): ReminderRepository

    @Binds
    abstract fun bindTagRepository(
        impl: TagRepositoryImpl
    ): TagRepository
}