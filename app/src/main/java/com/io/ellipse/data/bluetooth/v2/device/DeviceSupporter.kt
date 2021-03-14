package com.io.ellipse.data.bluetooth.v2.device

import kotlinx.coroutines.flow.Flow

interface DeviceSupporter {

    val data: Flow<ByteArray>

}