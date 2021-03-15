package com.io.ellipse.data.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppOverlayManager @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        private const val SCHEMA_PACKAGE = "package:"
    }

    val canDrawOverlays: Boolean
        get() = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> Settings.canDrawOverlays(context)
            else -> true
        }

    fun requestPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse(SCHEMA_PACKAGE + context.packageName)
        ).also {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}