package com.io.ellipse.data.network.state.impl

import android.net.ConnectivityManager
import com.io.ellipse.data.network.state.NetworkTracker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

abstract class BaseNetworkTracker(
    protected val connectivityManager: ConnectivityManager
) : NetworkTracker {

    protected val _networkState: MutableStateFlow<Boolean> = MutableStateFlow(
        connectivityManager.activeNetworkInfo?.isAvailable ?: false
    )

    final override val isNetworkAvailable: Flow<Boolean> = _networkState
}