package com.thefarhany.eventapp.data.model

import com.google.gson.annotations.SerializedName

data class EventDetail(
    @SerializedName("title")
    val title: String,

    @SerializedName("shortSummary")
    val shortSummary: String?,

    @SerializedName("description")
    val description: String,

    @SerializedName("imageUrl")
    val imageUrl: String?,

    @SerializedName("date")
    val date: String,

    @SerializedName("time")
    val time: String,

    @SerializedName("eventType")
    val eventType: String,

    @SerializedName("category")
    val category: String,

    @SerializedName("totalCapacity")
    val totalCapacity: Int,

    @SerializedName("remainingCapacity")
    val remainingCapacity: Int,

    @SerializedName("location")
    val location: LocationResponse?,

    @SerializedName("onlineEvent")
    val onlineEvent: OnlineEventResponse?,

    @SerializedName("tickets")
    val tickets: List<TicketResponse>,

    @SerializedName("warningMessage")
    val warningMessage: String?
)

// LocationResponse - tambahkan field yang missing
data class LocationResponse(
    @SerializedName("venue")
    val venue: String,

    @SerializedName("address")
    val address: String,

    @SerializedName("city")
    val city: String,

    @SerializedName("country")
    val country: String
)

// ⚠️ TAMBAH DATA CLASS INI untuk online event
data class OnlineEventResponse(
    @SerializedName("platform")
    val platform: String,

    @SerializedName("linkUrl")
    val linkUrl: String
)

data class TicketResponse(
    @SerializedName("ticketName")
    val ticketName: String,

    @SerializedName("price")
    val price: Double,

    @SerializedName("quantity")
    val quantity: Int
)

// Response wrapper tetap sama
data class EventDetailResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("errorCode")
    val errorCode: String?,

    @SerializedName("data")
    val data: EventDetail?
)
