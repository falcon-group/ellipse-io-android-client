package com.io.ellipse.data.bluetooth.v2.gatt.request

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattService
import com.io.ellipse.data.bluetooth.v2.gatt.exceptions.GattErrorStatusException
import kotlinx.coroutines.isActive
import java.util.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class DiscoverRequestProcessor(
    device: BluetoothDevice,
    private val continuation: Continuation<List<BluetoothGattService>>
) : RequestProcessor(device.uuids?.firstOrNull()?.uuid ?: UUID.randomUUID()) {

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        super.onServicesDiscovered(gatt, status)
        if (!continuation.context.isActive) {
            return
        }
        if (status == BluetoothGatt.GATT_SUCCESS) {
            continuation.resume(gatt?.services ?: emptyList())
        } else {
            continuation.resumeWithException(GattErrorStatusException(status))
        }
    }

}