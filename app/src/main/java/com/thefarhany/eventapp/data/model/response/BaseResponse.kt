package com.thefarhany.eventapp.data.model.response

data class BaseResponse<T>(
    val success: Boolean,
    val message: String,
    val errorCode: String?,
    val data: T?
)
