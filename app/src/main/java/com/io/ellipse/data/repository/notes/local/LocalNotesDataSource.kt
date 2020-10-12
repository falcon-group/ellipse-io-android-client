package com.io.ellipse.data.repository.notes.local

import com.io.ellipse.data.persistence.database.dao.NotesDao
import com.io.ellipse.data.persistence.database.entity.note.NoteEntity
import com.io.ellipse.data.repository.notes.specification.*
import com.io.ellipse.domain.repository.BaseDataSource
import com.io.ellipse.domain.repository.DeleteSpec
import com.io.ellipse.domain.repository.RetrieveSpec
import com.io.ellipse.domain.repository.UpdateSpec
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.*
import javax.inject.Inject

class LocalNotesDataSource @Inject constructor(
    private val notesDao: NotesDao
) : BaseDataSource<NoteEntity, NoteEntity> {

    suspend fun create(input: List<NoteEntity>) = notesDao.create(input)

    override suspend fun create(input: NoteEntity): NoteEntity {
        return input.also { notesDao.create(it) }
    }

    override suspend fun update(updateSpec: UpdateSpec): NoteEntity {
        return when (updateSpec) {
            is UpdateLocalWholeNoteSpec -> updateSpec.note.also { notesDao.update(it) }
            is UpdateLocalNoteSpec -> with(updateSpec) {
                notesDao.updateNoteContent(id, title, content, Date())
                notesDao.retrieve(id).first().first()
            }
            is UpdateLocalNoteStateSpec -> with(updateSpec) {
                notesDao.updateFlag(id, flag, Date())
                notesDao.retrieve(id).first().first()
            }
            else -> throw NotImplementedError()
        }
    }

    override suspend fun delete(deleteSpec: DeleteSpec) {
        when (deleteSpec) {
            is DeleteNoteSpec -> notesDao.deleteById(deleteSpec.id)
            is DeleteAllNotesSpec -> notesDao.deleteAll()
            else -> throw NotImplementedError()
        }
    }

    override fun retrieve(retrieveSpec: RetrieveSpec): Flow<List<NoteEntity>> {
        return when (retrieveSpec) {
            is RetrieveByIdSpec -> notesDao.retrieve(retrieveSpec.id)
            is PaginatedQuerySpec -> notesDao.retrieve(
                retrieveSpec.query,
                retrieveSpec.limit,
                retrieveSpec.offset
            )
            is RetrieveLocallyInteractedSpec -> notesDao.retrieveLocallyInteracted(retrieveSpec.flag)
            else -> throw NotImplementedError()
        }
    }

}