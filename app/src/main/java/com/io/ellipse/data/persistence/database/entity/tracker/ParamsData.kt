package com.io.ellipse.data.persistence.database.entity.tracker

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "params")
data class ParamsData(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id")
    val id: Int = 0,
    @ColumnInfo(name = "heartRate")
    val heartRate: Int,
    @ColumnInfo(name = "isUrgent")
    val isUrgent: Boolean
)