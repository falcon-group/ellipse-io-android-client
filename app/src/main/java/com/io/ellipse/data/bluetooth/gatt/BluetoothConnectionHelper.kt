package com.io.ellipse.data.bluetooth.gatt

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.io.ellipse.service.RecieverService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothConnectionHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun connect(device: BluetoothDevice) {
        RecieverService.connect(context, device)
    }

    fun disconnect() {
        RecieverService.disconnect(context)
    }
}