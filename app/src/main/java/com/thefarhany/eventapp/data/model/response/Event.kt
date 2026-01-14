package com.thefarhany.eventapp.data.model

import com.google.gson.annotations.SerializedName
import com.thefarhany.eventapp.data.model.response.EventCategories
import com.thefarhany.eventapp.data.model.response.EventType

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
    val eventType: EventType,

    @SerializedName("category")
    val category: EventCategories,

    @SerializedName("date")
    val date: String,

    @SerializedName("time")
    val time: String,

    @SerializedName("location")
    val location: LocationResponse?,

    @SerializedName("onlineEvent")
    val onlineEvent: OnlineEventResponse?,

    @SerializedName("price")
    val price: Int,

    @SerializedName("totalCapacity")
    val totalCapacity: Int,

    @SerializedName("remainingCapacity")
    val remainingCapacity: Int
)

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
