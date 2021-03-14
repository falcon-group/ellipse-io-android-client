package com.io.ellipse.data.network.http.rest.entity.auth.response

import com.google.gson.annotations.SerializedName

data class AuthResponseBody(
    @SerializedName("authorizationToken")
    val authorizationToken: String,
    @SerializedName("refreshToken")
    val refreshToken: String? = null
)