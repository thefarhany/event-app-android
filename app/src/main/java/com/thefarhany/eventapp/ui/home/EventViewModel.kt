package com.thefarhany.eventapp.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thefarhany.eventapp.data.model.Event
import com.thefarhany.eventapp.data.repository.EventRepository
import com.thefarhany.eventapp.utils.Resource
import kotlinx.coroutines.launch

class EventViewModel(private val repository: EventRepository) : ViewModel() {

    private val _events = MutableLiveData<Resource<List<Event>>>()
    val events: LiveData<Resource<List<Event>>> = _events

    private val _filteredEvents = MutableLiveData<Resource<List<Event>>>()
    val filteredEvents: LiveData<Resource<List<Event>>> = _filteredEvents

    init {
        loadAllEvents()
    }

    fun loadAllEvents() {
        _events.value = Resource.Loading()
        viewModelScope.launch {
            val result = repository.getAllEvents()
            _events.postValue(result)
            _filteredEvents.postValue(result)
        }
    }

    fun loadEventsByCategory(category: String) {
        _filteredEvents.value = Resource.Loading()
        viewModelScope.launch {
            val result = if (category.equals("ALL", ignoreCase = true)) {
                repository.getAllEvents()
            } else {
                repository.getEventsByCategory(category.uppercase())
            }
            _filteredEvents.postValue(result)
        }
    }

    fun filterEventsLocally(category: String) {
        val allEvents = _events.value
        if (allEvents is Resource.Success) {
            val filtered = if (category.equals("ALL", ignoreCase = true)) {
                allEvents.data
            } else {
                allEvents.data?.filter {
                    it.category.equals(category, ignoreCase = true)
                }
            }
            _filteredEvents.value = Resource.Success(filtered ?: emptyList())
        }
    }
}