package edu.sliit.myapplication.models

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("token")
    val token: String,
    
    @SerializedName("role")
    val role: String,
    
    @SerializedName("userId")
    val userId: String,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("userData")
    val userData: UserData
)

data class UserData(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("stationId")
    val stationId: String,
    
    @SerializedName("stationName")
    val stationName: String,
    
    @SerializedName("phone")
    val phone: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("active")
    val active: Boolean,
    
    @SerializedName("station")
    val station: Station
)

data class Station(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("location")
    val location: Location,
    
    @SerializedName("type")
    val type: String,
    
    @SerializedName("slots")
    val slots: List<Slot>,
    
    @SerializedName("active")
    val active: Boolean
)

data class Location(
    @SerializedName("lat")
    val lat: Double,
    
    @SerializedName("lng")
    val lng: Double
)

data class Slot(
    @SerializedName("slotId")
    val slotId: String,
    
    @SerializedName("label")
    val label: String,
    
    @SerializedName("active")
    val active: Boolean
)
