package com.thefarhany.eventapp.data.model.response

data class UserResponse(
    val userId: Long,
    val firstName: String,
    val lastName: String,
    val userName: String,
    val email: String,
    val phoneNumber: String,
    val role: String,
    val profilePicture: String?
)
