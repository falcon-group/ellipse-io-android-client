package com.io.ellipse.common.android

import android.app.Service
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.CompoundButton
import android.widget.ToggleButton

private const val VIBRATE_MILLIS = 150L

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

fun Context.vibrate() {
    val vibrator: Vibrator = getSystemService(Service.VIBRATOR_SERVICE) as Vibrator
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val effect = VibrationEffect.createOneShot(VIBRATE_MILLIS, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator.vibrate(effect)
    } else {
        vibrator.vibrate(VIBRATE_MILLIS)
    }
}

fun View.vibrate() = context.vibrate()

fun ToggleButton.changeState(state: Boolean, listener: CompoundButton.OnCheckedChangeListener) {
    setOnCheckedChangeListener(null)
    isChecked = state
    setOnCheckedChangeListener(listener)
}

fun WindowManager.createDefaultParams(width: Int, height: Int): WindowManager.LayoutParams {
    var flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
    flags = flags or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
    flags = flags or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
    flags = flags or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
    flags = flags or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
    flags = flags or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
    flags = flags or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED

    val params = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        WindowManager.LayoutParams(
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            flags,
            PixelFormat.TRANSLUCENT
        )
    } else {
        WindowManager.LayoutParams(
            WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
            flags,
            PixelFormat.TRANSLUCENT
        )
    }
    params.width = width
    params.height = width
    params.gravity = Gravity.NO_GRAVITY
    return params
}