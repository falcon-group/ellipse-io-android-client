package com.io.ellipse.data.persistence.database.dao.base

import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import androidx.room.Update

interface UpdateDao<T> {

    @Transaction
    @Update(onConflict = OnConflictStrategy.ABORT)
    suspend fun update(vararg data: T)
}