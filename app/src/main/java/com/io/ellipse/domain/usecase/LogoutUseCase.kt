package com.io.ellipse.domain.usecase

import com.io.ellipse.data.persistence.preferences.proto.auth.AuthPreferences
import javax.inject.Inject

class LogoutUseCase @Inject constructor(private val authPreferences: AuthPreferences) {

    suspend fun clearSession() = with(authPreferences) {
        setAuthToken("")
        setRefreshToken("")
    }
}