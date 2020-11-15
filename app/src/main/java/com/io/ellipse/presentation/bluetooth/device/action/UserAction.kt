package com.io.ellipse.presentation.bluetooth.device.action

sealed class UserAction

object DialogDescriptionAction: UserAction()

object DialogPermissionsAction: UserAction()

object DialogBluetoothAction: UserAction()