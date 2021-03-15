package com.io.ellipse.data.bluetooth.gatt.utils

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import java.math.BigInteger
import java.util.*

val UUID.id: Long
    get() = try {
        timestamp()
    } catch (ex: java.lang.Exception) {
        BigInteger(toString().split("-")[0], 16).toLong()
    }

operator fun BluetoothGatt.get(timestamp: Long): BluetoothGattService? {
    return services.find {
        try {
            it.uuid.id == timestamp
        } catch (ex: Exception) {
            false
        }
    }
}

operator fun BluetoothGattService.get(timestamp: Long): BluetoothGattCharacteristic? {
    return characteristics.find {
        try {
            it.uuid.id == timestamp
        } catch (ex: Exception) {
            false
        }
    }
}

operator fun BluetoothGattCharacteristic.get(timestamp: Long): BluetoothGattDescriptor? {
    return descriptors.find {
        try {
            it.uuid.id == timestamp
        } catch (ex: Exception) {
            false
        }
    }
}

var BluetoothGattDescriptor.isNotificationEnabled: Boolean
    set(value) {
        this.value = when (value) {
            true -> BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            else -> BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
        }
    }
    get() = this.value == BluetoothGattDescriptor.ENABLE_INDICATION_VALUE