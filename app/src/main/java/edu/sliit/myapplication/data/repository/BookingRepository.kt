package edu.sliit.myapplication.data.repository

import android.util.Log
import edu.sliit.myapplication.data.model.ConfirmRequest
import edu.sliit.myapplication.data.model.ScanRequest
import edu.sliit.myapplication.data.model.ScanResponse
import edu.sliit.myapplication.data.network.RetrofitClient
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class BookingRepository {
    
    private val apiService = RetrofitClient.apiService
    private val TAG = "BookingRepository"
    
    suspend fun scanQRCode(token: String): Result<ScanResponse> {
        return try {
            Log.d(TAG, "Scanning QR code...")
            val response = apiService.scanQRCode(ScanRequest(token))
            
            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "✓ Scan successful: ${response.body()!!.id}")
                Result.success(response.body()!!)
            } else {
                val errorMsg = when (response.code()) {
                    400 -> "Invalid QR code token"
                    401 -> "Unauthorized - Please login again"
                    404 -> "Booking not found"
                    500 -> "Server error - Please try again"
                    else -> "Scan failed: ${response.message()}"
                }
                Log.e(TAG, "Scan failed (${response.code()}): $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: UnknownHostException) {
            Log.e(TAG, "Network error: Cannot reach server", e)
            Result.failure(Exception("Cannot connect to server. Check your network connection"))
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Timeout error", e)
            Result.failure(Exception("Request timeout. Please try again"))
        } catch (e: ConnectException) {
            Log.e(TAG, "Connection error", e)
            Result.failure(Exception("Connection failed. Check server is running"))
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during scan", e)
            Result.failure(Exception("Error: ${e.message ?: "Unknown error"}"))
        }
    }
    
    suspend fun confirmAction(token: String, action: String): Result<ScanResponse> {
        return try {
            Log.d(TAG, "Confirming action: $action")
            val response = apiService.confirmAction(ConfirmRequest(token, action))
            
            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "✓ Action confirmed: $action")
                Result.success(response.body()!!)
            } else {
                val errorMsg = "Confirmation failed (${response.code()}): ${response.message()}"
                Log.e(TAG, errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error confirming action", e)
            Result.failure(Exception("Error: ${e.message ?: "Unknown error"}"))
        }
    }
    
    suspend fun startBooking(reservationId: String): Result<ScanResponse> {
        return try {
            Log.d(TAG, "Starting booking: $reservationId")
            val response = apiService.startBooking(reservationId)
            
            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "✓ Booking started successfully")
                Result.success(response.body()!!)
            } else {
                val errorMsg = when (response.code()) {
                    400 -> "Cannot start booking - Invalid state"
                    401 -> "Unauthorized - Please login again"
                    404 -> "Booking not found"
                    409 -> "Booking already started"
                    else -> "Start failed: ${response.message()}"
                }
                Log.e(TAG, "Start booking failed (${response.code()}): $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting booking", e)
            Result.failure(Exception("Error: ${e.message ?: "Unknown error"}"))
        }
    }
    
    suspend fun endBooking(reservationId: String): Result<ScanResponse> {
        return try {
            Log.d(TAG, "Completing booking: $reservationId")
            val response = apiService.endBooking(reservationId)
            
            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "✓ Booking completed successfully")
                Result.success(response.body()!!)
            } else {
                val errorMsg = when (response.code()) {
                    400 -> "Cannot complete booking - Invalid state"
                    401 -> "Unauthorized - Please login again"
                    404 -> "Booking not found"
                    409 -> "Booking already completed"
                    else -> "Complete failed: ${response.message()}"
                }
                Log.e(TAG, "Complete booking failed (${response.code()}): $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error completing booking", e)
            Result.failure(Exception("Error: ${e.message ?: "Unknown error"}"))
        }
    }
    
    suspend fun cancelBooking(reservationId: String): Result<ScanResponse> {
        return try {
            Log.d(TAG, "Cancelling booking: $reservationId")
            val response = apiService.cancelBooking(reservationId)
            
            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "✓ Booking cancelled successfully")
                Result.success(response.body()!!)
            } else {
                val errorMsg = when (response.code()) {
                    400 -> "Cannot cancel booking - Invalid state"
                    401 -> "Unauthorized - Please login again"
                    404 -> "Booking not found"
                    409 -> "Booking already cancelled"
                    else -> "Cancel failed: ${response.message()}"
                }
                Log.e(TAG, "Cancel booking failed (${response.code()}): $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling booking", e)
            Result.failure(Exception("Error: ${e.message ?: "Unknown error"}"))
        }
    }
}
