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

// New models for the updated API
data class AvailableSlotsResponse(
    @SerializedName("stationId")
    val stationId: String,
    
    @SerializedName("startUtc")
    val startUtc: String,
    
    @SerializedName("endUtc")
    val endUtc: String,
    
    @SerializedName("slots")
    val slots: List<AvailableSlot>
)

data class AvailableSlot(
    @SerializedName("slotId")
    val slotId: String,
    
    @SerializedName("label")
    val label: String,
    
    @SerializedName("available")
    val available: Boolean
)

data class QuickBookingRequest2(
    @SerializedName("stationId")
    val stationId: String,
    
    @SerializedName("slotId")
    val slotId: String,
    
    @SerializedName("startUtc")
    val startUtc: String,
    
    @SerializedName("endUtc")
    val endUtc: String,
    
    @SerializedName("ownerNic")
    val ownerNic: String
)

data class QuickBookingResponse2(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("ownerNic")
    val ownerNic: String,
    
    @SerializedName("stationId")
    val stationId: String,
    
    @SerializedName("stationName")
    val stationName: String?,
    
    @SerializedName("slotId")
    val slotId: String,
    
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
    
    @SerializedName("bookingToken")
    val bookingToken: String?,
    
    @SerializedName("nextActions")
    val nextActions: List<String>
)

// Slot Management Models
data class SlotStatusResponse(
    @SerializedName("stationId")
    val stationId: String,
    
    @SerializedName("startUtc")
    val startUtc: String,
    
    @SerializedName("endUtc")
    val endUtc: String,
    
    @SerializedName("slots")
    val slots: List<SlotDetail>
)

data class SlotDetail(
    @SerializedName("slotId")
    val slotId: String,
    
    @SerializedName("label")
    val label: String,
    
    @SerializedName("available")
    val available: Boolean,
    
    @SerializedName("active")
    val active: Boolean = true
)

data class SlotStatusUpdateRequest(
    @SerializedName("active")
    val active: Boolean
)
