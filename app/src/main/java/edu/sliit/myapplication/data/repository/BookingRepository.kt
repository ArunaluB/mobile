package edu.sliit.myapplication.data.repository

import edu.sliit.myapplication.data.model.ConfirmRequest
import edu.sliit.myapplication.data.model.ScanRequest
import edu.sliit.myapplication.data.model.ScanResponse
import edu.sliit.myapplication.data.network.RetrofitClient

class BookingRepository {
    
    private val apiService = RetrofitClient.apiService
    
    suspend fun scanQRCode(token: String): Result<ScanResponse> {
        return try {
            val response = apiService.scanQRCode(ScanRequest(token))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Scan failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun confirmAction(token: String, action: String): Result<ScanResponse> {
        return try {
            val response = apiService.confirmAction(ConfirmRequest(token, action))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Confirmation failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
