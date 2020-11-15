package com.io.ellipse.data.bluetooth.connection

import android.bluetooth.*
import android.content.Context
import com.io.ellipse.data.bluetooth.connection.support.HeartRateMonitor
import com.io.ellipse.data.bluetooth.connection.support.mi.v2.Mi2HeartRateMonitor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

class BluetoothConnectionManager @Inject constructor(
    @ApplicationContext private val context: Context
) : BluetoothGattCallback() {

    private val monitors: List<HeartRateMonitor> = listOf(Mi2HeartRateMonitor())
    private val _currentDevice = MutableStateFlow<BluetoothDevice?>(null)
    private val _gatt = MutableStateFlow<BluetoothGatt?>(null)
    private val _currentState = MutableStateFlow<ConnectionState?>(null)

    val currentDevice: Flow<BluetoothDevice?> = _currentDevice
    val connectionState: Flow<ConnectionState> = _currentState.filterNotNull()

    suspend fun connect(device: BluetoothDevice) {
        if (_currentState.first() !is Disconnected) {
            _gatt.first()?.close()
        }
        _gatt.value = device.connectGatt(context, true, this)
    }

    suspend fun disconnect() {
        _gatt.value?.disconnect()
    }

    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        val device = gatt?.device ?: return
        when (newState) {
            BluetoothProfile.STATE_CONNECTED -> {
                _currentDevice.value = device
                _currentState.value = Connected(device)
                monitors.forEach { it.onConnected(gatt) }
            }
            BluetoothProfile.STATE_CONNECTING -> {
                _currentState.value = Connecting(device)
            }
            BluetoothProfile.STATE_DISCONNECTING -> {
                _currentState.value = Disconnecting(device)
            }
            BluetoothProfile.STATE_DISCONNECTED -> {
                monitors.forEach { it.onDisconnected(gatt) }
                _currentState.value = Disconnected(device)
            }
        }
    }

    override fun onDescriptorWrite(
        gatt: BluetoothGatt?,
        descriptor: BluetoothGattDescriptor?,
        status: Int
    ) {
        super.onDescriptorWrite(gatt, descriptor, status)
    }

    override fun onDescriptorRead(
        gatt: BluetoothGatt?,
        descriptor: BluetoothGattDescriptor?,
        status: Int
    ) {
        super.onDescriptorRead(gatt, descriptor, status)
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ) {
        super.onCharacteristicChanged(gatt, characteristic)
        monitors.forEach { it.onReceive(gatt, characteristic) }
    }

    val data: Flow<DataReceivedState> = merge(*monitors.map { it.data }.toTypedArray())
}