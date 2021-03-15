package com.io.ellipse.data.network.utils

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.os.Build

@Suppress("DEPRECATION")
val Context.isConnected: Boolean
    get() {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cm.activeNetwork != null
        } else {
            cm.activeNetworkInfo != null && (cm.activeNetworkInfo?.isConnected ?: false)
        }
    }