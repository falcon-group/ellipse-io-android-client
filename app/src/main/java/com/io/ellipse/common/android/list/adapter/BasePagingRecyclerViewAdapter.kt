package com.io.ellipse.common.android.list.adapter

import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.io.ellipse.common.android.list.BaseViewHolder
import com.io.ellipse.common.android.list.OnItemClickListener
import com.io.ellipse.data.persistence.database.entity.NoteEntity

abstract class BasePagingRecyclerViewAdapter<T : Any, VH : BaseViewHolder<T>>(
    protected var itemClickListener: OnItemClickListener<T>? = null,
    itemCallback: DiffUtil.ItemCallback<T>
) : PagingDataAdapter<T, VH>(itemCallback) {

    override fun onBindViewHolder(holder: VH, position: Int) {
        getItem(position)?.let { holder.bindView(it, itemClickListener) }
    }

    override fun onViewRecycled(holder: VH) = holder.unbindView()
}