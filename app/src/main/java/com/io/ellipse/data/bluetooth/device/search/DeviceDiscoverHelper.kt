package com.io.ellipse.data.bluetooth.device.search

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.asFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceDiscoverHelper @Inject constructor(
    private val bluetoothAdapter: BluetoothAdapter
) : BroadcastReceiver() {

    companion object {
        private val INTENT_FILTER = IntentFilter(BluetoothDevice.ACTION_FOUND)
    }

    private var isRegistered: Boolean = false

    private val _deviceActionsChannel: BroadcastChannel<BluetoothDevice> = BroadcastChannel(
        Channel.CONFLATED
    )

    fun subscribeForDevices() = _deviceActionsChannel.asFlow()

    fun registerItself(context: Context) {
        context.takeUnless { isRegistered }?.registerReceiver(this, INTENT_FILTER)
        isRegistered = true
    }

    fun unregisterItself(context: Context) {
        context.takeIf { isRegistered }?.unregisterReceiver(this)
        isRegistered = false
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val device: BluetoothDevice? = intent?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        device?.let { _deviceActionsChannel.sendBlocking(it) }
    }
}