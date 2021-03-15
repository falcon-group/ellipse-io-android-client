package com.io.ellipse.di

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.location.LocationManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
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

    @Provides
    @Singleton
    fun locationManager(@ApplicationContext context: Context): LocationManager {
        return context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

}