package com.io.ellipse.data.bluetooth.connection.support

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import com.io.ellipse.data.bluetooth.connection.DataReceivedState
import kotlinx.coroutines.flow.Flow
import java.util.*

interface HeartRateMonitor {

    val serviceDomainUUID: String

    val characteristicDomainUUID: String

    val data: Flow<DataReceivedState>

    fun onConnected(bluetoothGatt: BluetoothGatt)

    fun onDisconnected(bluetoothGatt: BluetoothGatt)

    fun onDescriptorWrite(bluetoothGatt: BluetoothGatt, descriptor: BluetoothGattDescriptor)

    fun onCharacteristicWrite(bluetoothGatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic)

    fun onReceive(bluetoothGatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic)

    operator fun BluetoothGatt.get(uuid: String) : BluetoothGattService {
        return getService(UUID.fromString(uuid))
    }

    operator fun BluetoothGattService.get(uuid: String): BluetoothGattCharacteristic {
        return getCharacteristic(UUID.fromString(uuid))
    }
}