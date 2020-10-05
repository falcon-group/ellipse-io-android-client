package com.io.ellipse.data.persistence.database.converters

import androidx.room.TypeConverter
import java.util.*

class DateConverters {

    @TypeConverter
    fun fromTimestamp(value: Long): Date = Date(value)

    @TypeConverter
    fun dateToTimestamp(date: Date) = date.time
}