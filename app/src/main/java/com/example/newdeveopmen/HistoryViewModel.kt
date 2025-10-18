package com.example.newdeveopmen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class HistoryViewModel(private val repository: ScanHistoryRepository) : ViewModel() {
    
    private val bookingRepository = HistoryBookingRepository()
    
    private val _historyList = MutableLiveData<List<BookingHistory>>()
    val historyList: LiveData<List<BookingHistory>> = _historyList
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _actionResult = MutableLiveData<Result<String>>()
    val actionResult: LiveData<Result<String>> = _actionResult
    
    init {
        loadAllHistory()
    }
    
    fun loadAllHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val history = repository.getAllHistory()
                _historyList.value = history
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadTodayHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val history = repository.getTodayHistory()
                _historyList.value = history
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadHistoryByStatus(status: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val history = repository.getHistoryByStatus(status)
                _historyList.value = history
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun completeBooking(reservationId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Call API to complete booking
                val result = bookingRepository.completeBooking(reservationId)
                
                if (result.isSuccess) {
                    // Remove from history after successful completion
                    repository.deleteHistory(reservationId)
                    _actionResult.value = Result.success("✅ Booking completed successfully!")
                    loadAllHistory() // Reload to update UI
                } else {
                    _actionResult.value = Result.failure(
                        result.exceptionOrNull() ?: Exception("Failed to complete booking")
                    )
                }
            } catch (e: Exception) {
                _actionResult.value = Result.failure(e)
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun cancelBooking(reservationId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Call API to cancel booking
                val result = bookingRepository.quickCancelBooking(reservationId)
                
                if (result.isSuccess) {
                    // Remove from history after successful cancellation
                    repository.deleteHistory(reservationId)
                    _actionResult.value = Result.success("✅ Booking cancelled successfully!")
                    loadAllHistory() // Reload to update UI
                } else {
                    _actionResult.value = Result.failure(
                        result.exceptionOrNull() ?: Exception("Failed to cancel booking")
                    )
                }
            } catch (e: Exception) {
                _actionResult.value = Result.failure(e)
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteBooking(reservationId: String) {
        viewModelScope.launch {
            try {
                repository.deleteHistory(reservationId)
                loadAllHistory() // Reload to update UI
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
