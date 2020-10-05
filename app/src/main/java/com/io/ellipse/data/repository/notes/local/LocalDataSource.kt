package com.io.ellipse.data.repository.notes.local

import com.io.ellipse.data.persistence.database.dao.NotesDao
import com.io.ellipse.data.persistence.database.entity.NoteEntity
import com.io.ellipse.data.repository.notes.specification.*
import com.io.ellipse.domain.repository.BaseDataSource
import com.io.ellipse.domain.repository.DeleteSpec
import com.io.ellipse.domain.repository.RetrieveSpec
import com.io.ellipse.domain.repository.UpdateSpec
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocalDataSource @Inject constructor(
    private val notesDao: NotesDao
) : BaseDataSource<NoteEntity, NoteEntity> {

    suspend fun create(input: List<NoteEntity>) = notesDao.create(input)

    override suspend fun create(input: NoteEntity): NoteEntity {
        return input.also { notesDao.create(it) }
    }

    override suspend fun update(updateSpec: UpdateSpec): NoteEntity {
        return when (updateSpec) {
            is UpdateLocalNoteSpec -> updateSpec.note.also { notesDao.update(it) }
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
            else -> throw NotImplementedError()
        }
    }

}