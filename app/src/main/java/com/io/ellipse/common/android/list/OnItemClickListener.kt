package com.io.ellipse.common.android.list

interface OnItemClickListener<T> {

    fun onItemClick(item: T, position: Int)
}