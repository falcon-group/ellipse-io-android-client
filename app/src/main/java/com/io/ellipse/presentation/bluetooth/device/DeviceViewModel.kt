package com.io.ellipse.presentation.bluetooth.device

import androidx.activity.result.ActivityResultCallback
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import com.io.ellipse.data.bluetooth.connection.BluetoothConnectionHelper
import com.io.ellipse.data.bluetooth.connection.BluetoothConnectionManager
import com.io.ellipse.data.bluetooth.connection.BluetoothState
import com.io.ellipse.data.bluetooth.device.ActionBondedDevices
import com.io.ellipse.data.bluetooth.device.ActionDeviceFound
import com.io.ellipse.data.bluetooth.device.BluetoothDeviceManager
import com.io.ellipse.data.bluetooth.state.BluetoothStateManager
import com.io.ellipse.data.bluetooth.v2.device.Mi2Supporter
import com.io.ellipse.presentation.base.BaseViewModel
import com.io.ellipse.presentation.bluetooth.device.action.DialogBluetoothAction
import com.io.ellipse.presentation.bluetooth.device.action.DialogDescriptionAction
import com.io.ellipse.presentation.bluetooth.device.action.DialogPermissionsAction
import com.io.ellipse.presentation.bluetooth.device.action.UserAction
import com.io.ellipse.presentation.bluetooth.device.utils.ActionUpsertItem
import com.io.ellipse.presentation.bluetooth.device.utils.ListAction
import com.io.ellipse.presentation.bluetooth.device.utils.adapter.DeviceVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import timber.log.Timber

class DeviceViewModel @ViewModelInject constructor(
    private val bluetoothManager: BluetoothStateManager,
    private val deviceManager: BluetoothDeviceManager,
    private val connectManager: BluetoothConnectionManager,
    private val bluetoothConnectionHelper: BluetoothConnectionHelper,
    private val supporter: Mi2Supporter
) : BaseViewModel(), ActivityResultCallback<Map<String, Boolean>> {

    private val userActionChannel: BroadcastChannel<UserAction> =
        BroadcastChannel(Channel.CONFLATED)

    override fun onCleared() {
        stopScan()
        super.onCleared()
    }

    override fun onActivityResult(result: Map<String, Boolean>?) {
        val isPermissionGranted = result?.values
            ?.fold(true) { prev, current -> prev and current }
            ?: false
        if (isPermissionGranted) {
            userActionChannel.sendBlocking(DialogBluetoothAction)
        }
    }

    val isBluetoothEnabled: Boolean get() = bluetoothManager.isBluetoothEnabled

    val userActionState: Flow<UserAction> = userActionChannel.asFlow()

    val bluetoothState: Flow<Boolean> = bluetoothManager.bluetoothState

    val connectionState: Flow<BluetoothState> = connectManager.connectionState

    val deviceAction: Flow<ListAction> = deviceManager.subscribeForDevices()
        .combine(connectManager.currentDevice) { action, current ->
            val actions = when (action) {
                is ActionDeviceFound -> with(action) {
                    listOf(DeviceVM(device, device == current))
                }.map {
                    ActionUpsertItem(it)
                }
                is ActionBondedDevices -> action.items
                    .map { DeviceVM(it, it == current) }
                    .map { ActionUpsertItem(it) }
                else -> emptyList()
            }
            actions
        }.transform { list ->
            list.forEach { emit(it) }
        }


    fun startScan() = deviceManager.restartScan()

    fun stopScan() = deviceManager.stopScan()

    fun connect(deviceVm: DeviceVM) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                supporter.connect(deviceVm.device)
            }catch (ex: Exception) {
                Timber.e(ex)
            }
        }
//        bluetoothConnectionHelper.connect(deviceVm.device)
    }

    fun disconnect() = bluetoothConnectionHelper.disconnect()

    fun showDescriptionDialog() {
        userActionChannel.sendBlocking(DialogDescriptionAction)
    }

    fun showBluetoothDialog() {
        userActionChannel.sendBlocking(DialogBluetoothAction)
    }

    fun showPermissionsDialog() {
        userActionChannel.sendBlocking(DialogPermissionsAction)
    }
}