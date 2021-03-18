package com.io.ellipse.data.network.http.rest.auth

import android.content.Context
import android.content.Intent
import com.io.ellipse.presentation.login.LoginActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthenticatorImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? = runBlocking {
        val intent = LoginActivity.newIntent(context)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
        null
    }
}