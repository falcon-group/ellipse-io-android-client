package com.io.ellipse.data.bluetooth.state

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

private const val REQUEST_ENABLE_BLUETOOTH = 10012

@RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
fun Activity.requestBluetooth() {
    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH)
}

@RequiresPermission(Manifest.permission.BLUETOOTH_ADMIN)
fun Fragment.requestBluetooth() = requireActivity().requestBluetooth()

fun Context.checkPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PERMISSION_GRANTED
}

fun Context.checkPermissions(vararg permission: String): Boolean {
    return permission.map { checkPermission(it) }.fold(true) { prev, current -> prev and current }
}

fun Fragment.checkPermissions(vararg permission: String): Boolean {
    return requireContext().checkPermissions(*permission)
}
