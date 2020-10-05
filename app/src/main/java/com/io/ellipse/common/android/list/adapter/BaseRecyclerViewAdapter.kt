package com.io.ellipse.common.android.list.adapter

import androidx.recyclerview.widget.RecyclerView
import com.io.ellipse.common.android.list.BaseViewHolder
import com.io.ellipse.common.android.list.OnItemClickListener

abstract class BaseRecyclerViewAdapter<T, VH : BaseViewHolder<T>>(
    protected var itemClickListener: OnItemClickListener<T>? = null
) : RecyclerView.Adapter<VH>() {

    companion object {
        const val NO_INDEX = -1
        const val BEGIN = 0
    }

    private val _items = mutableListOf<T>()

    var items
        set(value) {
            _items.clear()
            _items.addAll(value)
            notifyDataSetChanged()
        }
        get() = _items.toList()

    fun addItems(position: Int = _items.lastIndex, vararg items: T) = _items.run {
        val currentPosition = when (position) {
            NO_INDEX -> BEGIN
            else -> position
        }
        addAll(currentPosition, listOf(*items))
        notifyItemRangeInserted(currentPosition, items.size)
    }

    fun removeItem(position: Int) {
        _items.removeAt(position)
        notifyItemRemoved(position)
    }

    fun removeItem(item: T) {
        val position = _items.indexOf(item)
        if (position != NO_INDEX) {
            removeItem(position)
        }
    }

    fun changeItem(position: Int, item: T) {
        _items[position] = item
        notifyItemChanged(position)
    }

    override fun getItemCount() = _items.size

    override fun onBindViewHolder(p0: VH, p1: Int) = p0.bindView(_items[p1], itemClickListener)

    override fun onViewRecycled(holder: VH) = holder.unbindView()
}