package edu.sliit.myapplication.data.repository

import edu.sliit.myapplication.data.model.QuickBookingRequest
import edu.sliit.myapplication.data.model.QuickBookingResponse
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
}
