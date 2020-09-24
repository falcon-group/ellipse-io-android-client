package com.io.ellipse.data.network.http.rest.auth.jwt

interface JWTDecoder<T> {

    /**
     * Decodes jwt token using base64 components
     *
     * @param token - access token with structure header.payload.signature
     */
    fun decode(token: String) : T
}