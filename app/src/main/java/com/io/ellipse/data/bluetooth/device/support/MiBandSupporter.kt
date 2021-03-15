package com.io.ellipse.data.bluetooth.device.support

import android.bluetooth.BluetoothGattService
import com.io.ellipse.data.bluetooth.device.core.AbstractSupporter
import com.io.ellipse.data.bluetooth.gatt.GattClientManager
import com.io.ellipse.data.bluetooth.gatt.utils.aesEncrypt
import com.io.ellipse.data.bluetooth.gatt.utils.get
import com.io.ellipse.data.bluetooth.gatt.utils.id
import com.io.ellipse.data.bluetooth.gatt.utils.isNotificationEnabled
import com.io.ellipse.data.utils.random
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MiBandSupporter @Inject constructor(client: GattClientManager) : AbstractSupporter(client) {

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

    override suspend fun authorize(services: List<BluetoothGattService>): ByteArray? {
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
        val authKey = aesEncrypt(key, randomKey)
        authCharacteristic.value = byteArrayOf(0x03, 0x00) + authKey
        client.writeNotifiedCharacteristic(authCharacteristic)
        authDescriptor.isNotificationEnabled = false
        client.writeDescriptor(authDescriptor)

        return authKey
    }

    override suspend fun setup(key: ByteArray?, services: List<BluetoothGattService>) {
        val heartRateService = services.find { it.uuid.id == HEART_RATE_SERVICE }
        val hrm = heartRateService!![HEART_RATE_MONITOR]!!
        val hmc = heartRateService[HEART_MONITOR_CONTROL]!!

        hmc.value = byteArrayOf(0x15, 0x02, 0x00)
        client.writeCharacteristic(hmc)
        hmc.value = byteArrayOf(0x15, 0x01, 0x00)
        client.writeCharacteristic(hmc)

        // events handling
        val baseService = services.findLast { it.uuid.id == BASE_SERVICE }
        val sens = baseService!![SENSOR_DATA_CHARACTERISTIC]!!

        client.writeDescriptor(hrm[NOTIFICATION_DESCRIPTOR]!!.also {
            it.isNotificationEnabled = true
        })
        client.writeDescriptor(sens[NOTIFICATION_DESCRIPTOR]!!.also {
            it.isNotificationEnabled = true
        })

        sens.value = byteArrayOf(0x01, 0x03, 0x19)
        client.writeNotifiedCharacteristic(sens)

        hmc.value = byteArrayOf(0x15, 0x01, 0x01)
        client.writeCharacteristic(hmc)

        sens.value = byteArrayOf(0x02)
        client.writeNotifiedCharacteristic(sens)

        GlobalScope.launch(Dispatchers.Default + currentCoroutineContext()) {
            while (true) {
                delay(12_000)
                hmc.value = byteArrayOf(0x16)
                client.writeCharacteristic(hmc)
            }
        }
        Unit
    }

    override suspend fun subscribe(
        key: ByteArray?,
        services: List<BluetoothGattService>,
        block: suspend (Int) -> Unit
    ) {
        val heartRateService = services.find { it.uuid.id == HEART_RATE_SERVICE }
        val hrm = heartRateService!![HEART_RATE_MONITOR]!!
        val subscription = client.subscribe(hrm)
        subscription.data.collect {
            val heartRate: Int = it[0] * 100 + it[1]
            block(heartRate)
        }
    }

}