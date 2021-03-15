package com.io.ellipse.data.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppBackgroundManager @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        private const val SCHEMA_PACKAGE = "package:"
    }

    private val powerManager: PowerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    val isIgnoringBatteryOptimizations: Boolean
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            powerManager.isIgnoringBatteryOptimizations(context.packageName).also { Timber.e("ALLOWED $it") }
        } else {
            true
        }

    fun requestPermission() {
        val intent = Intent(
            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            Uri.parse(SCHEMA_PACKAGE + context.packageName)
        ).also {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}