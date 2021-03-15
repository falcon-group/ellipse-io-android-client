package com.io.ellipse.data.bluetooth.state

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.annotation.RequiresPermission
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val currentState: MutableStateFlow<Boolean> = MutableStateFlow(false)

    init {
        context.registerReceiver(bluetoothReceiver, INTENT_FILTER)
    }

    fun subscribeState(): Flow<Boolean> {
        if (context.checkPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            )
        ) {
            currentState.value = isBluetoothEnabled
        }
        return currentState
    }

    val isBluetoothEnabled: Boolean
        @RequiresPermission(
            allOf = [
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            ]
        ) get() = bluetoothAdapter.isEnabled

    private inner class BluetoothReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            val state = intent?.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) ?: -1
            currentState.value = when (state) {
                BluetoothAdapter.STATE_ON -> true
                else -> false
            }
        }
    }
}