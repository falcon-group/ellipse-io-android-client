package com.io.ellipse.data.bluetooth.v2.gatt.request

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothProfile
import com.io.ellipse.data.bluetooth.v2.gatt.BluetoothGattCallbackAggregator
import com.io.ellipse.data.bluetooth.v2.gatt.exceptions.GattConnectionException
import com.io.ellipse.data.bluetooth.v2.gatt.exceptions.GattErrorStatusException
import kotlinx.coroutines.isActive
import timber.log.Timber
import java.util.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ConnectionRequestProcessor(
    private val device: BluetoothDevice,
    private val continuation: Continuation<Unit>
) : RequestProcessor(device.uuids?.firstOrNull()?.uuid ?: UUID.randomUUID()) {

    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        if (!continuation.context.isActive) {
            return
        }
        when (newState) {
            BluetoothProfile.STATE_CONNECTING -> {
                Timber.i("Connecting to ${gatt?.device}")
            }
            BluetoothProfile.STATE_CONNECTED -> {
                Timber.i("Connected to ${gatt?.device}")
                if (device == gatt?.device) when (status) {
                    BluetoothGatt.GATT_SUCCESS -> continuation.resume(Unit)
                    else -> continuation.resumeWithException(GattErrorStatusException(status))
                }
            }
            else -> if (device == gatt?.device) {
               continuation.resumeWithException(GattConnectionException())
            }
        }
    }

}