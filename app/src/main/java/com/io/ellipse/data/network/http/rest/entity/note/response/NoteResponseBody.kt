package com.io.ellipse.data.network.http.rest.entity.note.response

import com.google.gson.annotations.SerializedName
import java.util.*

data class NoteResponseBody(
    @SerializedName("_id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("updateDate") val updateDate: Date,
    @SerializedName("createDate") val createDate: Date
)