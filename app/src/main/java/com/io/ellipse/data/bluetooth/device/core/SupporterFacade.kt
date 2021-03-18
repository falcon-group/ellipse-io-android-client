package com.io.ellipse.data.bluetooth.device.core

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.widget.Toast
import com.io.ellipse.R
import com.io.ellipse.data.bluetooth.device.support.LefunSupporter
import com.io.ellipse.data.bluetooth.device.support.MiBandSupporter
import com.io.ellipse.data.bluetooth.gatt.GattClientManager
import com.io.ellipse.data.bluetooth.gatt.exceptions.NoDeviceSupportException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class SupporterFacade @Inject constructor(
    @ApplicationContext private val context: Context,
    private val client: GattClientManager
) {

    private val supporters: Map<UUID, DeviceSupporter> = mapOf(
        UUID.fromString("0000fee0-0000-1000-8000-00805f9b34fb") to MiBandSupporter(client),
        UUID.fromString("000018d0-0000-1000-8000-00805f9b34fb") to LefunSupporter(client)
    )

    private var _currentDevice = MutableStateFlow<BluetoothDevice?>(null)
    private val _heartRate: MutableStateFlow<Int?> = MutableStateFlow(null)

    val currentDevice: Flow<BluetoothDevice?> = _currentDevice

    val heartRate: Flow<Int> = _heartRate.filterNotNull()

    suspend fun connectAndReceive(device: BluetoothDevice, block: suspend (Int) -> Unit) {
        client.connect(device)
        withContext(Dispatchers.Main) {
            val message = context.getString(R.string.title_device_connected, device.name)
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
        val services = client.discoverServices(device)
        services.forEach { Timber.e(it.uuid.toString()) }
        val mainService = services.map { it.uuid }.find { supporters.containsKey(it) }
        val supporter = supporters[mainService] ?: throw NoDeviceSupportException()
        val secret = supporter.authorize(services)
        supporter.setup(secret, services)
        _currentDevice.value = device
        supporter.subscribe(secret, services) {
            block(it)
            _heartRate.value = it
        }
    }

    fun disconnect() = client.disconnect()
}