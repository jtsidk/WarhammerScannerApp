package com.example.warhammer40kscanner

import com.google.gson.annotations.SerializedName

data class StatisticsDTO(
    @SerializedName("unidad") val unidad: String,
    @SerializedName("m") val M: String?,
    @SerializedName("t") val T: String?,
    @SerializedName("w") val W: String?,
    @SerializedName("ld") val LD: String?,
    @SerializedName("sv") val SV: String?,
    @SerializedName("oc") val OC: String?
)