package com.thefarhany.eventapp.data.model.request

data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val userName: String,
    val email: String,
    val phoneNumber: String,
    val password: String,
    val confirmPassword: String
)
