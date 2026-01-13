package com.thefarhany.eventapp.data.remote

import com.thefarhany.eventapp.data.model.EventDetailResponse
import com.thefarhany.eventapp.data.model.EventResponse
import com.thefarhany.eventapp.data.model.request.LoginRequest
import com.thefarhany.eventapp.data.model.request.RegisterRequest
import com.thefarhany.eventapp.data.model.response.BaseResponse
import com.thefarhany.eventapp.data.model.response.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): RegisterResponse

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): BaseResponse<Unit>

    @POST("auth/logout")
    suspend fun logout(): Response<String>

    @GET("events")
    suspend fun getAllEvents(): EventResponse

    @GET("events/category/{category}")
    suspend fun getEventsByCategory(
        @Path("category") category: String
    ): EventResponse

    @GET("events/{id}")
    suspend fun getEventDetail(
        @Path("id") eventId: Long
    ): EventDetailResponse

    @GET("events/search")
    suspend fun searchEvents(
        @Query("keyword") keyword: String
    ): EventResponse
}