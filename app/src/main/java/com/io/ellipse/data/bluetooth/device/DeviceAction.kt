package com.io.ellipse.data.bluetooth.device

import android.bluetooth.BluetoothDevice

sealed class DeviceAction

data class ActionBondedDevices(val items: Set<BluetoothDevice>): DeviceAction()

data class ActionDeviceFound(val device: BluetoothDevice): DeviceAction()

data class ActionDeviceLost(val device: BluetoothDevice): DeviceAction()