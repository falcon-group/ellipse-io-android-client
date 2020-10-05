package com.io.ellipse.data

import com.io.ellipse.data.network.http.rest.entity.note.response.NoteResponseBody
import com.io.ellipse.data.persistence.database.entity.NoteEntity

fun map(noteResponseBody: NoteResponseBody): NoteEntity = with(noteResponseBody) {
    NoteEntity(id, title, content, updateDate, createDate)
}