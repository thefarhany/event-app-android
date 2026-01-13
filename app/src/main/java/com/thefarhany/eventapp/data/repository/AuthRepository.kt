package com.thefarhany.eventapp.data.repository

import android.util.Log
import com.google.gson.Gson
import com.thefarhany.eventapp.data.model.request.LoginRequest
import com.thefarhany.eventapp.data.model.request.RegisterRequest
import com.thefarhany.eventapp.data.model.response.LoginResponse
import com.thefarhany.eventapp.data.model.response.RegisterResponse
import com.thefarhany.eventapp.data.remote.ApiService
import com.thefarhany.eventapp.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class AuthRepository(private val apiService: ApiService) {

    companion object {
        private const val TAG = "AuthRepository"
    }

    suspend fun register(request: RegisterRequest): Resource<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.register(request)

                if (response.success) {
                    Log.d(TAG, "Registration successful")
                    Resource.Success(response.data)
                } else {
                    Log.w(TAG, "Registration failed: ${response.message}")
                    val errorMessage = parseRegisterError(response.errorCode, response.message)
                    Resource.Error(errorMessage)
                }

            } catch (e: HttpException) {
                Log.e(TAG, "HTTP ${e.code()}: ${e.message()}")

                val errorMessage = try {
                    val errorBody = e.response()?.errorBody()?.string()

                    if (!errorBody.isNullOrEmpty()) {
                        val gson = Gson()
                        val errorResponse = gson.fromJson(errorBody, RegisterResponse::class.java)

                        parseRegisterError(errorResponse.errorCode, errorResponse.message)
                    } else {
                        "Registration failed"
                    }
                } catch (parseException: Exception) {
                    Log.e(TAG, "Failed to parse error: ${parseException.message}")
                    "Registration failed"
                }

                Resource.Error(errorMessage)

            } catch (e: IOException) {
                Log.e(TAG, "Network error: ${e.message}")
                Resource.Error("Connection failed. Please check your internet connection.")
            } catch (e: Exception) {
                Log.e(TAG, "Unknown error: ${e.message}", e)
                Resource.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    suspend fun login(request: LoginRequest): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.login(request)

                if (response.success) {
                    Log.d(TAG, "Login successful")
                    Resource.Success(null)
                } else {
                    Log.w(TAG, "Login failed: ${response.message}")
                    val errorMessage = parseLoginError(response.errorCode, response.message)
                    Resource.Error(errorMessage)
                }

            } catch (e: HttpException) {
                Log.e(TAG, "HTTP ${e.code()}: ${e.message()}")

                val errorMessage = try {
                    val errorBody = e.response()?.errorBody()?.string()

                    if (!errorBody.isNullOrEmpty()) {
                        val gson = Gson()
                        val errorResponse = gson.fromJson(errorBody, LoginResponse::class.java)
                        parseLoginError(errorResponse.errorCode, errorResponse.message)
                    } else {
                        "Login failed"
                    }
                } catch (parseException: Exception) {
                    Log.e(TAG, "Failed to parse error: ${parseException.message}")
                    "Login failed"
                }

                Resource.Error(errorMessage)

            } catch (e: IOException) {
                Log.e(TAG, "Network error: ${e.message}")
                Resource.Error("Connection failed. Please check your internet connection.")
            } catch (e: Exception) {
                Log.e(TAG, "Unknown error: ${e.message}", e)
                Resource.Error(e.message ?: "Login failed")
            }
        }
    }

    suspend fun logout(): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                apiService.logout()
                Resource.Success(null)
            } catch (e: Exception) {
                Log.e(TAG, "Logout failed: ${e.message}", e)
                Resource.Error(e.message ?: "Logout failed")
            }
        }
    }

    private fun parseRegisterError(errorCode: String?, message: String?): String {
        return when (errorCode) {
            "02" -> "Email already exists"
            "03" -> "Phone number already exists"
            "04" -> "Passwords do not match"
            "05" -> "Password must be at least 8 characters and contain both letters and numbers"
            else -> message ?: "Registration failed"
        }
    }

    private fun parseLoginError(errorCode: String?, message: String?): String {
        return when (errorCode) {
            "01" -> "Email not registered"
            "02" -> "Incorrect password"
            "03" -> "You are not authorized to access this page"
            else -> message ?: "Login failed"
        }
    }
}
