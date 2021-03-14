package com.io.ellipse.data.network.socket

import com.google.gson.JsonObject
import com.io.ellipse.BuildConfig
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkSocketManager @Inject constructor() : Emitter.Listener {

    companion object {
        private val URL = BuildConfig.API_URL + "customer"
        private const val KEY_TOKEN = "token"
        private const val KEY_HEART_RATE = "heartRate"
        private const val KEY_EVENT_MONITOR = "monitor"
        private const val KEY_EVENT_CONNECTED = "monitor"

        private const val PATH = "/socket"
    }

    private var socket: Socket? = null

    private val _state: MutableStateFlow<NetworkSocketState> = MutableStateFlow(IdleState)

    suspend fun connect(token: String) {
        socket = try {
            val options = IO.Options()
                .also { it.auth = mutableMapOf(KEY_TOKEN to token) }
                .also { it.path = PATH }
            IO.socket(URL, options)
                .also { it.connect() }
                .also { it.on(KEY_EVENT_CONNECTED, this) }
                .also { _state.value = OpenedState() }
        } catch (ex: Exception) {
            _state.value = FailureState(ex)
            null
        }
    }

    val state: Flow<NetworkSocketState> = _state

    suspend fun send(heartRate: Int) {
        val message = JsonObject()
        message.addProperty(KEY_HEART_RATE, heartRate)
        socket?.emit(KEY_EVENT_MONITOR, message.toString())
    }

    suspend fun disconnect() {
        try {
            socket?.disconnect()
            socket = null
        } catch (ex: Exception) {
            _state.value = FailureState(ex)
        }
    }

    override fun call(vararg args: Any?) {
        Timber.e(args.joinToString())
    }
}