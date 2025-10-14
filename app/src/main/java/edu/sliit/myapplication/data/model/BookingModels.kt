package edu.sliit.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class QuickBookingRequest(
    @SerializedName("slotId")
    val slotId: String
)

data class QuickBookingResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("bookingId")
    val bookingId: String?,
    
    @SerializedName("slotDetails")
    val slotDetails: SlotDetails?
)

data class SlotDetails(
    @SerializedName("slotId")
    val slotId: String,
    
    @SerializedName("stationName")
    val stationName: String,
    
    @SerializedName("startTime")
    val startTime: String,
    
    @SerializedName("endTime")
    val endTime: String,
    
    @SerializedName("isActive")
    val isActive: Boolean
)

data class TimeSlot(
    val id: String,
    val startTime: String,
    val endTime: String,
    val stationName: String,
    val isActive: Boolean,
    val isAvailable: Boolean
)
