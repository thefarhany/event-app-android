package com.thefarhany.eventapp.data.model

import com.google.gson.annotations.SerializedName

data class CreateBookingRequest(
    @SerializedName("eventId")
    val eventId: Long,

    @SerializedName("tickets")
    val tickets: List<TicketOrder>
) {
    data class TicketOrder(
        @SerializedName("ticketId")
        val ticketId: Long,

        @SerializedName("quantity")
        val quantity: Int
    )
}
