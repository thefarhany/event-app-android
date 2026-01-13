package com.thefarhany.eventapp.data.model.response

import com.google.gson.annotations.SerializedName

data class RegisterResponse(
    @SerializedName("data")
    val data: String? = null,

    @SerializedName("errorCode")
    val errorCode: String? = null,

    @SerializedName("message")
    val message: String,

    @SerializedName("success")
    val success: Boolean
)