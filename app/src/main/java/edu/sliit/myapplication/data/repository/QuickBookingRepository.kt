package edu.sliit.myapplication.data.repository

import edu.sliit.myapplication.data.model.AvailableSlotsResponse
import edu.sliit.myapplication.data.model.QuickBookingRequest
import edu.sliit.myapplication.data.model.QuickBookingRequest2
import edu.sliit.myapplication.data.model.QuickBookingResponse
import edu.sliit.myapplication.data.model.QuickBookingResponse2
import edu.sliit.myapplication.data.network.RetrofitClient

class QuickBookingRepository {
    
    private val apiService = RetrofitClient.apiService
    
    suspend fun createQuickBooking(slotId: String): Result<QuickBookingResponse> {
        return try {
            val response = apiService.createQuickBooking(QuickBookingRequest(slotId))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Booking failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // New methods for updated quick booking flow
    suspend fun getAvailableSlots(
        stationId: String,
        startUtc: String,
        endUtc: String
    ): Result<AvailableSlotsResponse> {
        return try {
            val response = apiService.getAvailableSlots(stationId, startUtc, endUtc)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch slots: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createQuickBooking2(
        stationId: String,
        slotId: String,
        startUtc: String,
        endUtc: String,
        ownerNic: String
    ): Result<QuickBookingResponse2> {
        return try {
            val request = QuickBookingRequest2(stationId, slotId, startUtc, endUtc, ownerNic)
            val response = apiService.createQuickBooking2(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Booking failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun startBooking(bookingId: String): Result<Any> {
        return try {
            val response = apiService.startBooking(bookingId)
            if (response.isSuccessful) {
                Result.success(Any())
            } else {
                Result.failure(Exception("Failed to start booking: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
