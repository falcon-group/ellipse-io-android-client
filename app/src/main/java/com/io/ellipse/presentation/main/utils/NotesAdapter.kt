package com.io.ellipse.presentation.main.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.io.ellipse.R
import com.io.ellipse.common.android.list.adapter.BasePagingRecyclerViewAdapter
import com.io.ellipse.data.persistence.database.entity.note.NoteEntity

class NotesAdapter(listener: OnNoteInteractListener) :
    BasePagingRecyclerViewAdapter<NoteEntity, NoteVH>(
        itemClickListener = listener,
        itemCallback = ITEM_CALLBACK
    ) {

    companion object {
        private val ITEM_CALLBACK = object : DiffUtil.ItemCallback<NoteEntity>() {

            override fun areItemsTheSame(oldItem: NoteEntity, newItem: NoteEntity): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: NoteEntity, newItem: NoteEntity): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteVH {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_note, parent, false)
        return NoteVH(view)
    }


}
