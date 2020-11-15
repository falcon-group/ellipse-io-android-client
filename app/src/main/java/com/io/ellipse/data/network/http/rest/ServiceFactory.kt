package com.io.ellipse.data.network.http.rest

import com.io.ellipse.BuildConfig
import com.io.ellipse.data.network.http.JSON_SERIALIZER
import com.io.ellipse.data.network.http.rest.auth.AuthInterceptor
import com.io.ellipse.data.network.http.rest.auth.AuthenticatorImpl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceFactory @Inject constructor(
    private val authInterceptor: AuthInterceptor
) {

    companion object {
        private const val CONNECTION_TIMEOUT = 60L
        private const val READ_TIMEOUT = CONNECTION_TIMEOUT
        private const val WRITE_TIMEOUT = CONNECTION_TIMEOUT
    }

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .authenticator(AuthenticatorImpl(this))
            .addInterceptor(authInterceptor)
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.MINUTES)
            .readTimeout(READ_TIMEOUT, TimeUnit.MINUTES)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.MINUTES)
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create(JSON_SERIALIZER))
            .build()
    }

    fun <T> createService(clazz: Class<T>) = retrofit.create(clazz)
}