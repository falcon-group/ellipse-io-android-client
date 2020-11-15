package com.io.ellipse.data.network.http.rest.auth

import com.io.ellipse.data.network.http.HEADER_AUTHORIZATION
import com.io.ellipse.data.network.http.HEADER_AUTHORIZATION_BEARER
import com.io.ellipse.data.persistence.preferences.proto.auth.AuthPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val preferences: AuthPreferences
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response = runBlocking {
        val header = with(preferences.data.first()) {
            HEADER_AUTHORIZATION to "$HEADER_AUTHORIZATION_BEARER $authorizationToken"
        }
        val request = chain.request()
            .newBuilder()
            .header(header.first, header.second)
            .build()
        return@runBlocking chain.proceed(request)
    }
}