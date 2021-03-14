package com.io.ellipse.workers

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.io.ellipse.data.map
import com.io.ellipse.data.network.http.rest.entity.note.request.NoteRequestBody
import com.io.ellipse.data.paging.INITIAL_PAGE
import com.io.ellipse.data.persistence.database.entity.note.FLAG_LOCALLY_CREATED
import com.io.ellipse.data.persistence.database.entity.note.FLAG_LOCALLY_DELETED
import com.io.ellipse.data.persistence.database.entity.note.FLAG_LOCALLY_UPDATED
import com.io.ellipse.data.repository.notes.local.LocalNotesDataSource
import com.io.ellipse.data.repository.notes.remote.RemoteNotesDataSource
import com.io.ellipse.data.repository.notes.specification.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class SyncWorker @WorkerInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val remoteNoteNotesDataSource: RemoteNotesDataSource,
    private val localNoteNotesDataSource: LocalNotesDataSource
) : CoroutineWorker(context, workerParameters) {

    companion object {
        private const val PAGE_SIZE = 20
    }

    override suspend fun doWork(): Result {
        return try {
            syncLocalCreatedNotes()
            syncLocalUpdatedNotes()
            syncLocalDeletedNotes()
            localNoteNotesDataSource.delete(DeleteAllNotesSpec)
            saveItems(INITIAL_PAGE, PAGE_SIZE)
            Result.success()
        } catch (ex: Exception) {
            Result.failure()
        }
    }

    private suspend fun syncLocalCreatedNotes() {
        val spec = RetrieveLocallyInteractedSpec(FLAG_LOCALLY_CREATED)
        val items = localNoteNotesDataSource.retrieve(spec)
            .map { list -> list.map { NoteRequestBody(it.title, it.content) } }
            .first()
        items.onEach { remoteNoteNotesDataSource.create(it) }
    }

    private suspend fun syncLocalUpdatedNotes() {
        val spec = RetrieveLocallyInteractedSpec(FLAG_LOCALLY_UPDATED)
        val items = localNoteNotesDataSource.retrieve(spec)
            .first()
        items.filter {
            it.localFlags and FLAG_LOCALLY_CREATED != FLAG_LOCALLY_CREATED
        }.onEach {
            val body = NoteRequestBody(it.title, it.content)
            remoteNoteNotesDataSource.update(UpdateRemoteNoteSpec(it.id, body))
        }
    }

    private suspend fun syncLocalDeletedNotes() {
        val spec = RetrieveLocallyInteractedSpec(FLAG_LOCALLY_DELETED)
        val items = localNoteNotesDataSource.retrieve(spec)
            .first()
        items.onEach { remoteNoteNotesDataSource.delete(DeleteNoteSpec(it.id)) }
    }

    private suspend fun saveItems(page: Int, pageSize: Int) {
        val items = remoteNoteNotesDataSource
            .retrieve(PaginatedSpec(page, pageSize))
            .map { list -> list.map { map(it) } }
            .first()
        localNoteNotesDataSource.create(items)
        if (items.size == pageSize) {
            saveItems(page + 1, pageSize)
        } else {
            // ignore
        }
    }
}