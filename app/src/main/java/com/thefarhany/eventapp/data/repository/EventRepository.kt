package com.thefarhany.eventapp.data.repository

import com.thefarhany.eventapp.data.model.Event
import com.thefarhany.eventapp.data.model.EventDetail
import com.thefarhany.eventapp.data.remote.ApiService
import com.thefarhany.eventapp.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class EventRepository(private val apiService: ApiService) {

    companion object {
        private const val TAG = "EventRepository"
    }

    suspend fun getAllEvents(): Resource<List<Event>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAllEvents()

                if (response.success && response.data != null) {
                    Resource.Success(response.data)
                } else {
                    Resource.Error(response.message)
                }

            } catch (e: HttpException) {
                val errorMsg = when (e.code()) {
                    404 -> "No events available"
                    500 -> "Server error. Please try again later"
                    else -> "Failed to load events. Please try again."
                }
                Resource.Error(errorMsg)
            } catch (e: IOException) {
                Resource.Error("Connection failed. Please check your internet.")
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    /**
     * Get events by category
     */
    suspend fun getEventsByCategory(category: String): Resource<List<Event>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getEventsByCategory(category)
                if (response.success && response.data != null) {
                    Resource.Success(response.data)
                } else {
                    Resource.Error(response.message ?: "No events found in this category")
                }
            } catch (e: HttpException) {
                val errorMsg = when (e.code()) {
                    400, 404 -> "No events found in this category"
                    else -> "Failed to load events."
                }
                Resource.Error(errorMsg)
            } catch (e: IOException) {
                Resource.Error("Connection failed.")
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Unknown error")
            }
        }
    }


    /**
     * Get event detail by ID
     */
    suspend fun getEventDetail(eventId: Long): Resource<EventDetail> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getEventDetail(eventId)

                if (response.success && response.data != null) {
                    Resource.Success(response.data)
                } else {
                    Resource.Error("Event not found")
                }

            } catch (e: HttpException) {
                val errorMsg = when (e.code()) {
                    404 -> "Event not found"
                    else -> "Failed to load event detail."
                }
                Resource.Error(errorMsg)
            } catch (e: IOException) {
                Resource.Error("Connection failed.")
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Search events by keyword
     */
    suspend fun searchEvents(keyword: String): Resource<List<Event>> {
        return withContext(Dispatchers.IO) {
            try {
                if (keyword.isBlank()) {
                    return@withContext Resource.Error<List<Event>>("Search keyword cannot be empty")
                }

                val response = apiService.searchEvents(keyword)
                if (response.success && response.data != null) {
                    Resource.Success(response.data)
                } else {
                    Resource.Error(response.message)
                }
            } catch (e: HttpException) {
                val errorMsg = when (e.code()) {
                    404 -> "No events found matching your search"
                    else -> "Failed to search events."
                }
                Resource.Error(errorMsg)
            } catch (e: IOException) {
                Resource.Error("Connection failed.")
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Unknown error")
            }
        }
    }
}