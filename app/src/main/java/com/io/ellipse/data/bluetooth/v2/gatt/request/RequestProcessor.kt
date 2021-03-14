package com.io.ellipse.data.bluetooth.v2.gatt.request

import android.bluetooth.BluetoothGattCallback
import com.io.ellipse.data.bluetooth.v2.gatt.BluetoothGattCallbackAggregator
import java.util.*

abstract class RequestProcessor(
    val uuid: UUID
) : BluetoothGattCallback() {

    override fun toString(): String {
        return javaClass.simpleName + " " + uuid.toString()
    }
}