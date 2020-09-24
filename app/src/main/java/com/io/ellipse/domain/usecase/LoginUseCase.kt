package com.io.ellipse.domain.usecase

import com.io.ellipse.data.network.http.rest.entity.auth.request.AuthRequestBody
import com.io.ellipse.data.network.http.rest.entity.auth.response.AuthResponseBody
import com.io.ellipse.data.network.http.rest.services.AuthService
import com.io.ellipse.data.persistence.preferences.proto.auth.AuthPreferences
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authService: AuthService,
    private val authPreferences: AuthPreferences
) {

    suspend fun authorize(username: String, password: String) {
        val authBody: AuthResponseBody = authService.authenticate(
            AuthRequestBody(
                username,
                password
            )
        )
        with(authBody) {
            authPreferences.setAuthToken(authorizationToken)
            authPreferences.setRefreshToken(refreshToken ?: "")
        }
    }
}