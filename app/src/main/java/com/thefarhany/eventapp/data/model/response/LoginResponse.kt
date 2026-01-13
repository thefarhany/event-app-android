package com.thefarhany.eventapp.data.model.response

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("data")
    val data: Any? = null,

    @SerializedName("errorCode")
    val errorCode: String? = null,

    @SerializedName("message")
    val message: String,

    @SerializedName("success")
    val success: Boolean
)
