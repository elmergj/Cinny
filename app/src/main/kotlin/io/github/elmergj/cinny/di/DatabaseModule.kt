package io.github.elmergj.cinny.di

import io.github.elmergj.cinny.data.local.dao.FolderDao
import io.github.elmergj.cinny.data.local.dao.NoteDao
import io.github.elmergj.cinny.data.local.dao.ReminderDao
import io.github.elmergj.cinny.data.local.dao.TagDao
import io.github.elmergj.cinny.data.local.database.AppDatabase
import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "cinny_database"
        )
            .setDriver(BundledSQLiteDriver())
            .build()
    }

    @Provides
    fun provideNoteDao(db: AppDatabase): NoteDao =
        db.noteDao()

    @Provides
    fun provideFolderDao(db: AppDatabase): FolderDao =
        db.folderDao()

    @Provides
    fun provideReminderDao(db: AppDatabase): ReminderDao =
        db.reminderDao()

    @Provides
    fun provideTagDao(db: AppDatabase): TagDao =
        db.tagDao()
}