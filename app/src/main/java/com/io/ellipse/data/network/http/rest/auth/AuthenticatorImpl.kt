package com.io.ellipse.data.network.http.rest.auth

import com.io.ellipse.data.network.http.rest.ServiceFactory
import com.io.ellipse.data.network.http.rest.services.AuthService
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.nio.channels.NotYetBoundException

class AuthenticatorImpl constructor(serviceFactory: ServiceFactory) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? = runBlocking {
        null
    }
}