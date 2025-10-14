package edu.sliit.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class ScanRequest(
    @SerializedName("token")
    val token: String
)

data class ScanResponse(
    @SerializedName("reservationId")
    val reservationId: String,
    
    @SerializedName("stationId")
    val stationId: String,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("startTimeUtc")
    val startTimeUtc: String,
    
    @SerializedName("endTimeUtc")
    val endTimeUtc: String,
    
    @SerializedName("ownerNic")
    val ownerNic: String?,
    
    @SerializedName("nextActions")
    val nextActions: List<String>
)

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
