package com.io.ellipse.common.android.list

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class GridItemMarginDecoration(
    private val marginRect: Rect,
    private val spanCount: Int
) : RecyclerView.ItemDecoration() {

    private val outlineRect = Rect()

    constructor(horizontalMargin: Int, verticalMargins: Int, columns: Int) : this(Rect().also {
        it.top = verticalMargins / 2
        it.left = horizontalMargin / 2
        it.right = horizontalMargin / 2
        it.bottom = verticalMargins / 2
    }, columns)

    constructor(
        margin: Int,
        columns: Int,
        outlineRect: Rect = Rect()
    ) : this(margin, margin, columns) {
        this.outlineRect.set(outlineRect)
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val position = parent.getChildAdapterPosition(view)
        with(outRect.also { it.set(marginRect) }) {
            if (isFirstLine(position)) {
                top = outlineRect.top
            }
            if (isLastLine(position, parent.adapter!!.itemCount - 1)) {
                bottom = outlineRect.bottom
            }
            if (isFirstColumn(position)) {
                left = outlineRect.left
            }
            if (isLastColumn(position)) {
                right = outlineRect.right
            }
        }
    }

    private fun isFirstLine(position: Int) = position < spanCount

    private fun isLastLine(position: Int, lastIndex: Int): Boolean {
        val from = (lastIndex / spanCount) * spanCount
        val to = from + spanCount
        return position in from..to
    }

    private fun isFirstColumn(position: Int) = position % spanCount == 0

    private fun isLastColumn(position: Int) = (position + 1) % spanCount == 0
}