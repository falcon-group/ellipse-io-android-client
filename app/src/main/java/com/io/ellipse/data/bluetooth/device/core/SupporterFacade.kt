package com.io.ellipse.data.bluetooth.device.core

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattService
import com.io.ellipse.data.bluetooth.device.support.LefunSupporter
import com.io.ellipse.data.bluetooth.device.support.MiBandSupporter
import com.io.ellipse.data.bluetooth.gatt.GattClientManager
import com.io.ellipse.data.bluetooth.gatt.exceptions.NoDeviceSupportException
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.receiveAsFlow
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class SupporterFacade @Inject constructor(private val client: GattClientManager) {

    private val supporters: Map<UUID, DeviceSupporter> = mapOf(
        UUID.fromString("0000fee0-0000-1000-8000-00805f9b34fb") to MiBandSupporter(client),
        UUID.fromString("000018d0-0000-1000-8000-00805f9b34fb") to LefunSupporter(client)
    )

    private var _currentDevice = MutableStateFlow<BluetoothDevice?>(null)
    private val heartRateChannel: BroadcastChannel<Int> = BroadcastChannel(Channel.CONFLATED)

    val currentDevice: Flow<BluetoothDevice?> = _currentDevice

    val heartRate: Flow<Int> get() = heartRateChannel.openSubscription().receiveAsFlow()

    suspend fun connectAndReceive(device: BluetoothDevice, block: suspend (Int) -> Unit) {
        client.connect(device)
        val services = client.discoverServices(device)
        services.forEach { Timber.e(it.uuid.toString()) }
        val mainService = services.map { it.uuid }.find { supporters.containsKey(it) }
        val supporter = supporters[mainService] ?: throw NoDeviceSupportException()
        val secret = supporter.authorize(services)
        supporter.setup(secret, services)
        _currentDevice.value = device
        supporter.subscribe(secret, services) {
            block(it)
            heartRateChannel.send(it)
        }
    }
    fun disconnect() = client.disconnect()
}