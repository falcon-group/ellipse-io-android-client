package com.io.ellipse.data.network.state.impl

import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import androidx.annotation.RequiresApi
import com.io.ellipse.data.network.state.impl.BaseNetworkTracker

@RequiresApi(Build.VERSION_CODES.N)
class NetworkTrackerImpl(
    connectivityManager: ConnectivityManager
) : BaseNetworkTracker(connectivityManager) {

    private val networkCallback = NetworkCallbackImpl()

    override fun startTracking() = connectivityManager.registerDefaultNetworkCallback(networkCallback)

    override fun stopTracking() = connectivityManager.unregisterNetworkCallback(networkCallback)

    private inner class NetworkCallbackImpl : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            _networkState.value = true
        }

        override fun onUnavailable() {
            _networkState.value = false
        }

        override fun onLost(network: Network) {
            _networkState.value = false
        }
    }
}