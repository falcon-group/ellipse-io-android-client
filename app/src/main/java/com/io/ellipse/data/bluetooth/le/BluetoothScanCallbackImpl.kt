package com.io.ellipse.data.bluetooth.le

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import com.io.ellipse.data.bluetooth.device.ActionBondedDevices
import com.io.ellipse.data.bluetooth.device.ActionDeviceFound
import com.io.ellipse.data.bluetooth.device.DeviceAction
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothScanCallbackImpl @Inject constructor() : ScanCallback() {

    private val _deviceActionsChannel: BroadcastChannel<DeviceAction> = BroadcastChannel(
        Channel.CONFLATED
    )

    override fun onScanResult(callbackType: Int, result: ScanResult?) {
        val device = result?.device ?: return
        _deviceActionsChannel.sendBlocking(ActionDeviceFound(device))
    }

    override fun onScanFailed(errorCode: Int) {
        Timber.e("CODE $errorCode")
    }

    override fun onBatchScanResults(results: MutableList<ScanResult>?) {
        val devices = results?.map { it.device }?.toSet() ?: emptySet()
        _deviceActionsChannel.sendBlocking(ActionBondedDevices(devices))
    }

    fun subscribeForDevices(): Flow<DeviceAction> = _deviceActionsChannel.asFlow()
}