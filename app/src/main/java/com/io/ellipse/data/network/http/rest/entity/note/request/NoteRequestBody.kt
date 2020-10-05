package com.io.ellipse.data.network.http.rest.entity.note.request

import com.google.gson.annotations.SerializedName

data class NoteRequestBody(
    @SerializedName("title") val title: String? = null,
    @SerializedName("content") val content: String? = null
)