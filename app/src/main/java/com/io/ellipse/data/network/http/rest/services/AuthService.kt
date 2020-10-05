package com.io.ellipse.data.network.http.rest.services

import com.io.ellipse.data.network.http.rest.entity.auth.request.AuthRequestBody
import com.io.ellipse.data.network.http.rest.entity.auth.response.AuthResponseBody
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {

    companion object {

        private const val LOGIN_ENDPOINT = "/auth/login"
    }

    @POST(LOGIN_ENDPOINT)
    suspend fun authenticate(@Body authRequestBody: AuthRequestBody): AuthResponseBody
}