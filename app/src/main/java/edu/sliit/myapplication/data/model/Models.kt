package edu.sliit.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class ScanRequest(
    @SerializedName("token")
    val token: String
)

data class ScanResponse(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("ownerNic")
    val ownerNic: String?,
    
    @SerializedName("stationId")
    val stationId: String,
    
    @SerializedName("startUtc")
    val startUtc: String,
    
    @SerializedName("endUtc")
    val endUtc: String,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("startedUtc")
    val startedUtc: String?,
    
    @SerializedName("endedUtc")
    val endedUtc: String?,
    
    @SerializedName("nextActions")
    val nextActions: List<String>
) {
    // Helper properties for backward compatibility
    val reservationId: String get() = id
    val startTimeUtc: String get() = startUtc
    val endTimeUtc: String get() = endUtc
}

data class ConfirmRequest(
    @SerializedName("token")
    val token: String,
    
    @SerializedName("action")
    val action: String
)

data class BookingHistory(
    val reservationId: String,
    val stationId: String,
    val status: String,
    val startTime: String,
    val endTime: String,
    val scannedAt: Long,
    val isCompleted: Boolean,
    val isCancelled: Boolean
)
