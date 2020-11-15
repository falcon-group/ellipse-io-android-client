package com.io.ellipse.presentation.bluetooth.device.utils.adapter

import android.bluetooth.BluetoothDevice

data class DeviceVM(
    val device: BluetoothDevice,
    val isConnected: Boolean
) {

    val uuid: String? get() = device.uuids?.firstOrNull()?.uuid?.toString()

    val name: String? get() = device.name ?: uuid ?: address

    val address: String get() = device.address
}