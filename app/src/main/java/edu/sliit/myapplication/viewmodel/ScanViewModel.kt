package edu.sliit.myapplication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.sliit.myapplication.data.model.ScanResponse
import edu.sliit.myapplication.data.repository.BookingRepository
import kotlinx.coroutines.launch

class ScanViewModel : ViewModel() {
    
    private val repository = BookingRepository()
    
    private val _scanResult = MutableLiveData<Result<ScanResponse>>()
    val scanResult: LiveData<Result<ScanResponse>> = _scanResult
    
    private val _confirmResult = MutableLiveData<Result<ScanResponse>>()
    val confirmResult: LiveData<Result<ScanResponse>> = _confirmResult
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _currentToken = MutableLiveData<String>()
    val currentToken: LiveData<String> = _currentToken
    
    private val _currentReservationId = MutableLiveData<String>()
    val currentReservationId: LiveData<String> = _currentReservationId
    
    fun scanQRCode(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _currentToken.value = token
            val result = repository.scanQRCode(token)
            
            // Store reservation ID from response
            result.getOrNull()?.let { response ->
                _currentReservationId.value = response.id
            }
            
            _scanResult.value = result
            _isLoading.value = false
        }
    }
    
    fun confirmAction(action: String) {
        val reservationId = _currentReservationId.value
        
        android.util.Log.d("ScanViewModel", "confirmAction called with action='$action', reservationId='$reservationId'")
        
        if (reservationId.isNullOrEmpty()) {
            android.util.Log.e("ScanViewModel", "No reservation ID found!")
            _confirmResult.value = Result.failure(Exception("No reservation ID found"))
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            
            val result = when (action.lowercase()) {
                "start" -> {
                    android.util.Log.d("ScanViewModel", "Calling startBooking for ID: $reservationId")
                    repository.startBooking(reservationId)
                }
                "end", "complete" -> {
                    android.util.Log.d("ScanViewModel", "Calling endBooking (complete) for ID: $reservationId")
                    repository.endBooking(reservationId)
                }
                "cancel" -> {
                    android.util.Log.d("ScanViewModel", "Calling cancelBooking for ID: $reservationId")
                    repository.cancelBooking(reservationId)
                }
                else -> {
                    android.util.Log.d("ScanViewModel", "Using fallback confirmAction for action: $action")
                    // Fallback to old confirm action for backward compatibility
                    val token = _currentToken.value ?: ""
                    repository.confirmAction(token, action)
                }
            }
            
            _confirmResult.value = result
            _isLoading.value = false
            
            android.util.Log.d("ScanViewModel", "confirmAction result: ${if (result.isSuccess) "SUCCESS" else "FAILURE"}")
        }
    }
    
    fun clearResults() {
        _scanResult.value = null
        _confirmResult.value = null
        _currentToken.value = null
        _currentReservationId.value = null
    }
}
