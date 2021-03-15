package com.io.ellipse.data.bluetooth.gatt.request

import android.bluetooth.BluetoothGattCallback
import java.util.*

abstract class RequestProcessor(
    val uuid: UUID
) : BluetoothGattCallback() {

    override fun toString(): String {
        return javaClass.simpleName + " " + uuid.toString()
    }
}