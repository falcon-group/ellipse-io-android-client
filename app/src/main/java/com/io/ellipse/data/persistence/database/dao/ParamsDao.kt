package com.io.ellipse.data.persistence.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.io.ellipse.data.persistence.database.dao.base.BaseDao
import com.io.ellipse.data.persistence.database.entity.note.NoteEntity
import com.io.ellipse.data.persistence.database.entity.tracker.ParamsData
import kotlinx.coroutines.flow.Flow

@Dao
interface ParamsDao : BaseDao<ParamsData> {

    @Query("select * from params")
    fun retrieveAll(): Flow<List<ParamsData>>

    @Query("DELETE from params")
    suspend fun deleteAll()
}