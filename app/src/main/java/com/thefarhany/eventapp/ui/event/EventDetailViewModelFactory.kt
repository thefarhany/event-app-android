package com.thefarhany.eventapp.ui.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.thefarhany.eventapp.data.repository.EventRepository

class EventDetailViewModelFactory(
    private val repository: EventRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EventDetailViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
