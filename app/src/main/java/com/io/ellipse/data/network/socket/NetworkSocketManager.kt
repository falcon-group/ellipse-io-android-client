package com.io.ellipse.data.network.socket

import com.io.ellipse.data.network.http.rest.ServiceFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkSocketManager @Inject constructor(
    private val serviceFactory: ServiceFactory
) : WebSocket, WebSocketListener() {

    private var delegatedWebSocket: WebSocket? = null

    private val _state: MutableStateFlow<NetworkSocketState> = MutableStateFlow(IdleState)

    suspend fun connect() {
        delegatedWebSocket = serviceFactory.createWebSocket(this)
    }

    val state: Flow<NetworkSocketState> = _state

    override fun cancel() {
        delegatedWebSocket?.cancel()
    }

    override fun close(code: Int, reason: String?): Boolean {
        return delegatedWebSocket?.close(code, reason) ?: false
    }

    override fun queueSize(): Long {
        return delegatedWebSocket?.queueSize() ?: 0
    }

    override fun request(): Request {
        return delegatedWebSocket!!.request()
    }

    override fun send(text: String): Boolean {
        return delegatedWebSocket?.send(text) ?: false
    }

    override fun send(bytes: ByteString): Boolean {
        return delegatedWebSocket?.send(bytes) ?: false
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        _state.value = OpenedState()
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        _state.value = EmittedState(byteString = bytes)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        _state.value = EmittedState(string = text)
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        _state.value = ClosedState()
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        _state.value = ClosedState()
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        _state.value = FailureState(t)
    }
}