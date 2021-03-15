package com.io.ellipse.data.bluetooth.device.core

import android.bluetooth.BluetoothGattService

interface DeviceSupporter {

    suspend fun authorize(services: List<BluetoothGattService>): ByteArray?

    suspend fun setup(key: ByteArray?, services: List<BluetoothGattService>)

    suspend fun subscribe(
        key: ByteArray?,
        services: List<BluetoothGattService>,
        block: suspend (Int) -> Unit
    )
}