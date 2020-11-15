package com.io.ellipse.domain.usecase

import com.io.ellipse.data.persistence.database.AppDatabase
import com.io.ellipse.data.persistence.preferences.proto.auth.AuthPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogoutUseCase @Inject constructor(
    private val authPreferences: AuthPreferences,
    private val database: AppDatabase
) {

    suspend fun clearSession() = with(authPreferences) {
        setAuthToken("")
        setRefreshToken("")
        database.clearAllTables()
    }
}