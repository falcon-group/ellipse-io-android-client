package com.io.ellipse.data.persistence.preferences.proto.auth

import androidx.datastore.Serializer
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

class AuthModelSerializer @Inject constructor() : Serializer<AuthModel> {

    override fun readFrom(input: InputStream): AuthModel {
        return AuthModel.parseFrom(input)
    }

    override fun writeTo(t: AuthModel, output: OutputStream) {
        t.writeTo(output)
    }
}