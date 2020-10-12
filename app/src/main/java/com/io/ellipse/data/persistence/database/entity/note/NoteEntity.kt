package com.io.ellipse.data.persistence.database.entity.note

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "notes")
data class NoteEntity constructor(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "content") val content: String,
    @ColumnInfo(name = "update_date") val updateDate: Date,
    @ColumnInfo(name = "create_date") val createDate: Date,
    @ColumnInfo(name = "local_flags") val localFlags: Int = 0
)