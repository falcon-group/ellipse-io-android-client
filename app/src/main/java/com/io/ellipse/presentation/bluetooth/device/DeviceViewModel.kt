package com.io.ellipse.presentation.bluetooth.device

import androidx.activity.result.ActivityResultCallback
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.io.ellipse.data.bluetooth.gatt.BluetoothConnectionHelper
import com.io.ellipse.data.bluetooth.device.core.SupporterFacade
import com.io.ellipse.data.bluetooth.device.search.DeviceSearchManager
import com.io.ellipse.data.bluetooth.device.search.exceptions.BluetoothDisabledException
import com.io.ellipse.data.bluetooth.device.search.exceptions.LocationDisabledException
import com.io.ellipse.data.bluetooth.device.search.exceptions.PermissionDeniedException
import com.io.ellipse.presentation.base.BaseViewModel
import com.io.ellipse.presentation.bluetooth.device.action.*
import com.io.ellipse.presentation.bluetooth.device.utils.adapter.DeviceVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.*
import timber.log.Timber

class DeviceViewModel @ViewModelInject constructor(
    private val bluetoothConnectionHelper: BluetoothConnectionHelper,
    private val deviceSearchManager: DeviceSearchManager,
    private val facade: SupporterFacade
) : BaseViewModel(), ActivityResultCallback<Map<String, Boolean>> {

    private val userActionChannel: BroadcastChannel<UserAction> =
        BroadcastChannel(Channel.CONFLATED)

    val userActionState: Flow<UserAction> = userActionChannel.asFlow()

    fun startScan(): LiveData<DeviceVM> = deviceSearchManager.startSearch()
        .flowOn(Dispatchers.IO)
        .catch {
            when (it) {
                is PermissionDeniedException -> showPermissionsDialog()
                is BluetoothDisabledException -> showBluetoothDialog()
                is LocationDisabledException -> showEnableLocationDialog()
                else -> Timber.e(it)
            }
        }
        .flowOn(Dispatchers.Main)
        .combine(facade.currentDevice) { found, current ->
            DeviceVM(found, current == found)
        }.asLiveData(viewModelScope.coroutineContext)

    fun connect(deviceVm: DeviceVM) {
        bluetoothConnectionHelper.connect(deviceVm.device)
    }

    fun showDescriptionDialog() {
        userActionChannel.sendBlocking(DialogDescriptionAction)
    }

    fun showBluetoothDialog() {
        userActionChannel.sendBlocking(DialogBluetoothAction)
    }

    fun showPermissionsDialog() {
        userActionChannel.sendBlocking(DialogPermissionsAction)
    }

    fun showEnableLocationDialog() {
        userActionChannel.sendBlocking(DialogLocationAction)
    }

    override fun onCleared() {
        deviceSearchManager.stopSearch()
        super.onCleared()
    }

    override fun onActivityResult(result: Map<String, Boolean>?) {
        val isPermissionGranted = result?.values
            ?.fold(true) { prev, current -> prev and current }
            ?: false
        if (!isPermissionGranted) {
            showDescriptionDialog()
        }
    }
}