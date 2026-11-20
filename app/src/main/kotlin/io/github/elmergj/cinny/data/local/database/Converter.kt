package io.github.elmergj.cinny.data.local.database

import androidx.room3.ColumnTypeConverter
import io.github.elmergj.cinny.domain.model.RecurrenceType
import java.time.Instant

class Converter {

    @ColumnTypeConverter
    fun instantToLong(value: Instant?): Long? =
        value?.toEpochMilli()

    @ColumnTypeConverter
    fun longToInstant(value: Long?): Instant? =
        value?.let { Instant.ofEpochMilli(it) }

    @ColumnTypeConverter
    fun recurrenceTypeToString(value: RecurrenceType): String =
        value.name

    @ColumnTypeConverter
    fun stringToRecurrenceType(value: String): RecurrenceType =
        RecurrenceType.valueOf(value)
}