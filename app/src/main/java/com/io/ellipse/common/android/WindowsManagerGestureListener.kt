package com.io.ellipse.common.android

import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.CompoundButton

class WindowsManagerGestureListener(
    private val windowsManager: WindowManager,
    private val view: CompoundButton,
    private val params: WindowManager.LayoutParams
) : GestureDetector.OnGestureListener, View.OnTouchListener {

    private val detector: GestureDetector = GestureDetector(view.context, this)
    private var dx = 0f
    private var dy = 0f

    override fun onDown(e: MotionEvent?): Boolean = true

    override fun onShowPress(e: MotionEvent?) = Unit

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        view.toggle()
        return false
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean = false

    override fun onLongPress(e: MotionEvent?) = Unit

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean = false

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                dx = params.x - event.rawX
                dy = params.y - event.rawY
                windowsManager.updateViewLayout(v, params)
            }
            MotionEvent.ACTION_MOVE -> {
                params.x = (event.rawX + dx).toInt()
                params.y = (event.rawY + dy).toInt()
                windowsManager.updateViewLayout(v, params)
            }
            MotionEvent.ACTION_UP -> {
                params.x = (event.rawX + dx).toInt()
                params.y = (event.rawY + dy).toInt()
                windowsManager.updateViewLayout(v, params)
            }
        }
        return detector.onTouchEvent(event) || true
    }
}