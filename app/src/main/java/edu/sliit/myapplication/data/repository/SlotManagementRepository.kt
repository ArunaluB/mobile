package edu.sliit.myapplication.data.repository

import android.util.Log
import edu.sliit.myapplication.data.model.SlotDetail
import edu.sliit.myapplication.data.model.SlotStatusResponse
import edu.sliit.myapplication.data.model.SlotStatusUpdateRequest
import edu.sliit.myapplication.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SlotManagementRepository {
    
    private val apiService = RetrofitClient.apiService
    
    companion object {
        private const val TAG = "SlotManagementRepository"
    }
    
    /**
     * Get slot status for a station
     * GET /api/bookings/slots-status?stationId={id}&startUtc={start}&endUtc={end}
     */
    suspend fun getSlotStatus(
        stationId: String,
        startUtc: String,
        endUtc: String
    ): Result<SlotStatusResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "========================================")
            Log.d(TAG, "Fetching slot status for station: $stationId")
            Log.d(TAG, "Time range: $startUtc to $endUtc")
            Log.d(TAG, "API Endpoint: api/Bookings/available-slots")
            
            val response = apiService.getSlotStatus(stationId, startUtc, endUtc)
            
            Log.d(TAG, "Response Code: ${response.code()}")
            Log.d(TAG, "Response Message: ${response.message()}")
            
            if (response.isSuccessful && response.body() != null) {
                val slotStatus = response.body()!!
                Log.d(TAG, "✓ Slot status fetched successfully")
                Log.d(TAG, "Station ID: ${slotStatus.stationId}")
                Log.d(TAG, "Total slots: ${slotStatus.slots.size}")
                slotStatus.slots.forEachIndexed { index, slot ->
                    Log.d(TAG, "  Slot $index: ${slot.slotId} - ${slot.label} - Available: ${slot.available}, Active: ${slot.active}")
                }
                Log.d(TAG, "========================================")
                Result.success(slotStatus)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Failed to fetch slot status: ${response.code()} - ${response.message()}"
                Log.e(TAG, errorMsg)
                Log.e(TAG, "Error Body: $errorBody")
                Log.d(TAG, "========================================")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching slot status: ${e.message}", e)
            Log.e(TAG, "Exception type: ${e.javaClass.simpleName}")
            e.printStackTrace()
            Log.d(TAG, "========================================")
            Result.failure(e)
        }
    }
    
    /**
     * Activate a slot (make it available for booking)
     * PATCH /api/Stations/{stationId}/slots/{slotId}/status
     * Body: { "active": true }
     */
    suspend fun activateSlot(
        stationId: String,
        slotId: String
    ): Result<SlotDetail> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Activating slot: $slotId at station: $stationId")
            Log.d(TAG, "API: PATCH /api/Stations/$stationId/slots/$slotId/status")
            Log.d(TAG, "Body: { \"active\": true }")
            
            val request = SlotStatusUpdateRequest(active = true)
            val response = apiService.updateSlotStatus(stationId, slotId, request)
            
            if (response.isSuccessful && response.body() != null) {
                val updatedSlot = response.body()!!
                Log.d(TAG, "✓ Slot activated successfully: ${updatedSlot.slotId}")
                Result.success(updatedSlot)
            } else {
                val errorMsg = "Failed to activate slot: ${response.code()} - ${response.message()}"
                Log.e(TAG, errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error activating slot", e)
            Result.failure(e)
        }
    }
    
    /**
     * Deactivate a slot (make it unavailable for booking)
     * PATCH /api/Stations/{stationId}/slots/{slotId}/status
     * Body: { "active": false }
     */
    suspend fun deactivateSlot(
        stationId: String,
        slotId: String
    ): Result<SlotDetail> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Deactivating slot: $slotId at station: $stationId")
            Log.d(TAG, "API: PATCH /api/Stations/$stationId/slots/$slotId/status")
            Log.d(TAG, "Body: { \"active\": false }")
            
            val request = SlotStatusUpdateRequest(active = false)
            val response = apiService.updateSlotStatus(stationId, slotId, request)
            
            if (response.isSuccessful && response.body() != null) {
                val updatedSlot = response.body()!!
                Log.d(TAG, "✓ Slot deactivated successfully: ${updatedSlot.slotId}")
                Result.success(updatedSlot)
            } else {
                val errorMsg = "Failed to deactivate slot: ${response.code()} - ${response.message()}"
                Log.e(TAG, errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deactivating slot", e)
            Result.failure(e)
        }
    }
    
    /**
     * Toggle slot status based on current availability
     * If available (true) → Deactivate (set active: false) to make unavailable
     * If unavailable (false) → Activate (set active: true) to make available
     */
    suspend fun toggleSlotStatus(
        stationId: String,
        slotId: String,
        currentlyAvailable: Boolean
    ): Result<SlotDetail> {
        return if (currentlyAvailable) {
            // Slot is currently available → Make it unavailable
            Log.d(TAG, "Slot is available → Deactivating (active: false)")
            deactivateSlot(stationId, slotId)
        } else {
            // Slot is currently unavailable → Make it available
            Log.d(TAG, "Slot is unavailable → Activating (active: true)")
            activateSlot(stationId, slotId)
        }
    }
}
