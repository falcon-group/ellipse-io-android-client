package com.io.ellipse.data.persistence.database.dao.base

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Transaction

interface CreateDao<T> {

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun create(item: T): Long

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun create(items: List<T>)
}