package com.io.ellipse.presentation.bluetooth.device.utils

import android.Manifest.permission.*
import androidx.fragment.app.Fragment
import com.io.ellipse.data.bluetooth.state.checkPermissions

fun Fragment.checkBluetoothPermissions(): Boolean = checkPermissions(
    ACCESS_FINE_LOCATION,
    BLUETOOTH,
    BLUETOOTH_ADMIN
)