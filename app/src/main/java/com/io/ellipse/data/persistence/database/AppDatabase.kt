package com.io.ellipse.data.persistence.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.io.ellipse.data.persistence.database.converters.DateConverters
import com.io.ellipse.data.persistence.database.dao.NoteRemoteKeyDao
import com.io.ellipse.data.persistence.database.dao.NotesDao
import com.io.ellipse.data.persistence.database.entity.NoteEntity
import com.io.ellipse.data.persistence.database.entity.NoteRemoteKeyEntity

@Database(
    entities = [NoteEntity::class, NoteRemoteKeyEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {


    abstract val notesDao: NotesDao

    abstract val noteRemoteKeyDao: NoteRemoteKeyDao
}