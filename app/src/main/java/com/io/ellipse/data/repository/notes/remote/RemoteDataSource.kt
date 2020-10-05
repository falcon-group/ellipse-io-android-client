package com.io.ellipse.data.repository.notes.remote

import com.io.ellipse.data.network.http.rest.entity.note.request.NoteRequestBody
import com.io.ellipse.data.network.http.rest.entity.note.response.NoteResponseBody
import com.io.ellipse.data.network.http.rest.services.NotesService
import com.io.ellipse.data.repository.notes.specification.DeleteNoteSpec
import com.io.ellipse.data.repository.notes.specification.PaginatedQuerySpec
import com.io.ellipse.data.repository.notes.specification.RetrieveByIdSpec
import com.io.ellipse.data.repository.notes.specification.UpdateRemoteNoteSpec
import com.io.ellipse.domain.repository.BaseDataSource
import com.io.ellipse.domain.repository.DeleteSpec
import com.io.ellipse.domain.repository.RetrieveSpec
import com.io.ellipse.domain.repository.UpdateSpec
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val notesService: NotesService
) : BaseDataSource<NoteRequestBody, NoteResponseBody> {

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
                emit(notesService.retrieve(HashMap()))
            }
            else -> throw NotImplementedError()
        }
    }


}