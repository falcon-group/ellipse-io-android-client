package com.io.ellipse.data.persistence.database.dao

import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import com.io.ellipse.data.persistence.database.dao.base.BaseDao
import com.io.ellipse.data.persistence.database.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotesDao : BaseDao<NoteEntity> {

    @Query("select * from notes where id like :id limit 1")
    fun retrieve(id: String): Flow<List<NoteEntity>>

    @Query("select * from notes where title like '%' || :query || '%' or content like '%' || :query || '%' limit :limit offset :offset")
    fun retrieve(query: String, limit: Int, offset: Int): Flow<List<NoteEntity>>

    @Query("select * from notes where title like '%' || :query || '%' or content like '%' || :query || '%'")
    fun retrieveSourced(query: String): PagingSource<Int, NoteEntity>

    @Query("DELETE from notes where id like :id")
    suspend fun deleteById(id: String)

    @Query("DELETE from notes")
    suspend fun deleteAll()
}