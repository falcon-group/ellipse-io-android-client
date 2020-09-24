package com.io.ellipse.common.android

import android.view.View

class PreventMultiClickListener(
    private val delay: Long,
    private val listener: View.OnClickListener
) : View.OnClickListener {

    companion object {
        const val DEFAULT_DELAY = 700L
    }

    private var clickTime = 0L

    override fun onClick(v: View?) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - clickTime > delay) {
            listener.onClick(v)
        }
        clickTime = currentTime
    }
}
