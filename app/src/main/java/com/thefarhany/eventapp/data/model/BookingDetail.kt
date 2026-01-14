package com.thefarhany.eventapp.data.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class BookingDetail(
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

    @SerializedName("paidAt")
    val paidAt: String?,

    @SerializedName("qrCode")
    val qrCode: String?,

    @SerializedName("createdAt")
    val createdAt: String
)
