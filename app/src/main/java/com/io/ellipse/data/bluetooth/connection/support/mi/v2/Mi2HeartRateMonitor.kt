package com.io.ellipse.data.bluetooth.connection.support.mi.v2

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import com.io.ellipse.data.bluetooth.connection.*
import com.io.ellipse.data.bluetooth.connection.support.HeartRateMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class Mi2HeartRateMonitor() : HeartRateMonitor {

    override val serviceDomainUUID: String = "0000%04x-0000-1000-8000-00805f9b34fb"

    override val characteristicDomainUUID: String = "0000%04x-0000-3512-2118-0009af100700"

    private object Services {
        const val BASIC = 0xfeee0
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

    private val dataChannel: BroadcastChannel<DataReceivedState> = BroadcastChannel(
        Channel.CONFLATED
    )

    private val encrypter = Cipher.getInstance("AES/ECB/NoPadding")

    private var authSteps = 0
    private var authKey: ByteArray = random(16)
    private var random: ByteArray = byteArrayOf()

    override fun onConnected(bluetoothGatt: BluetoothGatt) {
        Timber.e("SERVICE ${bluetoothGatt.services}")
        Timber.e("SERVICE ${serviceOf(Services.AUTH)}")
        val service = bluetoothGatt[serviceOf(Services.AUTH)]
        Timber.e("$service")
        val characteristic = service[characteristicOf(Characteristics.AUTH)]
        Timber.e("1 $characteristic")
        if (!bluetoothGatt.setCharacteristicNotification(characteristic, true)) {
            return
        }
        Timber.e("2 $characteristic")
        if (!bluetoothGatt.writeDescriptor(characteristic.notifyDescriptor(true))) {
            return
        }
        authSteps = authSteps enable AuthSteps.NOTIFIED
        characteristic.value = commandOf(Command.SEND_KEY, authKey)
        Timber.e("3 $characteristic")
        if (bluetoothGatt.writeCharacteristic(characteristic)) {
            authSteps = authSteps enable AuthSteps.SEND_KEY
        }
    }

    override fun onDisconnected(bluetoothGatt: BluetoothGatt) {
        authSteps = 0
    }

    override val data: Flow<DataReceivedState> = dataChannel.asFlow()

    override fun onReceive(
        bluetoothGatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ) {
        when (characteristic.uuid.toString()) {
            characteristicOf(Characteristics.AUTH) -> when {
                authSteps contains AuthSteps.SEND_ENC_RAND -> {
                    bluetoothGatt.writeDescriptor(characteristic.notifyDescriptor(false))
                    bluetoothGatt.setCharacteristicNotification(
                        bluetoothGatt.createHearRateCharacteristic(),
                        true
                    )
                    bluetoothGatt.writeCharacteristic(bluetoothGatt.createHearRateCharacteristic())
                }
                authSteps contains AuthSteps.REQUEST_RAND -> {
                    random = aesEncrypt(characteristic.value, random)
                    authSteps = authSteps enable AuthSteps.SEND_ENC_RAND
                    characteristic.value = commandOf(Command.SEND_ENCRYPTED, random)
                    bluetoothGatt.writeCharacteristic(characteristic)
                }
                authSteps contains AuthSteps.SEND_KEY -> {
                    authSteps = authSteps enable AuthSteps.REQUEST_RAND
                    characteristic.value = commandOf(Command.SEND_KEY, authKey)
                    bluetoothGatt.writeCharacteristic(characteristic)
                }
            }
            characteristicOf(Characteristics.HEART_RATE_CONTROL) -> GlobalScope.launch(Dispatchers.IO) {
                dataChannel.sendBlocking(DataReceivedState(characteristic.value))
                delay(5000)
                bluetoothGatt.writeCharacteristic(bluetoothGatt.createHearRateCharacteristic())
            }
        }
    }

    private fun serviceOf(service: Int) = String.format(serviceDomainUUID, service)

    private fun characteristicOf(characteristic: Int): String {
        return String.format(characteristicDomainUUID, characteristic)
    }

    private fun BluetoothGatt.createHearRateCharacteristic(): BluetoothGattCharacteristic {
        val service = get(serviceOf(Services.HEART_RATE))
        val characteristic = service[characteristicOf(Characteristics.HEART_RATE_CONTROL)]
        characteristic.value = commandOf(Command.HEART_START_CONTINUOUS)
        return characteristic
    }

    private fun aesEncrypt(message: ByteArray, secret: ByteArray): ByteArray {
        encrypter.init(Cipher.ENCRYPT_MODE, SecretKeySpec(secret, "AES"))
        encrypter.doFinal(message)
        return message
    }
}