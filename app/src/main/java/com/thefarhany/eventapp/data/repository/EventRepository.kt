package com.thefarhany.eventapp.data.repository

import android.util.Log
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

    /**
     * Get all events
     */
    suspend fun getAllEvents(): Resource<List<Event>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAllEvents()

                if (response.success && response.data != null) {
                    Log.d(TAG, "Events fetched successfully: ${response.data.size} items")
                    Resource.Success(response.data)
                } else {
                    Log.w(TAG, "No events available: ${response.message}")
                    Resource.Error(response.message)
                }

            } catch (e: HttpException) {
                Log.e(TAG, "HTTP error: ${e.code()} - ${e.message()}")
                val errorMsg = when (e.code()) {
                    404 -> "No events available"
                    500 -> "Server error. Please try again later"
                    else -> "Failed to load events. Please try again."
                }
                Resource.Error(errorMsg)
            } catch (e: IOException) {
                Log.e(TAG, "Network error: ${e.message}")
                Resource.Error("Connection failed. Please check your internet.")
            } catch (e: Exception) {
                Log.e(TAG, "Unknown error: ${e.message}", e)
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
                    Log.d(TAG, "Events by category '$category' fetched: ${response.data.size} items")
                    Resource.Success(response.data)
                } else {
                    Log.w(TAG, "No events in category '$category': ${response.message}")
                    Resource.Error(response.message)
                }

            } catch (e: HttpException) {
                Log.e(TAG, "HTTP error: ${e.code()}")
                val errorMsg = when (e.code()) {
                    404 -> "No events found in this category"
                    else -> "Failed to load events."
                }
                Resource.Error(errorMsg)
            } catch (e: IOException) {
                Log.e(TAG, "Network error: ${e.message}")
                Resource.Error("Connection failed.")
            } catch (e: Exception) {
                Log.e(TAG, "Unknown error: ${e.message}", e)
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
                    Log.d(TAG, "Event detail fetched: ${response.data.title}")
                    Resource.Success(response.data)
                } else {
                    Log.w(TAG, "Event not found")
                    Resource.Error("Event not found")
                }

            } catch (e: HttpException) {
                Log.e(TAG, "HTTP error: ${e.code()}")
                val errorMsg = when (e.code()) {
                    404 -> "Event not found"
                    else -> "Failed to load event detail."
                }
                Resource.Error(errorMsg)
            } catch (e: IOException) {
                Log.e(TAG, "Network error: ${e.message}")
                Resource.Error("Connection failed.")
            } catch (e: Exception) {
                Log.e(TAG, "Unknown error: ${e.message}", e)
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
                    Log.d(TAG, "Search results for '$keyword': ${response.data.size} items")
                    Resource.Success(response.data)
                } else {
                    Log.w(TAG, "No events found for '$keyword'")
                    Resource.Error(response.message)
                }
            } catch (e: HttpException) {
                Log.e(TAG, "HTTP error: ${e.code()}")
                val errorMsg = when (e.code()) {
                    404 -> "No events found matching your search"
                    else -> "Failed to search events."
                }
                Resource.Error(errorMsg)
            } catch (e: IOException) {
                Log.e(TAG, "Network error: ${e.message}")
                Resource.Error("Connection failed.")
            } catch (e: Exception) {
                Log.e(TAG, "Unknown error: ${e.message}", e)
                Resource.Error(e.message ?: "Unknown error")
            }
        }
    }
}