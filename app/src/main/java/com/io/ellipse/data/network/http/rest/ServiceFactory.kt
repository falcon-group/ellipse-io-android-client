package com.io.ellipse.data.network.http.rest

import com.io.ellipse.BuildConfig
import com.io.ellipse.data.network.http.HEADER_AUTHORIZATION
import com.io.ellipse.data.network.http.HEADER_AUTHORIZATION_BEARER
import com.io.ellipse.data.network.http.JSON_SERIALIZER
import com.io.ellipse.data.network.http.rest.auth.AuthInterceptor
import com.io.ellipse.data.network.http.rest.auth.AuthenticatorImpl
import com.io.ellipse.data.persistence.preferences.proto.auth.AuthPreferences
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceFactory @Inject constructor(
    private val authInterceptor: AuthInterceptor,
    private val authPreferences: AuthPreferences
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

    suspend fun createWebSocket(webSocketListener: WebSocketListener): WebSocket {
        val auth = authPreferences.data.first()
        val tokenValue = "$HEADER_AUTHORIZATION_BEARER ${auth.authorizationToken}"
        return httpClient.newWebSocket(
            Request.Builder()
                .url("ws://elepsio.herokuapp.com/")
                .header(HEADER_AUTHORIZATION, tokenValue)
                .build(),
            webSocketListener
        )
    }
}