package com.io.ellipse.data.network.http.rest.entity.auth.request

import com.google.gson.annotations.SerializedName


data class AuthRequestBody(
    @SerializedName("phone")
    val phone: String,
    @SerializedName("password")
    val password: String
)