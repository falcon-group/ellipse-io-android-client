package com.io.ellipse.domain.usecase

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingData
import com.io.ellipse.data.paging.CONFIG
import com.io.ellipse.data.paging.INITIAL_PAGE
import com.io.ellipse.data.paging.notes.NotesNetworkMediator
import com.io.ellipse.data.persistence.database.dao.NoteRemoteKeyDao
import com.io.ellipse.data.persistence.database.dao.NotesDao
import com.io.ellipse.data.persistence.database.entity.NoteEntity
import com.io.ellipse.data.repository.notes.local.LocalDataSource
import com.io.ellipse.data.repository.notes.remote.RemoteDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NotesPaginationUseCase @OptIn(ExperimentalPagingApi::class) @Inject constructor(
    private val noteRemoteKeyDao: NoteRemoteKeyDao,
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource,
    private val notesDao: NotesDao
) {

    fun retrieveItems(query: String): Flow<PagingData<NoteEntity>> {
        val mediator = NotesNetworkMediator(
            query,
            noteRemoteKeyDao,
            localDataSource,
            remoteDataSource
        )
        return Pager(
            CONFIG,
            INITIAL_PAGE,
            mediator,
            { notesDao.retrieveSourced(query) }
        ).flow
    }
}