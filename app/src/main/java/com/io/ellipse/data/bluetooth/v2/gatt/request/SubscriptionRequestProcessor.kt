package com.io.ellipse.data.bluetooth.v2.gatt.request

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class SubscriptionRequestProcessor constructor(
    val characteristic: BluetoothGattCharacteristic
) : RequestProcessor(characteristic.uuid) {

    private val dataChannel: BroadcastChannel<ByteArray> = BroadcastChannel(Channel.CONFLATED)

    val data: Flow<ByteArray>
        get() = dataChannel.openSubscription().receiveAsFlow()

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?
    ) {
        if (characteristic?.uuid == uuid) {
            dataChannel.offer(characteristic.value)
        }
    }

    fun unsubscribe() {
        dataChannel.close()
    }
}