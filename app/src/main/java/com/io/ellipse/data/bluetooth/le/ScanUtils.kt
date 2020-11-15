package com.io.ellipse.data.bluetooth.le

import android.bluetooth.le.ScanFilter
import android.os.ParcelUuid
import java.util.*


fun filterOfUUID(uuid: UUID) = ScanFilter.Builder()
    .setServiceUuid(ParcelUuid(uuid))
    .build()
