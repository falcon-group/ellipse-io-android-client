package com.io.ellipse.domain.usecase

import com.io.ellipse.data.persistence.preferences.proto.auth.AuthPreferences
import kotlinx.coroutines.flow.take
import javax.inject.Inject

class SessionExistenceUseCase @Inject constructor(private val authPreferences: AuthPreferences) {

    fun retrieveCurrentSession() = authPreferences.data.take(1)

}