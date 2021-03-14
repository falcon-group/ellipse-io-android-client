package com.io.ellipse.data.bluetooth.v2.gatt.exceptions

import android.bluetooth.BluetoothGatt


class GattErrorStatusException(val status: Int) : Exception() {

    companion object {
        const val DEVICE_NOT_CONNECTED = -1
    }

    override val message: String
        get() = when (status) {
            BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> "Write not permitted"
            else -> "Gatt unknown code"
        }
}