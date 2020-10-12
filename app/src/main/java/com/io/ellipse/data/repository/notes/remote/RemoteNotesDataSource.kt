package com.io.ellipse.data.repository.notes.remote

import com.io.ellipse.data.network.http.rest.entity.note.request.NoteRequestBody
import com.io.ellipse.data.network.http.rest.entity.note.response.NoteResponseBody
import com.io.ellipse.data.network.http.rest.services.NotesService
import com.io.ellipse.data.repository.notes.specification.*
import com.io.ellipse.domain.repository.BaseDataSource
import com.io.ellipse.domain.repository.DeleteSpec
import com.io.ellipse.domain.repository.RetrieveSpec
import com.io.ellipse.domain.repository.UpdateSpec
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class RemoteNotesDataSource @Inject constructor(
    private val notesService: NotesService
) : BaseDataSource<NoteRequestBody, NoteResponseBody> {

    companion object {
        private const val KEY_OFFSET = "offset"
        private const val KEY_COUNT = "count"
        private const val KEY_QUERY = "query"
        private const val KEY_ORDER_BY = "orderBy"
        private const val VALUE_DESC = "desc"
    }

    override suspend fun create(input: NoteRequestBody): NoteResponseBody {
        return notesService.create(input)
    }

    override suspend fun update(updateSpec: UpdateSpec): NoteResponseBody {
        return when (updateSpec) {
            is UpdateRemoteNoteSpec -> with(updateSpec) { notesService.update(id, noteRequestBody) }
            else -> throw NotImplementedError()
        }
    }

    override suspend fun delete(deleteSpec: DeleteSpec) {
        when (deleteSpec) {
            is DeleteNoteSpec -> notesService.delete(deleteSpec.id)
            else -> throw NotImplementedError()
        }
    }

    override fun retrieve(retrieveSpec: RetrieveSpec): Flow<List<NoteResponseBody>> {
        return when (retrieveSpec) {
            is RetrieveByIdSpec -> flow {
                emit(listOf(notesService.retrieve(retrieveSpec.id)))
            }
            is PaginatedQuerySpec -> flow {
                val args = mapOf(
                    KEY_OFFSET to retrieveSpec.offset,
                    KEY_COUNT to retrieveSpec.limit,
                    KEY_ORDER_BY to VALUE_DESC,
                    KEY_QUERY to retrieveSpec.query
                )
                emit(notesService.retrieve(args))
            }
            is PaginatedSpec -> flow {
                val args = mapOf(
                    KEY_OFFSET to retrieveSpec.offset,
                    KEY_COUNT to retrieveSpec.limit,
                    KEY_ORDER_BY to VALUE_DESC
                )
                emit(notesService.retrieve(args))
            }
            else -> flowOf(emptyList())
        }
    }


}