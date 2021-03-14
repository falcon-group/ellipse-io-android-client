package com.io.ellipse.domain.usecase

import com.io.ellipse.data.network.http.rest.entity.auth.request.AuthRequestBody
import com.io.ellipse.data.network.http.rest.services.AuthService
import com.io.ellipse.data.persistence.preferences.proto.auth.AuthPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginUseCase @Inject constructor(
    private val authService: AuthService,
    private val authPreferences: AuthPreferences
) {

    suspend fun authorize(username: String, password: String) {
        val (token, refresh) = authService.authenticate(
            AuthRequestBody(
                username,
                password
            )
        )
        authPreferences.updateData {
            it.newBuilderForType()
                .setAuthorizationToken(token)
                .setRefreshToken(refresh ?: "")
                .build()
        }
    }
}