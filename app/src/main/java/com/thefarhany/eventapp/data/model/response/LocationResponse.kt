package com.thefarhany.eventapp.data.model.response

import com.google.gson.annotations.SerializedName

data class LocationResponse(
    @SerializedName("venue")
    val venue: String,

    @SerializedName("address")
    val address: String,

    @SerializedName("city")
    val city: String,

    @SerializedName("country")
    val country: String
)
