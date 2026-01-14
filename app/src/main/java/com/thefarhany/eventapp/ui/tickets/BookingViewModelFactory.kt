package com.thefarhany.eventapp.ui.tickets

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.thefarhany.eventapp.ui.booking.BookingViewModel

class BookingViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookingViewModel::class.java)) {
            return BookingViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}