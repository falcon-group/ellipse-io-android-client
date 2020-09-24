package com.io.ellipse.di

import com.io.ellipse.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
class SecurityModule {

    companion object {
        const val KEY_ENCODE_CIPHER = "cipher.input"
        const val KEY_DECODE_CIPHER = "cipher.output"
    }

    @Provides
    @Singleton
    fun provideMessageDigest() = MessageDigest.getInstance(BuildConfig.MESSAGE_DIGEST)

    @Provides
    @Singleton
    fun provideSecretKey(messageDigest: MessageDigest) = SecretKeySpec(
        messageDigest.digest(BuildConfig.CIPHER_KEY.toByteArray(Charsets.UTF_8)).copyOf(16),
        BuildConfig.KEY_SPEC_ALGORITM
    )

    @Provides
    @Singleton
    @Named(KEY_ENCODE_CIPHER)
    fun provideEncryptionCipher(key: SecretKeySpec) = Cipher.getInstance(BuildConfig.CIPHER_ALGORITHM).apply {
        init(Cipher.ENCRYPT_MODE, key)
    }


    @Provides
    @Singleton
    @Named(KEY_DECODE_CIPHER)
    fun provideDecryptionCipher(key: SecretKeySpec) = Cipher.getInstance(BuildConfig.CIPHER_ALGORITHM).apply {
        init(Cipher.DECRYPT_MODE, key)
    }
}