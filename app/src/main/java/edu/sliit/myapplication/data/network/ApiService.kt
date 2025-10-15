package edu.sliit.myapplication.data.network

import edu.sliit.myapplication.data.model.ConfirmRequest
import edu.sliit.myapplication.data.model.QuickBookingRequest
import edu.sliit.myapplication.data.model.QuickBookingResponse
import edu.sliit.myapplication.data.model.ScanRequest
import edu.sliit.myapplication.data.model.ScanResponse
import edu.sliit.myapplication.models.LoginRequest
import edu.sliit.myapplication.models.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

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
    
    @POST("booking/create")
    suspend fun createQuickBooking(
        @Body request: QuickBookingRequest
    ): Response<QuickBookingResponse>
}
