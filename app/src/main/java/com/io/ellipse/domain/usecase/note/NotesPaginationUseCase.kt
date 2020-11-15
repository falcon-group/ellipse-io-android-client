package com.io.ellipse.domain.usecase.note

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingData
import com.io.ellipse.data.paging.CONFIG
import com.io.ellipse.data.paging.INITIAL_PAGE
import com.io.ellipse.data.persistence.database.dao.NotesDao
import com.io.ellipse.data.persistence.database.entity.note.NoteEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotesPaginationUseCase @OptIn(ExperimentalPagingApi::class) @Inject constructor(
    private val notesDao: NotesDao
) {

    fun retrieveItems(query: String): Flow<PagingData<NoteEntity>> {
        return Pager(
            config = CONFIG,
            initialKey = INITIAL_PAGE,
            remoteMediator = null,
            pagingSourceFactory = { notesDao.retrieveSourced(query) }
        ).flow
    }
}