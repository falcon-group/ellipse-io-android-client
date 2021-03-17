package com.io.ellipse.data.network.http.rest.entity.params

import com.google.gson.annotations.SerializedName
import java.util.*

data class ParamsBody(
    @SerializedName("heartRate")
    val heartRate: Int,
    @SerializedName("isUrgent")
    val isUrgent: Boolean,
    @SerializedName("createDate")
    val createDate: Date
)