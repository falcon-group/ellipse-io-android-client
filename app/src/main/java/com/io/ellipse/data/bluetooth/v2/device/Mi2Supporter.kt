package com.io.ellipse.data.bluetooth.v2.device

import android.bluetooth.BluetoothDevice
import com.io.ellipse.data.bluetooth.connection.random
import com.io.ellipse.data.bluetooth.v2.gatt.GattClientManager
import com.io.ellipse.data.bluetooth.v2.gatt.utils.aesEncrypt
import com.io.ellipse.data.bluetooth.v2.gatt.utils.get
import com.io.ellipse.data.bluetooth.v2.gatt.utils.id
import com.io.ellipse.data.bluetooth.v2.gatt.utils.isNotificationEnabled
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Mi2Supporter @Inject constructor(private val client: GattClientManager) {

    companion object {
        private const val BASE_SERVICE: Long = 0xFEE0
        private const val EVENT_DATA_CHARACTERISTIC: Long = 0x0002
        private const val SENSOR_DATA_CHARACTERISTIC: Long = 0x0001

        private const val MAIN_SERVICE: Long = 0xFEE1
        private const val AUTH_CHARACTERISTIC: Long = 0x0009
        private const val NOTIFICATION_DESCRIPTOR: Long = 0x2902

        private const val HEART_RATE_SERVICE: Long = 0x180D
        private const val HEART_RATE_MONITOR: Long = 0x2a37
        private const val HEART_MONITOR_CONTROL: Long = 0x2a39
    }

    suspend fun connect(device: BluetoothDevice) = coroutineScope {
        client.connect(device)
        val services = client.discoverServices(device)
        val mainService = services.find {
            it.uuid.id == MAIN_SERVICE
        }
        val authCharacteristic = mainService!![AUTH_CHARACTERISTIC]!!
        val authDescriptor = authCharacteristic[NOTIFICATION_DESCRIPTOR]!!
        authDescriptor.isNotificationEnabled = true
        client.writeDescriptor(authDescriptor)
        val randomKey = random(16)
        // send phone random key
        authCharacteristic.value = byteArrayOf(0x01, 0x00) + randomKey
        client.writeNotifiedCharacteristic(authCharacteristic)
        // request device random key
        authCharacteristic.value = byteArrayOf(0x02, 0x00)
        val key = client.writeNotifiedCharacteristic(authCharacteristic)
            .data
            .takeLast(16)
            .toByteArray()
        // send encrypted key
        Timber.e("RANDOM ${randomKey.joinToString(" ")}")
        Timber.e("RETRIEVED ${key.joinToString(" ")}")
        authCharacteristic.value = byteArrayOf(0x03, 0x00) + aesEncrypt(key, randomKey)
        client.writeNotifiedCharacteristic(authCharacteristic)
        authDescriptor.isNotificationEnabled = false
        client.writeDescriptor(authDescriptor)

        // heart rate
        val heartRateService = services.find { it.uuid.id == HEART_RATE_SERVICE }
        val hrm = heartRateService!![HEART_RATE_MONITOR]!!
        val hmc = heartRateService[HEART_MONITOR_CONTROL]!!

        Timber.e("Write hmc characteristic")
        hmc.value = byteArrayOf(0x15, 0x02, 0x00)
        client.writeCharacteristic(hmc)
        hmc.value = byteArrayOf(0x15, 0x01, 0x00)
        client.writeCharacteristic(hmc)

        // events handling
        val baseService = services.findLast { it.uuid.id == BASE_SERVICE }
        val sens = baseService!![SENSOR_DATA_CHARACTERISTIC]!!

        Timber.e("Enable dwscriptors")
        client.writeDescriptor(hrm[NOTIFICATION_DESCRIPTOR]!!.also { it.isNotificationEnabled  = true})
        client.writeDescriptor(sens[NOTIFICATION_DESCRIPTOR]!!.also { it.isNotificationEnabled  = true})

        Timber.e("Write sens")
        sens.value = byteArrayOf(0x01, 0x03, 0x19)
        client.writeNotifiedCharacteristic(sens)

        Timber.e("Write hmc characteristic")
        hmc.value = byteArrayOf(0x15, 0x01, 0x01)
        client.writeCharacteristic(hmc)

        Timber.e("Write sens")
        sens.value = byteArrayOf(0x02)
        client.writeNotifiedCharacteristic(sens)

        async(start = CoroutineStart.ATOMIC) {
            while (true) {
                Timber.e("Writing hmc characteristic")
                hmc.value = byteArrayOf(0x16)
                client.writeCharacteristic(hmc)
                delay(12_000)
            }
        }

        val subscription = client.subscribe(hrm)
        subscription.data.collect { Timber.e("$it") }
    }

}