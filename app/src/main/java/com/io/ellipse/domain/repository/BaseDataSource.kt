package com.io.ellipse.domain.repository

import kotlinx.coroutines.flow.Flow

interface BaseDataSource<Input, Output> {

    suspend fun create(input: Input): Output

    suspend fun update(updateSpec: UpdateSpec) : Output

    suspend fun delete(deleteSpec: DeleteSpec)

    fun retrieve(retrieveSpec: RetrieveSpec): Flow<List<Output>>
}