package com.io.ellipse.presentation.main.utils

import com.io.ellipse.common.android.list.OnItemClickListener
import com.io.ellipse.data.persistence.database.entity.note.NoteEntity

interface OnNoteInteractListener : OnItemClickListener<NoteEntity> {
    fun onItemRemove(note: NoteEntity, position: Int)
}