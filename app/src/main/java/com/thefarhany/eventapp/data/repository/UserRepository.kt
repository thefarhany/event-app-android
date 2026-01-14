package com.thefarhany.eventapp.data.repository

import android.util.Log
import com.thefarhany.eventapp.data.model.request.PatchUserRequest
import com.thefarhany.eventapp.data.model.request.UpdateUserRequest
import com.thefarhany.eventapp.data.model.response.UserProfile
import com.thefarhany.eventapp.data.remote.ApiService
import com.thefarhany.eventapp.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class UserRepository(private val apiService: ApiService) {

    companion object {
        private const val TAG = "UserRepository"
    }

    /**
     * Get current user profile
     */
    suspend fun getMyProfile(): Resource<UserProfile> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getMyProfile()
                if (response.success && response.data != null) {
                    Resource.Success(response.data)
                } else {
                    Resource.Error(response.message)
                }
            } catch (e: HttpException) {
                val errorMsg = when (e.code()) {
                    401 -> "Unauthorized. Please login again."
                    404 -> "User not found"
                    else -> "Failed to load profile"
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
     * Update user profile (PUT - full update)
     */
    suspend fun updateProfile(request: UpdateUserRequest): Resource<UserProfile> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.updateProfile(request)
                if (response.success && response.data != null) {
                    Log.d(TAG, "Profile updated successfully")
                    Resource.Success(response.data)
                } else {
                    Log.w(TAG, "Failed to update profile: ${response.message}")
                    Resource.Error(response.message)
                }
            } catch (e: HttpException) {
                Log.e(TAG, "HTTP error: ${e.code()}")
                val errorMsg = when (e.code()) {
                    400 -> "Invalid data. Please check your input."
                    401 -> "Unauthorized. Please login again."
                    else -> "Failed to update profile"
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
     * Patch user profile (PATCH - partial update)
     */
    suspend fun patchProfile(request: PatchUserRequest): Resource<UserProfile> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.patchProfile(request)
                if (response.success && response.data != null) {
                    Log.d(TAG, "Profile patched successfully")
                    Resource.Success(response.data)
                } else {
                    Log.w(TAG, "Failed to patch profile: ${response.message}")
                    Resource.Error(response.message)
                }
            } catch (e: HttpException) {
                Log.e(TAG, "HTTP error: ${e.code()}")
                Resource.Error("Failed to update profile")
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