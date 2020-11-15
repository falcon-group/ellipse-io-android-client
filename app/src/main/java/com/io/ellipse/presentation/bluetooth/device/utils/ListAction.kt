package com.io.ellipse.presentation.bluetooth.device.utils

import com.io.ellipse.presentation.bluetooth.device.utils.adapter.DeviceVM

sealed class ListAction {
    abstract val device: DeviceVM
}

data class ActionUpsertItem(override val device: DeviceVM): ListAction()

data class ActionUpdateItem(override val device: DeviceVM): ListAction()


