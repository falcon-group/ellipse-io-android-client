package com.io.ellipse.di

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import com.io.ellipse.data.bluetooth.connection.support.HeartRateMonitor
import com.io.ellipse.data.bluetooth.connection.support.mi.v2.Mi2HeartRateMonitor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.*
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
class BluetoothModule {

    companion object {
        private const val REPORT_DELAY = 1000L
    }

    @Provides
    @Singleton
    fun bluetoothAdapter(@ApplicationContext context: Context): BluetoothAdapter {
        val service = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        return service.adapter
    }

}