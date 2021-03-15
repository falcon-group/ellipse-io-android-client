package com.io.ellipse.data.bluetooth.gatt

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import com.io.ellipse.data.bluetooth.gatt.request.ConnectionRequestProcessor
import com.io.ellipse.data.bluetooth.gatt.request.DiscoverRequestProcessor
import com.io.ellipse.data.bluetooth.gatt.request.RequestProcessor
import com.io.ellipse.data.bluetooth.gatt.request.SyncRequestProcessor
import timber.log.Timber

class BluetoothGattCallbackAggregator : BluetoothGattCallback() {

    private val _callbacks: MutableSet<BluetoothGattCallback> = mutableSetOf()

    val callbacks: Set<BluetoothGattCallback> = _callbacks

    fun process(requestProcessor: RequestProcessor) {
        Timber.e("PROCCESS $requestProcessor")
        _callbacks.add(requestProcessor)
    }

    fun removeCallback(callback: BluetoothGattCallback) = _callbacks.remove(callback)

    fun releaseExecutionCallbacks() = _callbacks.removeAll {
        (it is ConnectionRequestProcessor) or (it is SyncRequestProcessor) or (it is DiscoverRequestProcessor)
    }

    fun releaseAll() {
        _callbacks.clear()
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?
    ) {
        Timber.e("onCharacteristicChanged $_callbacks")
        _callbacks.forEach { it.onCharacteristicChanged(gatt, characteristic) }
    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        Timber.e("onCharacteristicRead $_callbacks")
        _callbacks.forEach { it.onCharacteristicRead(gatt, characteristic, status) }
    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        Timber.e("onCharacteristicWrite $_callbacks")
        _callbacks.forEach { it.onCharacteristicWrite(gatt, characteristic, status) }
    }

    override fun onDescriptorRead(
        gatt: BluetoothGatt?,
        descriptor: BluetoothGattDescriptor?,
        status: Int
    ) {
        super.onDescriptorRead(gatt, descriptor, status)
        Timber.e("onDescriptorRead $_callbacks")
        _callbacks.forEach { it.onDescriptorRead(gatt, descriptor, status) }
    }

    override fun onDescriptorWrite(
        gatt: BluetoothGatt?,
        descriptor: BluetoothGattDescriptor?,
        status: Int
    ) {
        super.onDescriptorWrite(gatt, descriptor, status)
        Timber.e("onDescriptorWrite $_callbacks")
        _callbacks.forEach { it.onDescriptorWrite(gatt, descriptor, status) }
    }

    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        Timber.e("onConnectionStateChange $_callbacks")
        _callbacks.forEach { it.onConnectionStateChange(gatt, status, newState) }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        Timber.e("onServicesDiscovered $_callbacks")
        _callbacks.forEach { it.onServicesDiscovered(gatt, status) }
    }
}