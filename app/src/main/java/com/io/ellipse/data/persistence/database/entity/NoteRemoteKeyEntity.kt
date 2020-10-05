package com.io.ellipse.data.persistence.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "note_remote_keys")
data class NoteRemoteKeyEntity(
    @PrimaryKey val id: String,
    val prev: Int?,
    val next: Int?
)