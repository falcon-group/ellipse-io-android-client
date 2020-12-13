package com.io.ellipse.data.network.socket

import okio.ByteString


sealed class NetworkSocketState

object IdleState: NetworkSocketState()

data class EmittedState(
    val string: String? = null,
    val byteString: ByteString? = null
) : NetworkSocketState()

class OpenedState(): NetworkSocketState()

class ClosedState(): NetworkSocketState()

data class FailureState(val cause: Throwable): NetworkSocketState()
