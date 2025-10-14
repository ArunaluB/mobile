package edu.sliit.myapplication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.sliit.myapplication.data.model.QuickBookingResponse
import edu.sliit.myapplication.data.model.TimeSlot
import edu.sliit.myapplication.data.repository.QuickBookingRepository
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    
    private val repository = QuickBookingRepository()
    
    private val _bookingResult = MutableLiveData<Result<QuickBookingResponse>>()
    val bookingResult: LiveData<Result<QuickBookingResponse>> = _bookingResult
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _timeSlots = MutableLiveData<List<TimeSlot>>()
    val timeSlots: LiveData<List<TimeSlot>> = _timeSlots
    
    init {
        loadDemoTimeSlots()
    }
    
    fun createQuickBooking(slotId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.createQuickBooking(slotId)
            _bookingResult.value = result
            _isLoading.value = false
        }
    }
    
    fun loadDemoTimeSlots() {
        // Demo time slots - In production, this would come from an API
        val demoSlots = listOf(
            TimeSlot(
                id = "SLT-001",
                startTime = "08:00 AM",
                endTime = "09:00 AM",
                stationName = "Station Alpha",
                isActive = true,
                isAvailable = true
            ),
            TimeSlot(
                id = "SLT-002",
                startTime = "09:00 AM",
                endTime = "10:00 AM",
                stationName = "Station Beta",
                isActive = true,
                isAvailable = true
            ),
            TimeSlot(
                id = "SLT-003",
                startTime = "10:00 AM",
                endTime = "11:00 AM",
                stationName = "Station Gamma",
                isActive = true,
                isAvailable = false
            ),
            TimeSlot(
                id = "SLT-004",
                startTime = "11:00 AM",
                endTime = "12:00 PM",
                stationName = "Station Delta",
                isActive = true,
                isAvailable = true
            ),
            TimeSlot(
                id = "SLT-005",
                startTime = "12:00 PM",
                endTime = "01:00 PM",
                stationName = "Station Epsilon",
                isActive = false,
                isAvailable = true
            ),
            TimeSlot(
                id = "SLT-006",
                startTime = "01:00 PM",
                endTime = "02:00 PM",
                stationName = "Station Zeta",
                isActive = true,
                isAvailable = true
            ),
            TimeSlot(
                id = "SLT-007",
                startTime = "02:00 PM",
                endTime = "03:00 PM",
                stationName = "Station Eta",
                isActive = true,
                isAvailable = true
            ),
            TimeSlot(
                id = "SLT-008",
                startTime = "03:00 PM",
                endTime = "04:00 PM",
                stationName = "Station Theta",
                isActive = true,
                isAvailable = false
            )
        )
        _timeSlots.value = demoSlots
    }
    
    fun refreshTimeSlots() {
        loadDemoTimeSlots()
    }
}
