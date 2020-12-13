package com.io.ellipse.data.persistence.database.entity.tracker

import com.google.gson.annotations.SerializedName

data class TrackerData(
    @SerializedName("hr")
    val heartRate: Int,
    @SerializedName("IsCritical")
    val isUrgent: Boolean
)