package com.io.ellipse.data.paging.notes

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.io.ellipse.data.map
import com.io.ellipse.data.paging.INITIAL_PAGE
import com.io.ellipse.data.paging.PAGE_SIZE
import com.io.ellipse.data.persistence.database.dao.NoteRemoteKeyDao
import com.io.ellipse.data.persistence.database.entity.NoteEntity
import com.io.ellipse.data.persistence.database.entity.NoteRemoteKeyEntity
import com.io.ellipse.data.repository.notes.local.LocalDataSource
import com.io.ellipse.data.repository.notes.remote.RemoteDataSource
import com.io.ellipse.data.repository.notes.specification.DeleteAllNotesSpec
import com.io.ellipse.data.repository.notes.specification.PaginatedQuerySpec
import com.io.ellipse.data.repository.notes.specification.RetrieveByIdSpec
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

// GitHub page API is 1 based: https://developer.github.com/v3/#pagination
@OptIn(ExperimentalPagingApi::class)
class NotesNetworkMediator(
    private val query: String,
    private val notesRemoteKeysDao: NoteRemoteKeyDao,
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) : RemoteMediator<Int, NoteEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, NoteEntity>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.next?.minus(1) ?: INITIAL_PAGE
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                remoteKeys?.prev ?: return MediatorResult.Success(endOfPaginationReached = true)
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                remoteKeys?.next ?: 1
            }
        }
        Timber.e("PAGE $page")

        val spec = PaginatedQuerySpec(query, page * PAGE_SIZE, PAGE_SIZE)

        try {
            val networkItems = remoteDataSource.retrieve(spec)
                .map { items -> items.map { map(it) } }
                .first()
            val endOfPaginationReached = networkItems.isEmpty()
            // clear all tables in the database
            if (loadType == LoadType.REFRESH) {
                notesRemoteKeysDao.clearRemoteKeys()
                localDataSource.delete(DeleteAllNotesSpec)
            }
            val prevKey = page.takeIf { it > INITIAL_PAGE }?.minus(1)
            val nextKey = page.takeIf { !endOfPaginationReached }?.plus(1)
            val keys = networkItems.map {
                NoteRemoteKeyEntity(it.id, prevKey, nextKey)
            }
            localDataSource.create(networkItems)
            notesRemoteKeysDao.insertAll(keys)
            return MediatorResult.Success(endOfPaginationReached)
        } catch (exception: IOException) {
            Timber.e(exception)
            return MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            Timber.e(exception)
            return MediatorResult.Error(exception)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, NoteEntity>): NoteRemoteKeyEntity? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }
            ?.data?.lastOrNull()
            ?.let { repo -> notesRemoteKeysDao.remoteKeysRepoId(repo.id) }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, NoteEntity>): NoteRemoteKeyEntity? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }
            ?.data?.firstOrNull()
            ?.let { repo -> notesRemoteKeysDao.remoteKeysRepoId(repo.id) }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(
        state: PagingState<Int, NoteEntity>
    ): NoteRemoteKeyEntity? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)
        }?.let { repo ->
            notesRemoteKeysDao.remoteKeysRepoId(repo.id)
        }
    }

}