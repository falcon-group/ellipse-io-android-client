package com.io.ellipse.presentation.main.utils

import android.view.View
import android.widget.Button
import android.widget.TextView
import com.io.ellipse.R
import com.io.ellipse.common.android.list.BaseViewHolder
import com.io.ellipse.common.android.list.OnItemClickListener
import com.io.ellipse.common.android.onDelayClick
import com.io.ellipse.data.persistence.database.entity.NoteEntity
import java.text.SimpleDateFormat

class NoteVH(view: View) : BaseViewHolder<NoteEntity>(view) {

    companion object {
        private const val DATE_FORMAT = "yyyy-MM-dd hh:mm:ss a"
        private val DATE_FORMATTER = SimpleDateFormat(DATE_FORMAT)
    }

    private val titleTextView: TextView = view.findViewById(R.id.text_title)
    private val contentTextView: TextView = view.findViewById(R.id.text_content)
    private val dateTextView: TextView = view.findViewById(R.id.text_creation_date)
    private val deleteNoteButton: Button = view.findViewById(R.id.button_delete)

    override fun bindView(item: NoteEntity, itemClickListener: OnItemClickListener<NoteEntity>?) {
        super.bindView(item, itemClickListener)
        titleTextView.text = item.title
        contentTextView.text = item.content
        dateTextView.text = DATE_FORMATTER.format(item.createDate)
        deleteNoteButton.onDelayClick {
            val listener = itemClickListener as? OnNoteInteractListener ?: return@onDelayClick
            listener.onItemRemove(item, bindingAdapterPosition)
        }
    }

    override fun unbindView() {
        super.unbindView()
        deleteNoteButton.setOnClickListener(null)
    }

}