package com.io.ellipse.data.bluetooth.state

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import javax.inject.Inject

class BluetoothStateManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter
) {
    companion object {
        private val INTENT_FILTER = IntentFilter(ACTION_STATE_CHANGED)
    }

    private val bluetoothReceiver: BluetoothReceiver = BluetoothReceiver()

    init {
        context.registerReceiver(bluetoothReceiver, INTENT_FILTER)
    }

    private val bluetoothStateChannel: BroadcastChannel<Boolean> = BroadcastChannel(Channel.CONFLATED)

    val isBluetoothEnabled: Boolean = bluetoothAdapter.isEnabled

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