package com.io.ellipse.data.repository.notes.specification

import com.io.ellipse.data.network.http.rest.entity.note.request.NoteRequestBody
import com.io.ellipse.data.persistence.database.entity.NoteEntity
import com.io.ellipse.domain.repository.DeleteSpec
import com.io.ellipse.domain.repository.RetrieveSpec
import com.io.ellipse.domain.repository.UpdateSpec


data class RetrieveByIdSpec(val id: String) : RetrieveSpec()

data class PaginatedQuerySpec constructor(val query: String, val offset: Int, val limit: Int) : RetrieveSpec()

data class UpdateRemoteNoteSpec(
    val id: String,
    val noteRequestBody: NoteRequestBody
) : UpdateSpec()

data class UpdateLocalNoteSpec(val note: NoteEntity) : UpdateSpec()

data class DeleteNoteSpec(val id: String) : DeleteSpec()

object DeleteAllNotesSpec : DeleteSpec()
