package com.io.ellipse.data.network.http

import com.google.gson.GsonBuilder

const val HEADER_AUTHORIZATION = "Authorization"
const val HEADER_AUTHORIZATION_BEARER = "Bearer"

val JSON_SERIALIZER = GsonBuilder()
    .setPrettyPrinting()
    .setLenient()
    .create()