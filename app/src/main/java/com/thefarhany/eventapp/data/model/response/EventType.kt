package com.thefarhany.eventapp.data.model.response

import com.google.gson.annotations.SerializedName

enum class EventType {
    @SerializedName("ONLINE")
    ONLINE,

    @SerializedName("OFFLINE")
    OFFLINE;

    companion object {
        fun fromString(value: String): EventType? {
            return values().find {
                it.name.equals(value, ignoreCase = true)
            }
        }
    }
}