package edu.sliit.myapplication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.sliit.myapplication.data.model.AvailableSlot
import edu.sliit.myapplication.data.model.QuickBookingResponse
import edu.sliit.myapplication.data.model.QuickBookingResponse2
import edu.sliit.myapplication.data.model.SlotDetail
import edu.sliit.myapplication.data.model.TimeSlot
import edu.sliit.myapplication.data.repository.QuickBookingRepository
import edu.sliit.myapplication.data.repository.SlotManagementRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class HomeViewModel : ViewModel() {
    
    private val repository = QuickBookingRepository()
    private val slotRepository = SlotManagementRepository()
    
    private val _bookingResult = MutableLiveData<Result<QuickBookingResponse>>()
    val bookingResult: LiveData<Result<QuickBookingResponse>> = _bookingResult
    
    private val _bookingResult2 = MutableLiveData<Result<QuickBookingResponse2>>()
    val bookingResult2: LiveData<Result<QuickBookingResponse2>> = _bookingResult2
    
    private val _availableSlots = MutableLiveData<List<AvailableSlot>>()
    val availableSlots: LiveData<List<AvailableSlot>> = _availableSlots
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _timeSlots = MutableLiveData<List<TimeSlot>>()
    val timeSlots: LiveData<List<TimeSlot>> = _timeSlots
    
    private val _startBookingResult = MutableLiveData<Result<Any>>()
    val startBookingResult: LiveData<Result<Any>> = _startBookingResult
    
    // Slot Management LiveData
    private val _slotStatus = MutableLiveData<List<SlotDetail>>()
    val slotStatus: LiveData<List<SlotDetail>> = _slotStatus
    
    private val _slotUpdateResult = MutableLiveData<Result<SlotDetail>>()
    val slotUpdateResult: LiveData<Result<SlotDetail>> = _slotUpdateResult
    
    private val _slotStationName = MutableLiveData<String>()
    val slotStationName: LiveData<String> = _slotStationName
    
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
    
    fun fetchAvailableSlots(stationId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            
            // Get today's date in UTC
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            
            val startUtc = dateFormat.format(calendar.time)
            
            // Get tomorrow's date
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val endUtc = dateFormat.format(calendar.time)
            
            val result = repository.getAvailableSlots(stationId, startUtc, endUtc)
            result.onSuccess { response ->
                _availableSlots.value = response.slots
            }.onFailure { error ->
                _availableSlots.value = emptyList()
                // You can add error handling here
            }
            
            _isLoading.value = false
        }
    }
    
    fun createQuickBooking2(
        stationId: String,
        slotId: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            
            // Get today's date in UTC
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            
            val startUtc = dateFormat.format(calendar.time)
            
            // Get tomorrow's date
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val endUtc = dateFormat.format(calendar.time)
            
            val ownerNic = "0000000"
            
            val result = repository.createQuickBooking2(stationId, slotId, startUtc, endUtc, ownerNic)
            _bookingResult2.value = result
            _isLoading.value = false
        }
    }
    
    fun startBooking(bookingId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.startBooking(bookingId)
            _startBookingResult.value = result
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
    
    // ========== Slot Management Methods ==========
    
    /**
     * Fetch slot status from API
     * GET /api/bookings/slots-status
     */
    fun fetchSlotStatus(stationId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            
            // Get today's date in UTC
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            
            val startUtc = dateFormat.format(calendar.time)
            
            // Get end date (1 day from now)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val endUtc = dateFormat.format(calendar.time)
            
            val result = slotRepository.getSlotStatus(stationId, startUtc, endUtc)
            result.onSuccess { response ->
                _slotStatus.value = response.slots
                _slotStationName.value = "Station: $stationId"
            }.onFailure {
                _slotStatus.value = emptyList()
                _slotStationName.value = "Error loading station"
            }
            
            _isLoading.value = false
        }
    }
    
    /**
     * Toggle slot activation status based on availability
     * If available=true → Send active=false (deactivate/make unavailable)
     * If available=false → Send active=true (activate/make available)
     */
    fun toggleSlotStatus(stationId: String, slotId: String, currentlyAvailable: Boolean) {
        viewModelScope.launch {
            val result = slotRepository.toggleSlotStatus(stationId, slotId, currentlyAvailable)
            _slotUpdateResult.value = result
            
            // Refresh slot status after update
            if (result.isSuccess) {
                fetchSlotStatus(stationId)
            }
        }
    }
    
    /**
     * Activate a specific slot
     */
    fun activateSlot(stationId: String, slotId: String) {
        viewModelScope.launch {
            val result = slotRepository.activateSlot(stationId, slotId)
            _slotUpdateResult.value = result
            
            if (result.isSuccess) {
                fetchSlotStatus(stationId)
            }
        }
    }
    
    /**
     * Deactivate a specific slot
     */
    fun deactivateSlot(stationId: String, slotId: String) {
        viewModelScope.launch {
            val result = slotRepository.deactivateSlot(stationId, slotId)
            _slotUpdateResult.value = result
            
            if (result.isSuccess) {
                fetchSlotStatus(stationId)
            }
        }
    }
}
