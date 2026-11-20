package io.github.elmergj.cinny.data.local.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import io.github.elmergj.cinny.data.local.database.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getById(id: Long): ReminderEntity?

    @Query("SELECT * FROM reminders ORDER BY scheduledAt ASC")
    fun getAll(): Flow<List<ReminderEntity>>

    @Query("""
        SELECT * FROM reminders
        WHERE title LIKE '%' || :query || '%'
        ORDER BY scheduledAt ASC
    """)
    fun search(query: String): Flow<List<ReminderEntity>>

    @Upsert
    suspend fun save(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteById(id: Long)
}