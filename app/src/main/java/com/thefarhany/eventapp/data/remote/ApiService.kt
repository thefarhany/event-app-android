package com.thefarhany.eventapp.data.remote

import com.thefarhany.eventapp.data.model.Booking
import com.thefarhany.eventapp.data.model.BookingDetail
import com.thefarhany.eventapp.data.model.CreateBookingRequest
import com.thefarhany.eventapp.data.model.EventDetailResponse
import com.thefarhany.eventapp.data.model.EventResponse
import com.thefarhany.eventapp.data.model.request.LoginRequest
import com.thefarhany.eventapp.data.model.request.PatchUserRequest
import com.thefarhany.eventapp.data.model.request.RegisterRequest
import com.thefarhany.eventapp.data.model.request.UpdateUserRequest
import com.thefarhany.eventapp.data.model.response.BaseResponse
import com.thefarhany.eventapp.data.model.response.RegisterResponse
import com.thefarhany.eventapp.data.model.response.UserProfileResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
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
    ): Response<BaseResponse<Any>>

    @POST("auth/logout")
    suspend fun logout(): Response<String>

    @GET("events")
    suspend fun getAllEvents(): EventResponse

    @GET("events/category")
    suspend fun getEventsByCategory(
        @Query("category") category: String
    ): EventResponse

    @GET("events/{eventId}")
    suspend fun getEventDetail(
        @Path("eventId") eventId: Long
    ): EventDetailResponse

    @GET("events/search")
    suspend fun searchEvents(
        @Query("keyword") keyword: String
    ): EventResponse

    @GET("users/me")
    suspend fun getMyProfile(): UserProfileResponse

    @PUT("users/me")
    suspend fun updateProfile(
        @Body request: UpdateUserRequest
    ): UserProfileResponse

    @PATCH("users/me")
    suspend fun patchProfile(
        @Body request: PatchUserRequest
    ): UserProfileResponse

    @POST("bookings")
    suspend fun createBooking(
        @Body request: CreateBookingRequest
    ): Response<BaseResponse<Booking>>

    @GET("bookings/me")
    suspend fun getMyBookings(): Response<BaseResponse<List<Booking>>>

    @GET("bookings/{id}")
    suspend fun getBookingDetail(
        @Path("id") bookingId: Long
    ): Response<BaseResponse<BookingDetail>>

    @POST("bookings/{id}/pay")
    suspend fun payBooking(
        @Path("id") bookingId: Long
    ): Response<BaseResponse<BookingDetail>>

    @POST("bookings/{id}/cancel")
    suspend fun cancelBooking(
        @Path("id") bookingId: Long
    ): Response<BaseResponse<Booking>>
}