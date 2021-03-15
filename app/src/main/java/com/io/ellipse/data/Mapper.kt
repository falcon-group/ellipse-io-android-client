package com.io.ellipse.data

import com.io.ellipse.data.network.http.rest.entity.note.response.NoteResponseBody
import com.io.ellipse.data.network.http.rest.entity.params.ParamsBody
import com.io.ellipse.data.persistence.database.entity.note.NoteEntity
import com.io.ellipse.data.persistence.database.entity.tracker.ParamsData

fun map(noteResponseBody: NoteResponseBody): NoteEntity = with(noteResponseBody) {
    NoteEntity(
        id,
        title,
        content ?: "",
        updateDate,
        createDate
    )
}

fun map(paramsData: ParamsData): ParamsBody = with(paramsData) { ParamsBody(heartRate, isUrgent) }