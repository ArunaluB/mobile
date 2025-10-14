package com.example.newdeveopmen

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ScanHistoryRepository(context: Context) {
    
    private val prefs = ScanHistoryPreferences(context)
    
    suspend fun insertHistory(history: BookingHistory) {
        withContext(Dispatchers.IO) {
            prefs.saveHistory(history)
        }
    }
    
    suspend fun getAllHistory(): List<BookingHistory> {
        return withContext(Dispatchers.IO) {
            prefs.getAllHistory()
        }
    }
    
    suspend fun getTodayHistory(): List<BookingHistory> {
        return withContext(Dispatchers.IO) {
            prefs.getTodayHistory()
        }
    }
    
    suspend fun getHistoryByStatus(status: String): List<BookingHistory> {
        return withContext(Dispatchers.IO) {
            prefs.getByStatus(status)
        }
    }
    
    suspend fun updateHistoryStatus(
        reservationId: String,
        isCompleted: Boolean,
        isCancelled: Boolean
    ) {
        withContext(Dispatchers.IO) {
            prefs.updateHistory(reservationId, isCompleted, isCancelled)
        }
    }
    
    suspend fun deleteHistory(reservationId: String) {
        withContext(Dispatchers.IO) {
            prefs.deleteHistory(reservationId)
        }
    }
}
