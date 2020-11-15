package com.io.ellipse.domain.usecase.note

import com.io.ellipse.data.network.http.rest.entity.note.request.NoteRequestBody
import com.io.ellipse.data.persistence.database.entity.note.FLAG_LOCALLY_CREATED
import com.io.ellipse.data.persistence.database.entity.note.NoteEntity
import com.io.ellipse.data.repository.notes.NotesRepository
import com.io.ellipse.data.repository.notes.local.LocalNotesDataSource
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateNoteUseCase @Inject constructor(
    private val repository: NotesRepository,
    private val localNotesDataSource: LocalNotesDataSource
) {

    suspend fun create(title: String, content: String) {
        try {
            repository.create(NoteRequestBody(title, content))
        } catch (ex: Exception) {
            val id = UUID.randomUUID().toString()
            localNotesDataSource.create(
                NoteEntity(
                    id,
                    title,
                    content,
                    Date(),
                    Date(),
                    FLAG_LOCALLY_CREATED
                )
            )
        }
    }
}