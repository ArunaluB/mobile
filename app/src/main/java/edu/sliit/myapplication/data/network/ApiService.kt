package edu.sliit.myapplication.data.network

import edu.sliit.myapplication.data.model.AvailableSlotsResponse
import edu.sliit.myapplication.data.model.ConfirmRequest
import edu.sliit.myapplication.data.model.QuickBookingRequest
import edu.sliit.myapplication.data.model.QuickBookingRequest2
import edu.sliit.myapplication.data.model.QuickBookingResponse
import edu.sliit.myapplication.data.model.QuickBookingResponse2
import edu.sliit.myapplication.data.model.ScanRequest
import edu.sliit.myapplication.data.model.ScanResponse
import edu.sliit.myapplication.data.model.SlotDetail
import edu.sliit.myapplication.data.model.SlotStatusResponse
import edu.sliit.myapplication.data.model.SlotStatusUpdateRequest
import edu.sliit.myapplication.models.LoginRequest
import edu.sliit.myapplication.models.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    
    @POST("api/Auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>
    
    @POST("api/Operator/scan")
    suspend fun scanQRCode(
        @Body request: ScanRequest
    ): Response<ScanResponse>
    
    @POST("api/Operator/scan/confirm")
    suspend fun confirmAction(
        @Body request: ConfirmRequest
    ): Response<ScanResponse>
    
    @POST("api/Bookings/{reservationId}/start")
    suspend fun startBooking(
        @Path("reservationId") reservationId: String
    ): Response<ScanResponse>
    
    @POST("api/Bookings/{reservationId}/complete")
    suspend fun endBooking(
        @Path("reservationId") reservationId: String
    ): Response<ScanResponse>
    
    @POST("api/Bookings/{reservationId}/cancel")
    suspend fun cancelBooking(
        @Path("reservationId") reservationId: String
    ): Response<ScanResponse>
    
    @POST("api/Bookings/{reservationId}/quick-cancel")
    suspend fun quickCancelBooking(
        @Path("reservationId") reservationId: String
    ): Response<ScanResponse>
    
    @POST("booking/create")
    suspend fun createQuickBooking(
        @Body request: QuickBookingRequest
    ): Response<QuickBookingResponse>
    
    // New endpoints for updated quick booking flow
    @GET("api/Bookings/available-slots")
    suspend fun getAvailableSlots(
        @Query("stationId") stationId: String,
        @Query("startUtc") startUtc: String,
        @Query("endUtc") endUtc: String
    ): Response<AvailableSlotsResponse>
    
    @POST("api/Bookings/quick")
    suspend fun createQuickBooking2(
        @Body request: QuickBookingRequest2
    ): Response<QuickBookingResponse2>
    
    // Slot Management Endpoints
    @GET("api/Bookings/available-slots")
    suspend fun getSlotStatus(
        @Query("stationId") stationId: String,
        @Query("startUtc") startUtc: String,
        @Query("endUtc") endUtc: String
    ): Response<SlotStatusResponse>
    
    @PATCH("api/Stations/{stationId}/slots/{slotId}/status")
    suspend fun updateSlotStatus(
        @Path("stationId") stationId: String,
        @Path("slotId") slotId: String,
        @Body request: SlotStatusUpdateRequest
    ): Response<SlotDetail>
}
