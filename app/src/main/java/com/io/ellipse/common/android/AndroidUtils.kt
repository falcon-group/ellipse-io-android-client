package com.io.ellipse.common.android

import android.view.View

internal fun View.onDelayClick(
    delay: Long = PreventMultiClickListener.DEFAULT_DELAY,
    listener: View.OnClickListener
) {
    setOnClickListener(PreventMultiClickListener(delay, listener))
}

internal inline fun View.onDelayClick(
    delay: Long = PreventMultiClickListener.DEFAULT_DELAY,
    crossinline listener: (View?) -> Unit
) {
    onDelayClick(delay, View.OnClickListener { listener(it) })
}