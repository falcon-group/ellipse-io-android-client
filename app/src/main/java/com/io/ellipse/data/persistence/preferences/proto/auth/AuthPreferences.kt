package com.io.ellipse.data.persistence.preferences.proto.auth

import android.content.Context
import androidx.datastore.createDataStore
import com.io.ellipse.data.persistence.preferences.proto.BasePreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthPreferences @Inject constructor(
    @ApplicationContext context: Context,
    serializer: AuthModelSerializer
) : BasePreferences<AuthModel>(
    context.createDataStore(
        fileName = DATAFILE_PATH,
        serializer = serializer,
        scope = CoroutineScope(Dispatchers.IO)
    )
) {

    companion object {
        private const val DATAFILE_PATH = "auth_preferences.pb"
    }

    suspend fun setAuthToken(authToken: String) = updateData {
        it.toBuilder().setAuthorizationToken(authToken).build()
    }

    suspend fun setRefreshToken(refreshToken: String) = updateData {
        it.toBuilder().setRefreshToken(refreshToken).build()
    }
}