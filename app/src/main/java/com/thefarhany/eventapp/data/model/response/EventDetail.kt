package com.thefarhany.eventapp.data.model

import com.google.gson.annotations.SerializedName
import com.thefarhany.eventapp.data.model.response.EventCategories
import com.thefarhany.eventapp.data.model.response.EventType

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
    val eventType: EventType,

    @SerializedName("category")
    val category: EventCategories,

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

data class OnlineEventResponse(
    @SerializedName("platform")
    val platform: String,

    @SerializedName("linkUrl")
    val linkUrl: String
)

data class TicketResponse(
    @SerializedName("ticketId")
    val ticketId: Long,

    @SerializedName("ticketName")
    val ticketName: String,

    @SerializedName("price")
    val price: Double,

    @SerializedName("quantity")
    val quantity: Int,
)

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
