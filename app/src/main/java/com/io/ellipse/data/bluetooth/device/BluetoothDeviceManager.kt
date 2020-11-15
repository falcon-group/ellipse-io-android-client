package com.io.ellipse.data.bluetooth.device

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import androidx.annotation.RequiresPermission
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class BluetoothDeviceManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter,
    private val deviceDiscoverHelper: DeviceDiscoverHelper
) {

    private var isScanStarted: Boolean = false

    fun subscribeForDevices(): Flow<DeviceAction> = deviceDiscoverHelper.subscribeForDevices()
        .onStart { emit(ActionBondedDevices(bluetoothAdapter.bondedDevices)) }

    @RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
    fun startScan() {
        deviceDiscoverHelper.registerItself(context)
        bluetoothAdapter.startDiscovery()
        isScanStarted = true
    }

    fun stopScan() {
        deviceDiscoverHelper.unregisterItself(context)
        bluetoothAdapter.cancelDiscovery()
        isScanStarted = false
    }

}