package com.io.ellipse.domain.usecase

import com.io.ellipse.data.repository.notes.NotesRepository
import com.io.ellipse.data.repository.notes.specification.DeleteNoteSpec
import javax.inject.Inject

class DeleteNoteUseCase @Inject constructor(
    private val repository: NotesRepository
) {

    suspend fun delete(id: String) = repository.delete(DeleteNoteSpec(id))
}