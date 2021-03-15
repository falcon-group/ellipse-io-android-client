package com.io.ellipse.data.bluetooth.state

sealed class ReceiverState

data class HeartRateState(val heartRate: Int) : ReceiverState()

data class ErrorState(val throwable: Throwable) : ReceiverState()