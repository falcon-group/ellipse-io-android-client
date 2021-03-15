package com.io.ellipse.data.bluetooth.device.search

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import com.io.ellipse.data.bluetooth.state.checkPermissions
import com.io.ellipse.data.bluetooth.device.search.exceptions.BluetoothDisabledException
import com.io.ellipse.data.bluetooth.device.search.exceptions.LocationDisabledException
import com.io.ellipse.data.bluetooth.device.search.exceptions.PermissionDeniedException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import timber.log.Timber
import javax.inject.Inject


class DeviceSearchManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter,
    private val locationManager: LocationManager
) {
    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH
        )
    }

    private val deviceDiscoverHelper = DeviceDiscoverHelper(bluetoothAdapter)

    init {
        deviceDiscoverHelper.registerItself(context)
    }

    fun startSearch(): Flow<BluetoothDevice> = flow {
        if (!context.checkPermissions(*REQUIRED_PERMISSIONS)) {
            throw PermissionDeniedException()
        }
        if (!bluetoothAdapter.isEnabled) {
            throw BluetoothDisabledException()
        }
        if (!locationManager.isEnabled) {
            throw LocationDisabledException()
        }
        if (!bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.startDiscovery()
        }
        bluetoothAdapter.bondedDevices.forEach {
            emit(it)
        }
    }.onCompletion {
        Timber.e("$it")
        if (it == null) emitAll(deviceDiscoverHelper.subscribeForDevices())
    }

    fun stopSearch() = try {
        bluetoothAdapter.cancelDiscovery()
    } catch (ex: Exception) {
        Timber.e(ex)
    }

    fun release() {
        deviceDiscoverHelper.unregisterItself(context)
    }

    private val LocationManager.isEnabled: Boolean
       get() =  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            isLocationEnabled
        } else {
            // This was deprecated in API 28
            val mode: Int = Settings.Secure.getInt(
                context.contentResolver, Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF
            )
            mode != Settings.Secure.LOCATION_MODE_OFF
        }

}