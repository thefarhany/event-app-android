package com.thefarhany.eventapp.data.model.response

import com.google.gson.annotations.SerializedName

data class OnlineEventResponse(
    @SerializedName("platform")
    val platform: String,

    @SerializedName("linkUrl")
    val linkUrl: String
)
