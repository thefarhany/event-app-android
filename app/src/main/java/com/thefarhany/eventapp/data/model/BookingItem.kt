package com.thefarhany.eventapp.data.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class BookingItem(
    @SerializedName("ticketId")
    val ticketId: Long,

    @SerializedName("ticketName")
    val ticketName: String,

    @SerializedName("quantity")
    val quantity: Int,

    @SerializedName("price")
    val price: BigDecimal,

    @SerializedName("subtotal")
    val subtotal: BigDecimal
)
