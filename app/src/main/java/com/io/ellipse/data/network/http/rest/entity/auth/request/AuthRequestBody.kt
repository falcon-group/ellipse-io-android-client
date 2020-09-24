package com.io.ellipse.data.network.http.rest.entity.auth.request

import com.google.gson.annotations.SerializedName


data class AuthRequestBody(
    @SerializedName("username")
    val phone: String,
    @SerializedName("password")
    val password: String
)