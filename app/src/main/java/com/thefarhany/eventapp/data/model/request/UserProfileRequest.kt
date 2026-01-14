package com.thefarhany.eventapp.data.model.request

import com.google.gson.annotations.SerializedName

data class UpdateUserRequest(
    @SerializedName("firstName")
    val firstName: String,

    @SerializedName("lastName")
    val lastName: String,

    @SerializedName("userName")
    val userName: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("phoneNumber")
    val phoneNumber: String
)

data class PatchUserRequest(
    @SerializedName("firstName")
    val firstName: String? = null,

    @SerializedName("lastName")
    val lastName: String? = null,

    @SerializedName("userName")
    val userName: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("phoneNumber")
    val phoneNumber: String? = null,

    @SerializedName("profilePicture")
    val profilePicture: String? = null
)
