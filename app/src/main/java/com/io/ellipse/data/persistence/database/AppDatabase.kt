package com.io.ellipse.data.persistence.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.io.ellipse.data.persistence.database.converters.DateConverters
import com.io.ellipse.data.persistence.database.dao.NotesDao
import com.io.ellipse.data.persistence.database.dao.ParamsDao
import com.io.ellipse.data.persistence.database.entity.note.NoteEntity
import com.io.ellipse.data.persistence.database.entity.tracker.ParamsData

@Database(
    entities = [NoteEntity::class, ParamsData::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract val notesDao: NotesDao

    abstract val paramsDao: ParamsDao
}