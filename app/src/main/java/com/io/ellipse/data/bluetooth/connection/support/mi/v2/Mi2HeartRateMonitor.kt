package com.io.ellipse.data.bluetooth.connection.support.mi.v2

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import com.io.ellipse.data.bluetooth.connection.*
import com.io.ellipse.data.bluetooth.connection.support.HeartRateMonitor
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import timber.log.Timber
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class Mi2HeartRateMonitor constructor() : HeartRateMonitor {

    override val serviceDomainUUID: String = "0000%04x-0000-1000-8000-00805f9b34fb"

    override val characteristicDomainUUID: String = "0000%04x-0000-3512-2118-0009af100700"

    private object Services {
        const val BASIC = 0xfee0
        const val AUTH = 0xfee1
        const val ALERT = 0x1802
        const val ALERT_NOTIFICATION = 0x1811
        const val HEART_RATE = 0x180d
        const val DEVICE_INFO = 0x180a
    }

    private object Characteristics {
        const val HZ = 0x0002
        const val SENSOR_CONTROL = 0x0001
        const val SENSOR_DATA = 0x0002
        const val EVENT = 0x0010
        const val AUTH = 0x0009
        const val BATTERY = 0x0006
        const val STEPS = 0x0007
        const val HEART_RATE_MEASURE = 0x2a37
        const val HEART_RATE_CONTROL = 0x2a39
        const val ALERT = 0x2a06
        const val LE_PARAMS = 0xff09
    }

    //    public final static String NOTIFICATION_DESCRIPTOR = String.format(BASE, 0x2902);
    object Command {
        val SEND_KEY = byteArrayOf(0x01, 0x00)
        val RAND_REQUEST = byteArrayOf(0x02, 0x00)
        val SEND_ENCRYPTED = byteArrayOf(0x03, 0x00)
        val HEART_STOP_CONTINUOUS = byteArrayOf(0x15, 0x01, 0x00)
        val HEART_START_CONTINUOUS = byteArrayOf(0x15, 0x01, 0x01)
        val HEART_STOP_MANUAL = byteArrayOf(0x15, 0x02, 0x00)
        val HEART_START_MANUAL = byteArrayOf(0x15, 0x02, 0x01)
        val HEART_KEEP_ALIVE = byteArrayOf(0x16)
    }

    object AuthSteps {
        const val MASK = 0b1111

        const val NOTIFIED = 0b0001
        const val SEND_KEY = 0b0010
        const val REQUEST_RAND = 0b0100
        const val SEND_ENC_RAND = 0b1000
    }

    private val dataChannel: BroadcastChannel<HeartRateData> = BroadcastChannel(
        Channel.CONFLATED
    )

    private val encrypter = Cipher.getInstance("AES/ECB/NoPadding")

    private var authSteps = 0
    private var authKey: ByteArray = random(16)
    private var random: ByteArray = byteArrayOf()
    private var keepAliveJob: Job? = null


    override fun onConnected(bluetoothGatt: BluetoothGatt) {
        val service = bluetoothGatt[serviceOf(Services.AUTH)]
        val characteristic = service[characteristicOf(Characteristics.AUTH)]
        if (!bluetoothGatt.setCharacteristicNotification(characteristic, true)) {
            return
        }
        if (!bluetoothGatt.writeDescriptor(characteristic.notifyDescriptor(true))) {
            return
        }
        authSteps = authSteps enable AuthSteps.NOTIFIED
    }

    override val data: Flow<HeartRateData> get() = dataChannel.asFlow()

    override fun onDisconnected(bluetoothGatt: BluetoothGatt) {
        keepAliveJob?.cancel()
        authSteps = 0
    }

    override fun onCharacteristicWrite(
        bluetoothGatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ) {
        when (characteristic.uuid.toString()) {
            characteristicOf(Characteristics.AUTH) -> when {
                authSteps contains AuthSteps.SEND_ENC_RAND -> {
                    onCharacteristicChanged(bluetoothGatt, characteristic)
                }
            }
            serviceOf(Characteristics.HEART_RATE_CONTROL) -> when (characteristic.value) {
                Command.HEART_STOP_MANUAL -> {
                    characteristic.value = Command.HEART_STOP_CONTINUOUS
                    val result = bluetoothGatt.writeCharacteristic(characteristic)
                }
                Command.HEART_STOP_CONTINUOUS -> {
                    characteristic.value = Command.HEART_START_CONTINUOUS
                    val result = bluetoothGatt.writeCharacteristic(characteristic)
                }
                Command.HEART_START_CONTINUOUS -> {
                    keepAliveJob = startKeepingAlive(bluetoothGatt, characteristic)
                }
            }
        }
    }

    override fun onCharacteristicChanged(
        bluetoothGatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ) {
        when (characteristic.uuid.toString()) {
            characteristicOf(Characteristics.AUTH) -> when {
                authSteps contains AuthSteps.SEND_ENC_RAND -> {
                    bluetoothGatt.writeDescriptor(characteristic.notifyDescriptor(false))
                }
                authSteps contains AuthSteps.REQUEST_RAND -> {
                    random = try {
                        val result = characteristic.value.takeLast(16).toByteArray()
                        aesEncrypt(result, authKey)
                    } catch (ex: Exception) {
                        byteArrayOf()
                    }
                    authSteps = authSteps enable AuthSteps.SEND_ENC_RAND
                    characteristic.value = commandOf(Command.SEND_ENCRYPTED, random)
                    val result = bluetoothGatt.writeCharacteristic(characteristic)
                }
                authSteps contains AuthSteps.SEND_KEY -> {
                    authSteps = authSteps enable AuthSteps.REQUEST_RAND
                    characteristic.value = commandOf(Command.RAND_REQUEST)
                    bluetoothGatt.writeCharacteristic(characteristic)
                }
            }
            serviceOf(Characteristics.HEART_RATE_MEASURE) -> {
                val data = characteristic.value
                val heartRate: Int = data[0] * 100 + data[1]
                dataChannel.sendBlocking(HeartRateData(heartRate))
            }
        }

    }

    override fun onDescriptorWrite(
        bluetoothGatt: BluetoothGatt,
        descriptor: BluetoothGattDescriptor
    ) {
        val characteristic = descriptor.characteristic
        when (characteristic.uuid.toString()) {
            characteristicOf(Characteristics.AUTH) -> if (authSteps == AuthSteps.NOTIFIED) {
                characteristic.value = commandOf(Command.SEND_KEY, authKey)
                authSteps = authSteps enable AuthSteps.SEND_KEY
                bluetoothGatt.writeCharacteristic(characteristic)
            } else if (descriptor.value === BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE) {
                val basicService = try {
                    bluetoothGatt[serviceOf(Services.BASIC)]
                } catch (ex: Exception) {
                    Timber.e(ex)
                    throw ex
                }
                val basicCharacteristic =
                    basicService[characteristicOf(Characteristics.SENSOR_DATA)]
                val eventCharacteristic = basicService[characteristicOf(Characteristics.EVENT)]
                var result = bluetoothGatt.setCharacteristicNotification(basicCharacteristic, true)
                result = bluetoothGatt.setCharacteristicNotification(eventCharacteristic, true)
                val hrservice = bluetoothGatt[serviceOf(Services.HEART_RATE)]
                val heartRateMeasure = hrservice[serviceOf(Characteristics.HEART_RATE_MEASURE)]
                result = bluetoothGatt.setCharacteristicNotification(heartRateMeasure, true)
                bluetoothGatt.writeDescriptor(heartRateMeasure.notifyDescriptor(true))
            }
            serviceOf(Characteristics.HEART_RATE_MEASURE) -> {
                val heartRateCharacteristic = bluetoothGatt.createHearRateCharacteristic()
                var result =
                    bluetoothGatt.setCharacteristicNotification(heartRateCharacteristic, true)
                heartRateCharacteristic.value = Command.HEART_STOP_MANUAL
                result = bluetoothGatt.writeCharacteristic(heartRateCharacteristic)
            }
        }
    }

    private fun serviceOf(service: Int) = String.format(serviceDomainUUID, service)

    private fun characteristicOf(characteristic: Int): String {
        return String.format(characteristicDomainUUID, characteristic)
    }

    private fun BluetoothGatt.createHearRateCharacteristic(): BluetoothGattCharacteristic {
        val service = get(serviceOf(Services.HEART_RATE))
        return service[serviceOf(Characteristics.HEART_RATE_CONTROL)]
    }

    private fun aesEncrypt(message: ByteArray, secret: ByteArray): ByteArray {
        encrypter.init(Cipher.ENCRYPT_MODE, SecretKeySpec(secret, "AES"))
        return encrypter.doFinal(message)
    }

    private fun startKeepingAlive(
        bluetoothGatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ): Job? {
        return GlobalScope.launch(Dispatchers.IO) {
            while (true) {
                characteristic.value = Command.HEART_KEEP_ALIVE
                bluetoothGatt.writeCharacteristic(characteristic)
                delay(10000)
            }
        }
    }
}