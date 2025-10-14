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
    
    fun scanQRCode(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _currentToken.value = token
            val result = repository.scanQRCode(token)
            _scanResult.value = result
            _isLoading.value = false
        }
    }
    
    fun confirmAction(action: String) {
        val token = _currentToken.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.confirmAction(token, action)
            _confirmResult.value = result
            _isLoading.value = false
        }
    }
    
    fun clearResults() {
        _scanResult.value = null
        _confirmResult.value = null
        _currentToken.value = null
    }
}
