package com.io.ellipse.data.network.http.rest.entity.auth.response

import com.google.gson.annotations.SerializedName

data class AuthResponseBody(
    @SerializedName("token")
    val authorizationToken: String,
    @SerializedName("refreshToken")
    val refreshToken: String? = null
)