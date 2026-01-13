package com.thefarhany.eventapp.data.model

import com.google.gson.annotations.SerializedName

data class Event(
    @SerializedName("eventId")
    val eventId: Long,

    @SerializedName("title")
    val title: String,

    @SerializedName("shortSummary")
    val shortSummary: String?,

    @SerializedName("imageUrl")
    val imageUrl: String?,

    @SerializedName("eventType")
    val eventType: String,

    @SerializedName("category")
    val category: String,

    @SerializedName("date")
    val date: String,

    @SerializedName("time")
    val time: String,

    @SerializedName("venue")
    val venue: String?,

    @SerializedName("city")
    val city: String?,

    @SerializedName("price")
    val price: Int,

    @SerializedName("totalCapacity")
    val totalCapacity: Int,

    @SerializedName("remainingCapacity")
    val remainingCapacity: Int
)

// Response wrapper tetap sama
data class EventResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("errorCode")
    val errorCode: String?,

    @SerializedName("data")
    val data: List<Event>?
)
