package com.io.ellipse.data.bluetooth.device

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import androidx.annotation.RequiresPermission
import com.io.ellipse.data.bluetooth.le.BluetoothScanCallbackImpl
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothDeviceManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter,
    private val bluetoothScanCallback: BluetoothScanCallbackImpl
) {

    private val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
    private var isScanStarted: Boolean = false

    fun subscribeForDevices(): Flow<DeviceAction> = bluetoothScanCallback.subscribeForDevices()

    @RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
    fun startScan() {
        bluetoothLeScanner.takeUnless { isScanStarted }?.startScan(bluetoothScanCallback)
        isScanStarted = true
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
    fun restartScan() {
        stopScan()
        startScan()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
    fun stopScan() {
        bluetoothLeScanner.takeIf { isScanStarted }?.stopScan(bluetoothScanCallback)
        isScanStarted = false
    }

}