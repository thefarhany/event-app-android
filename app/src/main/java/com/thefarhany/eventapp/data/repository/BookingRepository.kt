package com.thefarhany.eventapp.data.repository

import com.thefarhany.eventapp.data.model.*
import com.thefarhany.eventapp.data.remote.ApiService
import com.thefarhany.eventapp.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

class BookingRepository(private val apiService: ApiService) {

    /**
     * Create new booking
     */
    suspend fun createBooking(request: CreateBookingRequest): Resource<Booking> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.createBooking(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Resource.Success(body.data)
                    } else {
                        Resource.Error(body?.message ?: "Failed to create booking")
                    }
                } else {
                    // ✅ Parse error response from backend
                    val errorMessage = parseErrorMessage(response.errorBody()?.string())
                    Resource.Error(errorMessage)
                }
            } catch (e: HttpException) {
                val errorMessage = parseErrorMessage(e.response()?.errorBody()?.string())
                Resource.Error(errorMessage)
            } catch (e: IOException) {
                Resource.Error("Connection failed. Please check your internet")
            } catch (e: Exception) {
                Resource.Error("Unexpected error occurred")
            }
        }
    }

    /**
     * Get my bookings list
     */
    suspend fun getMyBookings(): Resource<List<Booking>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getMyBookings()

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Resource.Success(body.data)
                    } else {
                        Resource.Success(emptyList())
                    }
                } else {
                    Resource.Error("Failed to load bookings")
                }
            } catch (e: HttpException) {
                Resource.Error("Network error: ${e.message()}")
            } catch (e: IOException) {
                Resource.Error("Connection failed. Please check your internet")
            } catch (e: Exception) {
                Resource.Error("Unexpected error occurred")
            }
        }
    }

    /**
     * Get booking detail by ID
     */
    suspend fun getBookingDetail(bookingId: Long): Resource<BookingDetail> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getBookingDetail(bookingId)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Resource.Success(body.data)
                    } else {
                        Resource.Error(body?.message ?: "Failed to load booking detail")
                    }
                } else {
                    when (response.code()) {
                        404 -> Resource.Error("Booking not found")
                        403 -> Resource.Error("You don't have access to this booking")
                        else -> Resource.Error("Failed to load booking detail")
                    }
                }
            } catch (e: HttpException) {
                Resource.Error("Network error: ${e.message()}")
            } catch (e: IOException) {
                Resource.Error("Connection failed. Please check your internet")
            } catch (e: Exception) {
                Resource.Error("Unexpected error occurred")
            }
        }
    }

    /**
     * Pay booking (generate QR code)
     */
    suspend fun payBooking(bookingId: Long): Resource<BookingDetail> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.payBooking(bookingId)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Resource.Success(body.data)
                    } else {
                        Resource.Error(body?.message ?: "Payment failed")
                    }
                } else {
                    // ✅ Parse payment error messages
                    val errorMessage = when (response.code()) {
                        404 -> "Booking not found"
                        400 -> parseErrorMessage(response.errorBody()?.string())
                            ?: "Booking already paid or cancelled"
                        403 -> "You don't have access to this booking"
                        else -> "Payment failed"
                    }
                    Resource.Error(errorMessage)
                }
            } catch (e: HttpException) {
                val errorMessage = parseErrorMessage(e.response()?.errorBody()?.string())
                Resource.Error(errorMessage)
            } catch (e: IOException) {
                Resource.Error("Connection failed. Please check your internet")
            } catch (e: Exception) {
                Resource.Error("Unexpected error occurred")
            }
        }
    }

    /**
     * Cancel booking
     */
    suspend fun cancelBooking(bookingId: Long): Resource<Booking> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.cancelBooking(bookingId)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Resource.Success(body.data)
                    } else {
                        Resource.Error(body?.message ?: "Failed to cancel booking")
                    }
                } else {
                    when (response.code()) {
                        404 -> Resource.Error("Booking not found")
                        400 -> Resource.Error("Cannot cancel this booking")
                        else -> Resource.Error("Failed to cancel booking")
                    }
                }
            } catch (e: HttpException) {
                Resource.Error("Network error: ${e.message()}")
            } catch (e: IOException) {
                Resource.Error("Connection failed. Please check your internet")
            } catch (e: Exception) {
                Resource.Error("Unexpected error occurred")
            }
        }
    }

    /**
     * ✅ Parse error message from backend BaseResponse
     */
    private fun parseErrorMessage(errorBody: String?): String {
        return try {
            if (errorBody.isNullOrEmpty()) {
                return "An error occurred"
            }

            val jsonObject = JSONObject(errorBody)

            // Try to get message from BaseResponse
            when {
                jsonObject.has("message") -> {
                    jsonObject.getString("message")
                }
                jsonObject.has("error") -> {
                    jsonObject.getString("error")
                }
                else -> {
                    "An error occurred"
                }
            }
        } catch (e: Exception) {
            "An error occurred"
        }
    }
}
