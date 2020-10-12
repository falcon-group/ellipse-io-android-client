package com.io.ellipse.domain.usecase.note

import com.io.ellipse.data.persistence.database.entity.note.FLAG_LOCALLY_DELETED
import com.io.ellipse.data.repository.notes.NotesRepository
import com.io.ellipse.data.repository.notes.specification.DeleteNoteSpec
import com.io.ellipse.data.repository.notes.specification.UpdateLocalNoteStateSpec
import javax.inject.Inject

class DeleteNoteUseCase @Inject constructor(
    private val repository: NotesRepository
) {

    suspend fun delete(id: String) {
        try {
            repository.delete(DeleteNoteSpec(id))
        } catch (ex: Exception) {
            repository.update(UpdateLocalNoteStateSpec(id, FLAG_LOCALLY_DELETED))
        }
    }
}