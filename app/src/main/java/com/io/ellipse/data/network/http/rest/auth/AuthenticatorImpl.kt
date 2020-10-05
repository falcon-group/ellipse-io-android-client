package com.io.ellipse.data.network.http.rest.auth

import com.io.ellipse.data.network.http.rest.ServiceFactory
import com.io.ellipse.data.network.http.rest.services.AuthService
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class AuthenticatorImpl constructor(serviceFactory: ServiceFactory) : Authenticator {

    private val authService: AuthService by lazy {
        serviceFactory.createService(AuthService::class.java)
    }

    override fun authenticate(route: Route?, response: Response): Request? = runBlocking {
        TODO("Not yet implemented")
    }
}