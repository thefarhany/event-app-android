package com.thefarhany.eventapp.data.model.response

import com.google.gson.annotations.SerializedName

enum class EventCategories(val displayValue: String) {
    @SerializedName("SPORTS")
    SPORTS("Sports"),

    @SerializedName("MUSIC")
    MUSIC("Music"),

    @SerializedName("CONFERENCE")
    CONFERENCE("Conference"),

    @SerializedName("WORKSHOP")
    WORKSHOP("Workshop"),

    @SerializedName("EXHIBITION")
    EXHIBITION("Exhibition"),

    @SerializedName("FESTIVALS")
    FESTIVALS("Festivals"),

    @SerializedName("SEMINAR")
    SEMINAR("Seminar"),

    @SerializedName("ARTS_THEATRE")
    ARTS_THEATRE("Arts & Theatre"),

    @SerializedName("COMPETITION")
    COMPETITION("Competition");

    companion object {
        fun fromString(value: String): EventCategories? {
            return values().find {
                it.name.equals(value, ignoreCase = true)
            }
        }
    }
}