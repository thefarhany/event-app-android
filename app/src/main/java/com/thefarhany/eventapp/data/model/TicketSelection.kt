package com.thefarhany.eventapp.data.model

data class TicketSelection(
    val ticket: TicketResponse,
    var selectedQuantity: Int = 0
)
