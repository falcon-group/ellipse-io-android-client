package com.io.ellipse.data.bluetooth.connection

import android.bluetooth.BluetoothDevice

sealed class ConnectionState

data class Connected(val device: BluetoothDevice): ConnectionState()

data class Connecting(val device: BluetoothDevice): ConnectionState()

data class Disconnected(val device: BluetoothDevice): ConnectionState()

data class Disconnecting(val device: BluetoothDevice): ConnectionState()

data class ErrorState(val device: BluetoothDevice): ConnectionState()


