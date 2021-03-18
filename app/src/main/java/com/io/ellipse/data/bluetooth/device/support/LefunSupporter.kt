package com.io.ellipse.data.bluetooth.device.support

import android.bluetooth.BluetoothGattService
import com.io.ellipse.data.bluetooth.device.core.AbstractSupporter
import com.io.ellipse.data.bluetooth.gatt.GattClientManager
import com.io.ellipse.data.bluetooth.gatt.utils.get
import com.io.ellipse.data.bluetooth.gatt.utils.id
import com.io.ellipse.data.bluetooth.gatt.utils.isNotificationEnabled
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class LefunSupporter @Inject constructor(
    client: GattClientManager
) : AbstractSupporter(client) {

    companion object {
        private val HRCMD: ByteArray = byteArrayOf(-85, 5, 15, 1, 78)
        private const val MAIN_SERVICE: Long = 0x18D0
        private const val WRITE_CHARACTERISTIC: Long = 0x2D01
        private const val NOTIFY_CHARACTERISTIC: Long = 0x2D00
        private const val NOTIFICATION_DESCRIPTOR: Long = 0x2902
        private const val CMD_HEADER_LENGTH = 4
    }

    override suspend fun authorize(services: List<BluetoothGattService>): ByteArray? = null

    override suspend fun setup(key: ByteArray?, services: List<BluetoothGattService>) {
        val mainService = services.find { it.uuid.id == MAIN_SERVICE }
        val writeCharacteristic = mainService!![WRITE_CHARACTERISTIC]!!
        val notifyCharacteristic = mainService[NOTIFY_CHARACTERISTIC]!!
        val descriptor = notifyCharacteristic[NOTIFICATION_DESCRIPTOR]!!.also {
            it.isNotificationEnabled = true
        }
        client.writeDescriptor(descriptor)
        writeCharacteristic.value = HRCMD

        client.writeCharacteristic(writeCharacteristic)
    }

    override suspend fun subscribe(
        key: ByteArray?,
        services: List<BluetoothGattService>,
        block: suspend (Int) -> Unit
    ) {
        val mainService = services.find { it.uuid.id == MAIN_SERVICE }
        val writeCharacteristic = mainService!![WRITE_CHARACTERISTIC]!!
        val notifyCharacteristic = mainService[NOTIFY_CHARACTERISTIC]!!
        var isHandled = false
        val subscription = client.subscribe(notifyCharacteristic)
        subscription.data
            .onEach {
                isHandled = if (isHandled) {
                    // check whether package is Holistic
                    Timber.e("MESSAGE ${it.asList()}")
                    writeCharacteristic.value = HRCMD
                    client.writeCharacteristic(writeCharacteristic)
                    false
                } else {
                    true
                }
            }
            .map {
                require(it.isPackageSizeAppropriate()) { "Response is too short" }
                deserializePackage(it)
            }
            .filter {
                it > 20
            }
            .transformLatest {
                while (true) {
                    emit(it)
                    delay(2500)
                }
            }
            .collect {
                block(it)
            }
    }

    private fun deserializePackage(byteArray: ByteArray): Int {
        val offset = CMD_HEADER_LENGTH - 1
        val size = byteArray[1] - CMD_HEADER_LENGTH
        val buffer =
            ByteBuffer.wrap(byteArray, offset, size).also { it.order(ByteOrder.BIG_ENDIAN) }
        val paramsLength: Int = buffer.limit() - buffer.position()
        val deserialized = ByteArray(paramsLength)
        buffer.get(deserialized)
        return deserialized[1].toInt() and 0xff
    }

    private fun calculateChecksum(data: ByteArray, offset: Int, length: Int): Byte {
        var checksum = 0
        for (i in offset until offset + length) {
            var b = data[i].toInt()
            for (j in 0..7) {
                checksum = if ((b xor checksum) and 1 == 0) {
                    checksum shr 1
                } else {
                    checksum xor 0x18 shr 1 or 0x80
                }
                b = b shr 1
            }
        }
        return checksum.toByte()
    }

    private fun ByteArray.isPackageSizeAppropriate(): Boolean {
        return !(size < CMD_HEADER_LENGTH || size < this[1])
    }

    private fun ByteArray.isPackageHolistic(): Boolean {
        val checksum = calculateChecksum(this, 0, this[1] - 1)
        return checksum == this[this[1] - 1]
    }
}