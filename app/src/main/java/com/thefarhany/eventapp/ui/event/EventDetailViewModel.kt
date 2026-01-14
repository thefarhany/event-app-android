package com.thefarhany.eventapp.ui.event

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thefarhany.eventapp.data.model.EventDetail
import com.thefarhany.eventapp.data.model.TicketSelection
import com.thefarhany.eventapp.data.repository.EventRepository
import com.thefarhany.eventapp.utils.Resource
import kotlinx.coroutines.launch

class EventDetailViewModel(private val repository: EventRepository) : ViewModel() {

    private val _eventDetail = MutableLiveData<Resource<EventDetail>>()
    val eventDetail: LiveData<Resource<EventDetail>> = _eventDetail

    private val _selectedTickets = MutableLiveData<List<TicketSelection>>()
    val selectedTickets: LiveData<List<TicketSelection>> = _selectedTickets

    private val _totalTickets = MutableLiveData<Int>(0)
    val totalTickets: LiveData<Int> = _totalTickets

    private val _totalPrice = MutableLiveData<Double>(0.0)
    val totalPrice: LiveData<Double> = _totalPrice

    fun loadEventDetail(eventId: Long) {
        _eventDetail.value = Resource.Loading()

        viewModelScope.launch {
            val result = repository.getEventDetail(eventId)
            _eventDetail.value = result
        }
    }

    fun updateTicketSelections(selections: List<TicketSelection>) {
        _selectedTickets.value = selections

        // Calculate total tickets
        val total = selections.sumOf { it.selectedQuantity }
        _totalTickets.value = total

        // Calculate total price
        val price = selections.sumOf { it.ticket.price * it.selectedQuantity }
        _totalPrice.value = price
    }
}
