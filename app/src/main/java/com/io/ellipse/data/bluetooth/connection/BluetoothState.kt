package com.io.ellipse.data.bluetooth.connection

import android.bluetooth.BluetoothDevice

sealed class BluetoothState

data class Connected(val device: BluetoothDevice): BluetoothState()

data class Connecting(val device: BluetoothDevice): BluetoothState()

data class Disconnected(val device: BluetoothDevice): BluetoothState()

data class Disconnecting(val device: BluetoothDevice): BluetoothState()

data class ErrorState(val device: BluetoothDevice): BluetoothState()


