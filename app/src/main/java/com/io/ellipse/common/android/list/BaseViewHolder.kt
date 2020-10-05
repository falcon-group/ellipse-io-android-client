package com.io.ellipse.common.android.list

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class BaseViewHolder<T>(view: View) : RecyclerView.ViewHolder(view) {

    open fun bindView(item: T, itemClickListener: OnItemClickListener<T>?) {
        itemView.setOnClickListener {
            itemClickListener?.onItemClick(item!!, bindingAdapterPosition)
        }
    }

    open fun unbindView() {
        itemView.setOnClickListener(null)
    }
}