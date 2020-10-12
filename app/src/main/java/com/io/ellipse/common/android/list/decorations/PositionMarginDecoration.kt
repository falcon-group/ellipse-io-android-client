package com.io.ellipse.common.android.list.decorations

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class PositionMarginDecoration(
    private val position: Int,
    private val marginRect: Rect
) : RecyclerView.ItemDecoration() {

    constructor(position: Int, left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0) : this(
        position,
        Rect(left, top, right, bottom)
    )

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val position = parent.getChildAdapterPosition(view)
        if (position == this.position) {
            outRect.set(marginRect)
        }
    }
}