package com.io.ellipse.data.bluetooth.connection

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.io.ellipse.service.BluetoothReceiverService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class BluetoothConnectionHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun connect(device: BluetoothDevice) {
        BluetoothReceiverService.connect(context, device)
    }

    fun disconnect() {
        BluetoothReceiverService.disconnect(context)
    }
}