package com.io.ellipse.data.persistence.preferences.proto.auth

import androidx.datastore.Serializer
import com.io.ellipse.di.SecurityModule
import java.io.InputStream
import java.io.OutputStream
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.inject.Inject
import javax.inject.Named

class AuthModelSerializer @Inject constructor(
    @Named(SecurityModule.KEY_ENCODE_CIPHER) private val encoder: Cipher,
    @Named(SecurityModule.KEY_DECODE_CIPHER) private val decoder: Cipher
) : Serializer<AuthModel> {

    override fun readFrom(input: InputStream): AuthModel {
        return AuthModel.parseDelimitedFrom(CipherInputStream(input, encoder))
    }

    override fun writeTo(t: AuthModel, output: OutputStream) {
        t.writeDelimitedTo(CipherOutputStream(output, decoder))
    }
}