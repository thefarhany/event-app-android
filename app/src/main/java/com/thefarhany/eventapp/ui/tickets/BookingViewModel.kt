package com.thefarhany.eventapp.ui.booking

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.thefarhany.eventapp.data.model.*
import com.thefarhany.eventapp.data.remote.RetrofitClient
import com.thefarhany.eventapp.data.repository.BookingRepository
import com.thefarhany.eventapp.utils.Resource
import kotlinx.coroutines.launch

class BookingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BookingRepository

    init {
        val apiService = RetrofitClient.instance
        repository = BookingRepository(apiService)
    }

    private val _myBookings = MutableLiveData<Resource<List<Booking>>>()
    val myBookings: LiveData<Resource<List<Booking>>> = _myBookings

    private val _bookingDetail = MutableLiveData<Resource<BookingDetail>>()
    val bookingDetail: LiveData<Resource<BookingDetail>> = _bookingDetail

    private val _createBooking = MutableLiveData<Resource<Booking>?>()
    val createBooking: MutableLiveData<Resource<Booking>?> = _createBooking

    private val _payBooking = MutableLiveData<Resource<BookingDetail>?>()
    val payBooking: MutableLiveData<Resource<BookingDetail>?> = _payBooking

    private val _cancelBooking = MutableLiveData<Resource<Booking>?>()
    val cancelBooking: MutableLiveData<Resource<Booking>?> = _cancelBooking

    fun loadMyBookings() {
        _myBookings.value = Resource.Loading()

        viewModelScope.launch {
            val result = repository.getMyBookings()
            _myBookings.value = result
        }
    }

    fun loadBookingDetail(bookingId: Long) {
        _bookingDetail.value = Resource.Loading()

        viewModelScope.launch {
            val result = repository.getBookingDetail(bookingId)
            _bookingDetail.value = result
        }
    }

    fun createBooking(eventId: Long, tickets: List<CreateBookingRequest.TicketOrder>) {
        _createBooking.value = Resource.Loading()

        viewModelScope.launch {
            val request = CreateBookingRequest(
                eventId = eventId,
                tickets = tickets
            )
            val result = repository.createBooking(request)
            _createBooking.value = result
        }
    }

    fun resetCreateBookingState() {
        _createBooking.value = null
    }

    fun payBooking(bookingId: Long) {
        _payBooking.value = Resource.Loading()

        viewModelScope.launch {
            val result = repository.payBooking(bookingId)
            _payBooking.value = result
        }
    }

    fun resetPayBookingState() {
        _payBooking.value = null
    }

    fun cancelBooking(bookingId: Long) {
        _cancelBooking.value = Resource.Loading()

        viewModelScope.launch {
            val result = repository.cancelBooking(bookingId)
            _cancelBooking.value = result
        }
    }

    fun resetCancelBookingState() {
        _cancelBooking.value = null
    }
}
