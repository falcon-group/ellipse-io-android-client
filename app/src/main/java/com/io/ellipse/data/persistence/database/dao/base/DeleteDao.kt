package com.io.ellipse.data.persistence.database.dao.base

import androidx.room.Delete
import androidx.room.Transaction


interface DeleteDao<T> {

    @Delete
    @Transaction
    suspend fun delete(vararg item: T)
}