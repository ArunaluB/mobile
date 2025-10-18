package com.example.newdeveopmen

import android.util.Log
import edu.sliit.myapplication.data.network.RetrofitClient

class HistoryBookingRepository {
    
    private val apiService = RetrofitClient.apiService
    private val TAG = "HistoryBookingRepo"
    
    suspend fun completeBooking(bookingId: String): Result<Any> {
        return try {
            Log.d(TAG, "Completing booking: $bookingId")
            val response = apiService.endBooking(bookingId)
            
            if (response.isSuccessful) {
                Log.d(TAG, "✓ Booking completed successfully")
                Result.success(Any())
            } else {
                val errorMsg = "Complete failed: ${response.message()}"
                Log.e(TAG, errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error completing booking", e)
            Result.failure(Exception("Error: ${e.message ?: "Unknown error"}"))
        }
    }
    
    suspend fun quickCancelBooking(bookingId: String): Result<Any> {
        return try {
            Log.d(TAG, "Quick cancelling booking: $bookingId")
            val response = apiService.quickCancelBooking(bookingId)
            
            if (response.isSuccessful) {
                Log.d(TAG, "✓ Booking cancelled successfully")
                Result.success(Any())
            } else {
                val errorMsg = "Cancel failed: ${response.message()}"
                Log.e(TAG, errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling booking", e)
            Result.failure(Exception("Error: ${e.message ?: "Unknown error"}"))
        }
    }
}
