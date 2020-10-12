package com.io.ellipse.data.network.http.rest.services

import com.io.ellipse.data.network.http.rest.entity.note.request.NoteRequestBody
import com.io.ellipse.data.network.http.rest.entity.note.response.NoteResponseBody
import retrofit2.http.*

@JvmSuppressWildcards
interface NotesService {

    companion object {

        private const val NOTES_ENDPOINT = "/user/notes"
    }

    @POST(NOTES_ENDPOINT)
    suspend fun create(@Body authRequestBody: NoteRequestBody): NoteResponseBody

    @GET("$NOTES_ENDPOINT/{id}")
    suspend fun retrieve(@Path("id") id: String): NoteResponseBody

    @GET(NOTES_ENDPOINT)
    suspend fun retrieve(@QueryMap map: Map<String, Any?>): List<NoteResponseBody>

    @PATCH("$NOTES_ENDPOINT/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Body authRequestBody: NoteRequestBody
    ): NoteResponseBody

    @DELETE("$NOTES_ENDPOINT/{id}")
    suspend fun delete(@Path("id") id: String)
}