package com.io.ellipse.data.repository.notes

import com.io.ellipse.data.map
import com.io.ellipse.data.network.http.rest.entity.note.request.NoteRequestBody
import com.io.ellipse.data.persistence.database.entity.note.NoteEntity
import com.io.ellipse.data.repository.notes.local.LocalNotesDataSource
import com.io.ellipse.data.repository.notes.remote.RemoteNotesDataSource
import com.io.ellipse.data.repository.notes.specification.UpdateLocalNoteSpec
import com.io.ellipse.data.repository.notes.specification.UpdateLocalNoteStateSpec
import com.io.ellipse.data.repository.notes.specification.UpdateLocalWholeNoteSpec
import com.io.ellipse.data.repository.notes.specification.UpdateRemoteNoteSpec
import com.io.ellipse.domain.repository.BaseDataSource
import com.io.ellipse.domain.repository.DeleteSpec
import com.io.ellipse.domain.repository.RetrieveSpec
import com.io.ellipse.domain.repository.UpdateSpec
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotesRepository @Inject constructor(
    private val localNotesDataSource: LocalNotesDataSource,
    private val remoteNotesDataSource: RemoteNotesDataSource
) : BaseDataSource<NoteRequestBody, NoteEntity> {

    override suspend fun create(input: NoteRequestBody): NoteEntity {
        return localNotesDataSource.create(map(remoteNotesDataSource.create(input)))
    }

    override suspend fun update(updateSpec: UpdateSpec): NoteEntity {
        return when (updateSpec) {
            is UpdateRemoteNoteSpec -> update(
                UpdateLocalWholeNoteSpec(map(remoteNotesDataSource.update(updateSpec)))
            )
            is UpdateLocalWholeNoteSpec -> localNotesDataSource.update(updateSpec)
            is UpdateLocalNoteStateSpec -> localNotesDataSource.update(updateSpec)
            is UpdateLocalNoteSpec -> localNotesDataSource.update(updateSpec)
            else -> throw NotImplementedError()
        }
    }

    override suspend fun delete(deleteSpec: DeleteSpec) {
        remoteNotesDataSource.delete(deleteSpec)
        localNotesDataSource.delete(deleteSpec)
    }

    override fun retrieve(retrieveSpec: RetrieveSpec): Flow<List<NoteEntity>> {
        return localNotesDataSource.retrieve(retrieveSpec)
            .combine(
                remoteNotesDataSource.retrieve(retrieveSpec).catch { emit(emptyList()) }
            ) { local, remote ->
                val networkItems = remote.map { map(it) }
                when {
                    local == networkItems -> {
                        local
                    }
                    remote.isEmpty() -> {
                        local
                    }
                    else -> {
                        networkItems.also { localNotesDataSource.create(it) }
                    }
                }
            }
    }
}