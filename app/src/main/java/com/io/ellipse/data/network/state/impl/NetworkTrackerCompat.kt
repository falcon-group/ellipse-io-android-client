package com.io.ellipse.data.network.state.impl

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager

class NetworkTrackerCompat(
    connectivityManager: ConnectivityManager,
    private val context: Context
) : BaseNetworkTracker(connectivityManager) {

    companion object {
        private const val ACTION_CONNECTION_CHANGED = "android.net.conn.CONNECTIVITY_CHANGE"
        private val INTENT_FILTER = IntentFilter(ACTION_CONNECTION_CHANGED)
    }

    private val networkBroadcastReceiver = NetworkBroadcastReceiver()

    override fun startTracking() = networkBroadcastReceiver.registerItself(context)

    override fun stopTracking() = networkBroadcastReceiver.unregisterItself(context)

    private inner class NetworkBroadcastReceiver : BroadcastReceiver() {

        private var isRegistered: Boolean = false

        fun registerItself(context: Context) {
            context.takeUnless { isRegistered }
                ?.also { isRegistered = true }
                ?.let { it.registerReceiver(this, INTENT_FILTER) }
        }

        fun unregisterItself(context: Context) {
            context.takeIf { isRegistered }
                ?.also { isRegistered = false }
                ?.let { it.unregisterReceiver(this) }
        }

        override fun onReceive(context: Context?, intent: Intent?) {
            _networkState.value = connectivityManager.activeNetworkInfo?.isAvailable ?: false
        }
    }
}