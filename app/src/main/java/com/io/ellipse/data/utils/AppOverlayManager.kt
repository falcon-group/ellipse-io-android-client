package com.io.ellipse.data.utils

import android.content.Context
import android.os.Build
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppOverlayManager @Inject constructor(@ApplicationContext private val context: Context) {

    val canDrawOverlays: Boolean
        get() = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> Settings.canDrawOverlays(context)
            else -> true
        }
}