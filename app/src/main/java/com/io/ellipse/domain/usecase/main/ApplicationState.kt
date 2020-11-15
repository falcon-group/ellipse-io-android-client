package com.io.ellipse.domain.usecase.main

sealed class ApplicationState

object BluetoothDisabledState: ApplicationState()

object NetworkDisabledState: ApplicationState()

object HardwareActiveState: ApplicationState()
