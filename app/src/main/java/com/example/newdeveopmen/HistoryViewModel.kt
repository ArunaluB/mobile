package com.example.newdeveopmen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class HistoryViewModel(private val repository: ScanHistoryRepository) : ViewModel() {
    
    private val _historyList = MutableLiveData<List<BookingHistory>>()
    val historyList: LiveData<List<BookingHistory>> = _historyList
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
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
            try {
                repository.updateHistoryStatus(reservationId, isCompleted = true, isCancelled = false)
                loadAllHistory() // Reload to update UI
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun cancelBooking(reservationId: String) {
        viewModelScope.launch {
            try {
                repository.updateHistoryStatus(reservationId, isCompleted = false, isCancelled = true)
                loadAllHistory() // Reload to update UI
            } catch (e: Exception) {
                e.printStackTrace()
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
