package com.io.ellipse.data.bluetooth.gatt

import android.bluetooth.*
import android.content.Context
import com.io.ellipse.data.bluetooth.gatt.request.ConnectionRequestProcessor
import com.io.ellipse.data.bluetooth.gatt.request.DiscoverRequestProcessor
import com.io.ellipse.data.bluetooth.gatt.request.SubscriptionRequestProcessor
import com.io.ellipse.data.bluetooth.gatt.request.SyncRequestProcessor
import com.io.ellipse.data.bluetooth.gatt.response.Response
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.Continuation

@Singleton
class GattClientManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val CONNECTION_TIMEOUT = 10_000L
        private const val WRITE_TIMEOUT = CONNECTION_TIMEOUT
        private const val READ_TIMEOUT = CONNECTION_TIMEOUT
    }

    private val callbackAggregator = BluetoothGattCallbackAggregator()

    private var gatt: BluetoothGatt? = null


    suspend fun connect(device: BluetoothDevice) {
        execute<Unit>(CONNECTION_TIMEOUT) {
            Timber.e("START CONNECTION")
            callbackAggregator.process(ConnectionRequestProcessor(device, it))
            Timber.e("AFTER PROCESS")
            gatt = device.connectGatt(context, false, callbackAggregator)
        }
    }

    suspend fun discoverServices(device: BluetoothDevice): List<BluetoothGattService> {
        return execute(CONNECTION_TIMEOUT) {
            callbackAggregator.process(DiscoverRequestProcessor(device, it))
            gatt?.discoverServices()
        }
    }

    suspend fun readCharacteristic(characteristic: BluetoothGattCharacteristic): Response {
        return execute(READ_TIMEOUT) {
            val processor = SyncRequestProcessor.ReadCharacteristic(characteristic.uuid, it)
            callbackAggregator.process(processor)
            gatt?.readCharacteristic(characteristic)
        }
    }

    suspend fun readDescriptor(descriptor: BluetoothGattDescriptor): Response {
        return execute(WRITE_TIMEOUT) {
            val processor = SyncRequestProcessor.ReadDescriptor(descriptor.uuid, it)
            callbackAggregator.process(processor)
            gatt?.readDescriptor(descriptor)
        }
    }

    suspend fun writeCharacteristic(characteristic: BluetoothGattCharacteristic): Response {
        return execute(WRITE_TIMEOUT) {
            val processor = SyncRequestProcessor.WriteCharacteristic(characteristic.uuid, it)
            callbackAggregator.process(processor)
            gatt?.writeCharacteristic(characteristic)
        }
    }

    fun enableCharacteristicNotification(characteristic: BluetoothGattCharacteristic, isEnabled: Boolean) {
        gatt?.setCharacteristicNotification(characteristic, isEnabled)
    }

    suspend fun writeNotifiedCharacteristic(characteristic: BluetoothGattCharacteristic): Response {
        return execute(WRITE_TIMEOUT) {
            enableCharacteristicNotification(characteristic, true)
            val processor = SyncRequestProcessor.NotifyCharacteristic(characteristic.uuid, it)
            callbackAggregator.process(processor)
            gatt?.writeCharacteristic(characteristic)
        }
    }

    suspend fun writeDescriptor(descriptor: BluetoothGattDescriptor): Response {
        return execute(WRITE_TIMEOUT) {
            val processor = SyncRequestProcessor.WriteDescriptor(descriptor.uuid, it)
            callbackAggregator.process(processor)
            gatt?.writeDescriptor(descriptor)
        }
    }

    fun subscribe(characteristic: BluetoothGattCharacteristic): SubscriptionRequestProcessor {
        val processor = SubscriptionRequestProcessor(characteristic)
        gatt?.setCharacteristicNotification(characteristic, true)
        callbackAggregator.process(processor)
        return processor
    }

    fun unsubscribe(subscriptionRequestProcessor: SubscriptionRequestProcessor) {
        gatt?.setCharacteristicNotification(subscriptionRequestProcessor.characteristic, false)
        callbackAggregator.removeCallback(subscriptionRequestProcessor)
        subscriptionRequestProcessor.unsubscribe()
    }

    fun disconnect() {
        gatt?.disconnect()
        callbackAggregator.releaseAll()
    }

    private suspend inline fun <T> execute(
        timeOut: Long,
        crossinline block: (Continuation<T>) -> Unit
    ): T = try {
        withTimeout(timeOut) { suspendCancellableCoroutine<T> { block(it) } }
    } finally {
        callbackAggregator.releaseExecutionCallbacks()
    }
}