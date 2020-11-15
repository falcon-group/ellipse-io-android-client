package com.io.ellipse.data.network.state

import kotlinx.coroutines.flow.Flow

interface NetworkTracker  {

    val isNetworkAvailable: Flow<Boolean>

    fun startTracking()

    fun stopTracking()
}