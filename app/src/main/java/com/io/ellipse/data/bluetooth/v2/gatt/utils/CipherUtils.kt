package com.io.ellipse.data.bluetooth.v2.gatt.utils

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

private const val DEFAULT_TRANSFORMATION = "AES/ECB/NoPadding"
private const val ALGORITHM = "AES"

private val CIPHER = Cipher.getInstance(DEFAULT_TRANSFORMATION)
private val mutex = Mutex()

suspend fun aesEncrypt(message: ByteArray, secret: ByteArray): ByteArray = mutex.withLock(CIPHER) {
    CIPHER.init(Cipher.ENCRYPT_MODE, SecretKeySpec(secret, ALGORITHM))
    return CIPHER.doFinal(message)
}

