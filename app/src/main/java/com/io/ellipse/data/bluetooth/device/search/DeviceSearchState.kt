package com.io.ellipse.data.bluetooth.device.search

import android.bluetooth.BluetoothDevice

sealed class DeviceSearchState

data class DeviceFoundState(val device: BluetoothDevice): DeviceSearchState()

data class DeviceSearchFailure(val code: Int): DeviceSearchState() {

    companion object {
        const val PERMISSION_NOT_GRANTED = 0x001
        const val BLUETOOTH_DISABLED = 0x002
        const val BLUETOOTH_SCAN_DISABLED = 0x003
    }
}