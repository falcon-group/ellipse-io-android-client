package com.io.ellipse.data.persistence.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import com.io.ellipse.data.persistence.database.dao.base.BaseDao
import com.io.ellipse.data.persistence.database.entity.note.FLAG_LOCALLY_DELETED
import com.io.ellipse.data.persistence.database.entity.note.NoteEntity
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface NotesDao : BaseDao<NoteEntity> {

    @Query("select * from notes where id like :id limit 1")
    fun retrieve(id: String): Flow<List<NoteEntity>>

    @Query("select * from notes where title like '%' || :query || '%' or content like '%' || :query || '%' order by update_date desc limit :limit offset :offset ")
    fun retrieve(query: String, limit: Int, offset: Int): Flow<List<NoteEntity>>

    @Query("select * from notes where local_flags & :flag = :flag order by update_date desc")
    fun retrieveLocallyInteracted(flag: Int): Flow<List<NoteEntity>>

    @Query("select * from notes where (title like '%' || :query || '%' or (content like '%' || :query || '%')) and (local_flags & $FLAG_LOCALLY_DELETED != $FLAG_LOCALLY_DELETED) order by update_date desc")
    fun retrieveSourced(query: String): PagingSource<Int, NoteEntity>

    @Query("update notes set local_flags = local_flags | :flag, update_date = :date where id = :id")
    fun updateFlag(id: String, flag: Int, date: Date)

    @Query("update notes set title = :title, content = :content, update_date = :date  where id = :id")
    fun updateNoteContent(id: String, title: String, content: String, date: Date)

    @Query("DELETE from notes where id like :id")
    suspend fun deleteById(id: String)

    @Query("DELETE from notes")
    suspend fun deleteAll()


}