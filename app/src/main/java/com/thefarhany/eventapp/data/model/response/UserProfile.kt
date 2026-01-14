package com.thefarhany.eventapp.data.model.response

import com.google.gson.annotations.SerializedName

data class UserProfile(
    @SerializedName("firstName")
    val firstName: String,

    @SerializedName("lastName")
    val lastName: String,

    @SerializedName("userName")
    val userName: String,

    @SerializedName("profilePicture")
    val profilePicture: String?,

    @SerializedName("email")
    val email: String,

    @SerializedName("phoneNumber")
    val phoneNumber: String
)

data class UserProfileResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("errorCode")
    val errorCode: String?,

    @SerializedName("data")
    val data: UserProfile?
)
