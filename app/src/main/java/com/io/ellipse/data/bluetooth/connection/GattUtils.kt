package com.io.ellipse.data.bluetooth.connection

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import java.util.*

private const val UUID_CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR =
    "00002902-0000-1000-8000-00805f9b34fb"

private val RANDOM = Random()


data class DataReceivedState(val data: ByteArray)

fun BluetoothGattCharacteristic.descriptorOf(uuid: String): BluetoothGattDescriptor {
    return descriptorOf(UUID.fromString(uuid))
}

fun BluetoothGattCharacteristic.descriptorOf(uuid: UUID): BluetoothGattDescriptor {
    return getDescriptor(uuid)
}

fun BluetoothGattCharacteristic.notifyDescriptor(isEnabled: Boolean): BluetoothGattDescriptor {
    val value = when (isEnabled) {
        true -> BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        else -> BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
    }
    return descriptorOf(UUID_CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR).also { it.value = value }
}

fun commandOf(commandKey: ByteArray, data: ByteArray = byteArrayOf()): ByteArray {
    return commandKey + data
}

fun random(size: Int): ByteArray = ByteArray(size).also { RANDOM.nextBytes(it) }

infix fun Int.enable(flag: Int) = this or flag

infix fun Int.contains(flag: Int) = this and flag == flag
