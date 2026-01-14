package com.thefarhany.eventapp.data.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class Booking(
    @SerializedName("bookingId")
    val bookingId: Long,

    @SerializedName("eventTitle")
    val eventTitle: String,

    @SerializedName("items")
    val items: List<BookingItem>,

    @SerializedName("totalPrice")
    val totalPrice: BigDecimal,

    @SerializedName("status")
    val status: String,

    @SerializedName("createdAt")
    val createdAt: String
)
