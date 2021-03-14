package com.io.ellipse.data.bluetooth.v2.gatt.request

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import com.io.ellipse.data.bluetooth.v2.gatt.exceptions.GattErrorStatusException
import com.io.ellipse.data.bluetooth.v2.gatt.response.Response
import timber.log.Timber
import java.util.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

sealed class SyncRequestProcessor constructor(
    uuid: UUID,
    private val continuation: Continuation<Response>
) : RequestProcessor(uuid) {

    protected fun execute(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {
        if (characteristic.uuid == uuid) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                continuation.resume(Response(uuid, characteristic.value))
            } else {
                continuation.resumeWithException(GattErrorStatusException(status))
            }
        }
    }

    protected fun execute(
        gatt: BluetoothGatt?,
        descriptor: BluetoothGattDescriptor,
        status: Int
    ) {
        if (descriptor.uuid == uuid) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                continuation.resume(Response(uuid, descriptor.value))
            } else {
                continuation.resumeWithException(GattErrorStatusException(status))
            }
        }
    }

    class WriteCharacteristic(
        uuid: UUID,
        continuation: Continuation<Response>
    ) : SyncRequestProcessor(uuid, continuation) {

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            characteristic?.let { execute(gatt, it, status) }
        }
    }

    class NotifyCharacteristic(
        uuid: UUID,
        continuation: Continuation<Response>
    ) : SyncRequestProcessor(uuid, continuation) {

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            Timber.e("NOTIFIED CHARACTERISTIC")
            characteristic?.let { execute(gatt, it, BluetoothGatt.GATT_SUCCESS) }
        }
    }

    class WriteDescriptor(
        uuid: UUID,
        continuation: Continuation<Response>
    ) : SyncRequestProcessor(uuid, continuation) {

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            descriptor?.let { execute(gatt, it, status) }
        }
    }

    class ReadCharacteristic(
        uuid: UUID,
        continuation: Continuation<Response>
    ) : SyncRequestProcessor(uuid, continuation) {

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            characteristic?.let { execute(gatt, it, status) }
        }
    }

    class ReadDescriptor(
        uuid: UUID,
        continuation: Continuation<Response>
    ) : SyncRequestProcessor(uuid, continuation) {

        override fun onDescriptorRead(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            descriptor?.let { execute(gatt, it, status) }
        }
    }
}