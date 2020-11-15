package com.io.ellipse.domain.usecase.note

import com.io.ellipse.data.network.http.rest.entity.note.request.NoteRequestBody
import com.io.ellipse.data.persistence.database.entity.note.FLAG_LOCALLY_UPDATED
import com.io.ellipse.data.repository.notes.NotesRepository
import com.io.ellipse.data.repository.notes.specification.UpdateLocalNoteSpec
import com.io.ellipse.data.repository.notes.specification.UpdateLocalNoteStateSpec
import com.io.ellipse.data.repository.notes.specification.UpdateRemoteNoteSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateNoteUseCase @Inject constructor(
    private val repository: NotesRepository
) {

    suspend fun update(id: String, title: String, content: String) {
        try {
            repository.update(UpdateRemoteNoteSpec(id, NoteRequestBody(title, content)))
        } catch (ex: Exception) {
            repository.update(UpdateLocalNoteSpec(id, title, content))
            repository.update(UpdateLocalNoteStateSpec(id, FLAG_LOCALLY_UPDATED))
        }
    }
}