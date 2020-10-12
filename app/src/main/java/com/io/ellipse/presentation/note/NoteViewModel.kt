package com.io.ellipse.presentation.note

import androidx.hilt.lifecycle.ViewModelInject
import com.io.ellipse.data.persistence.database.entity.note.NoteEntity
import com.io.ellipse.data.repository.notes.NotesRepository
import com.io.ellipse.data.repository.notes.specification.RetrieveByIdSpec
import com.io.ellipse.domain.usecase.note.CreateNoteUseCase
import com.io.ellipse.domain.usecase.note.DeleteNoteUseCase
import com.io.ellipse.domain.usecase.note.UpdateNoteUseCase
import com.io.ellipse.domain.validation.exceptions.base.EmptyFieldException
import com.io.ellipse.domain.validation.exceptions.note.IllegalFieldLengthException
import com.io.ellipse.presentation.base.BaseViewModel
import com.io.ellipse.presentation.util.BackState
import com.io.ellipse.presentation.util.Failure
import kotlinx.coroutines.flow.*

class NoteViewModel @ViewModelInject constructor(
    private val notesRepository: NotesRepository,
    private val createNoteUseCase: CreateNoteUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase
) : BaseViewModel() {

    private val _noteEntity: MutableStateFlow<NoteEntity?> = MutableStateFlow(null)

    private val _titleError: MutableStateFlow<Failure?> = MutableStateFlow(null)
    private val _contentError: MutableStateFlow<Failure?> = MutableStateFlow(null)

    val titleError: Flow<Failure?> get() = _titleError
    val contentError: Flow<Failure?> get() = _contentError

    fun retrieveItem(id: String) = notesRepository.retrieve(RetrieveByIdSpec(id))
        .map { it.first() }
        .onEach { _noteEntity.value = it }

    suspend fun commit(title: String, content: String) = proceed {
        if (title.isBlank()) {
            throw IllegalFieldLengthException()
        }
        when (val current = _noteEntity.first()) {
            null -> createNoteUseCase.create(title, content)
            else -> updateNoteUseCase.update(current.id, title, content)
        }
        _navigationState.send(BackState())
    }

    suspend fun delete() = proceed {
        deleteNoteUseCase.delete(_noteEntity.first()!!.id)
        _navigationState.send(BackState())
    }

    fun validateTitle(title: String) {
        _titleError.value = when {
            title.isBlank() -> Failure(error = EmptyFieldException())
            title.length !in 1..100 -> Failure(error = IllegalFieldLengthException())
            else -> null
        }
    }

    fun validateContent(content: String) {
        _contentError.value = when {
            content.length !in 0..1000 -> Failure(error = IllegalFieldLengthException())
            else -> null
        }
    }


}