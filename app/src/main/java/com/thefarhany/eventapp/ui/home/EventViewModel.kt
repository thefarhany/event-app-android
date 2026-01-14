package com.thefarhany.eventapp.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thefarhany.eventapp.data.model.Event
import com.thefarhany.eventapp.data.model.response.EventCategories
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

    /**
     * Load all active events
     * Filter out events with remainingCapacity = 0 (sold out)
     */
    fun loadAllEvents() {
        _filteredEvents.value = Resource.Loading()

        viewModelScope.launch {
            val result = repository.getAllEvents()

            when (result) {
                is Resource.Success -> {
                    val availableEvents = result.data?.filter { event ->
                        event.remainingCapacity > 0
                    } ?: emptyList()

                    _events.postValue(Resource.Success(availableEvents))

                    if (availableEvents.isEmpty()) {
                        _filteredEvents.postValue(
                            Resource.Error("No events available")
                        )
                    } else {
                        _filteredEvents.postValue(Resource.Success(availableEvents))
                    }
                }
                is Resource.Error -> {
                    _events.postValue(result)
                    _filteredEvents.postValue(
                        Resource.Error(result.message ?: "Failed to load events. Please try again.")
                    )
                }
                is Resource.Loading -> {
                    _filteredEvents.postValue(Resource.Loading())
                }
            }
        }
    }

    /**
     * Load events by category
     * Filter out sold out events
     */
    fun loadEventsByCategory(category: String) {
        _filteredEvents.value = Resource.Loading()

        viewModelScope.launch {
            val result = if (category.equals("ALL", ignoreCase = true)) {
                repository.getAllEvents()
            } else {
                repository.getEventsByCategory(category.uppercase())
            }

            when (result) {
                is Resource.Success -> {
                    val availableEvents = result.data?.filter { event ->
                        event.remainingCapacity > 0
                    } ?: emptyList()

                    if (availableEvents.isEmpty()) {
                        val message = if (category.equals("ALL", ignoreCase = true)) {
                            "No events available"
                        } else {
                            "No events found in this category"
                        }
                        _filteredEvents.postValue(Resource.Error(message))
                    } else {
                        _filteredEvents.postValue(Resource.Success(availableEvents))
                    }
                }
                is Resource.Error -> {
                    _filteredEvents.postValue(
                        Resource.Error(result.message ?: "Failed to load events. Please try again.")
                    )
                }
                is Resource.Loading -> {
                    _filteredEvents.postValue(Resource.Loading())
                }
            }
        }
    }

    /**
     * Local filtering (faster, no network call)
     * ✅ FIXED: Handle EventCategories enum
     */
    fun filterEventsLocally(category: String) {
        val allEvents = _events.value

        if (allEvents is Resource.Success) {
            val filtered = if (category.equals("ALL", ignoreCase = true)) {
                allEvents.data
            } else {
                // ✅ Convert string to enum untuk comparison
                val categoryEnum = try {
                    EventCategories.valueOf(category.uppercase())
                } catch (e: IllegalArgumentException) {
                    null
                }

                allEvents.data?.filter { event ->
                    event.category == categoryEnum && // ✅ Enum comparison
                            event.remainingCapacity > 0
                }
            }

            if (filtered.isNullOrEmpty()) {
                _filteredEvents.value = Resource.Error("No events available")
            } else {
                _filteredEvents.value = Resource.Success(filtered)
            }
        }
    }

    /**
     * Search events by keyword
     * Filter out sold out events
     */
    fun searchEvents(keyword: String) {
        if (keyword.isBlank()) {
            loadAllEvents()
            return
        }

        _filteredEvents.value = Resource.Loading()

        viewModelScope.launch {
            val result = repository.searchEvents(keyword.trim())

            when (result) {
                is Resource.Success -> {
                    val availableEvents = result.data?.filter { event ->
                        event.remainingCapacity > 0
                    } ?: emptyList()

                    if (availableEvents.isEmpty()) {
                        _filteredEvents.postValue(
                            Resource.Error("No events found matching your search")
                        )
                    } else {
                        _filteredEvents.postValue(Resource.Success(availableEvents))
                    }
                }
                is Resource.Error -> {
                    _filteredEvents.postValue(
                        Resource.Error(result.message ?: "Failed to search events.")
                    )
                }
                is Resource.Loading -> {
                    _filteredEvents.postValue(Resource.Loading())
                }
            }
        }
    }
}
