package io.github.elmergj.cinny.data.local.database

import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.ColumnTypeConverters
import io.github.elmergj.cinny.data.local.dao.FolderDao
import io.github.elmergj.cinny.data.local.dao.NoteDao
import io.github.elmergj.cinny.data.local.dao.ReminderDao
import io.github.elmergj.cinny.data.local.dao.TagDao
import io.github.elmergj.cinny.data.local.database.entity.FolderEntity
import io.github.elmergj.cinny.data.local.database.entity.NoteEntity
import io.github.elmergj.cinny.data.local.database.entity.NoteTagCrossRef
import io.github.elmergj.cinny.data.local.database.entity.ReminderEntity
import io.github.elmergj.cinny.data.local.database.entity.TagEntity

@Database(
    entities = [
        NoteEntity::class,
        FolderEntity::class,
        ReminderEntity::class,
        TagEntity::class,
        NoteTagCrossRef::class
    ],
    version = 1,
    exportSchema = true
)
@ColumnTypeConverters(Converter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao
    abstract fun reminderDao(): ReminderDao
    abstract fun tagDao(): TagDao
}