package com.io.ellipse.data.bluetooth.state

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothStateManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter
) {
    companion object {
        private val INTENT_FILTER = IntentFilter(ACTION_STATE_CHANGED)
    }

    private val bluetoothReceiver: BluetoothReceiver = BluetoothReceiver()
    private val bluetoothStateChannel: BroadcastChannel<Boolean> =
        BroadcastChannel(Channel.CONFLATED)

    init {
        context.registerReceiver(bluetoothReceiver, INTENT_FILTER)
        val isPermissionsGranted = context.checkPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN
        )
        if (isPermissionsGranted) {
            bluetoothStateChannel.sendBlocking(isBluetoothEnabled)
        } else {
            bluetoothStateChannel.sendBlocking(false)
        }
    }


    val isBluetoothEnabled: Boolean get() = bluetoothAdapter.isEnabled

    val bluetoothState: Flow<Boolean> = bluetoothStateChannel.asFlow()

    private inner class BluetoothReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            val state = intent?.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) ?: -1
            when (state) {
                BluetoothAdapter.STATE_OFF -> {
                    bluetoothStateChannel.sendBlocking(false)
                }
                BluetoothAdapter.STATE_TURNING_ON -> {
                    bluetoothStateChannel.sendBlocking(false)
                }
                BluetoothAdapter.STATE_ON -> {
                    bluetoothStateChannel.sendBlocking(true)
                }
                BluetoothAdapter.STATE_TURNING_OFF -> {
                    bluetoothStateChannel.sendBlocking(false)
                }
            }
        }
    }
}