package com.io.ellipse.data.repository.notes

import com.io.ellipse.data.map
import com.io.ellipse.data.network.http.rest.entity.note.request.NoteRequestBody
import com.io.ellipse.data.persistence.database.entity.NoteEntity
import com.io.ellipse.data.repository.notes.local.LocalDataSource
import com.io.ellipse.data.repository.notes.remote.RemoteDataSource
import com.io.ellipse.data.repository.notes.specification.UpdateLocalNoteSpec
import com.io.ellipse.data.repository.notes.specification.UpdateRemoteNoteSpec
import com.io.ellipse.domain.repository.BaseDataSource
import com.io.ellipse.domain.repository.DeleteSpec
import com.io.ellipse.domain.repository.RetrieveSpec
import com.io.ellipse.domain.repository.UpdateSpec
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class NotesRepository @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
) : BaseDataSource<NoteRequestBody, NoteEntity> {

    override suspend fun create(input: NoteRequestBody): NoteEntity {
        return localDataSource.create(map(remoteDataSource.create(input)))
    }

    override suspend fun update(updateSpec: UpdateSpec): NoteEntity {
        return when (updateSpec) {
            is UpdateRemoteNoteSpec -> update(
                UpdateLocalNoteSpec(
                    map(
                        remoteDataSource.update(
                            updateSpec
                        )
                    )
                )
            )
            is UpdateLocalNoteSpec -> localDataSource.update(updateSpec)
            else -> throw NotImplementedError()
        }
    }

    override suspend fun delete(deleteSpec: DeleteSpec) {
        remoteDataSource.delete(deleteSpec)
        localDataSource.delete(deleteSpec)
    }

    override fun retrieve(retrieveSpec: RetrieveSpec): Flow<List<NoteEntity>> {
        return localDataSource.retrieve(retrieveSpec)
            .combine(
                remoteDataSource.retrieve(retrieveSpec).catch { emit(emptyList()) }
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
                        networkItems.also { localDataSource.create(it) }
                    }
                }
            }
    }
}