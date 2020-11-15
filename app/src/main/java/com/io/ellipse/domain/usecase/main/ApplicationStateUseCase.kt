package com.io.ellipse.domain.usecase.main

import com.io.ellipse.data.bluetooth.state.BluetoothStateManager
import com.io.ellipse.data.network.state.NetworkStateManager
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApplicationStateUseCase @Inject constructor(
    private val networkStateManager: NetworkStateManager,
    private val bluetoothStateManager: BluetoothStateManager
) {

    fun subscribeForApplicationState() = networkStateManager.isNetworkAvailable
        .combine(bluetoothStateManager.bluetoothState) { network, bluetooth -> network to bluetooth }
        .map { (network, bluetooth) ->
            return@map if (!network) {
                NetworkDisabledState
            } else if (!bluetooth) {
                BluetoothDisabledState
            } else {
                HardwareActiveState
            }
        }
}