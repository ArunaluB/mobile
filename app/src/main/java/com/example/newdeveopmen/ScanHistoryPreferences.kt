package com.example.newdeveopmen

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

data class BookingHistory(
    val id: String = UUID.randomUUID().toString(),
    val reservationId: String,
    val stationId: String,
    val status: String,
    val ownerNic: String,
    val startTime: String,
    val endTime: String,
    val scanTime: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val isCancelled: Boolean = false
)

class ScanHistoryPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("scan_history", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val KEY_HISTORY = "history_list"
    }
    
    fun saveHistory(history: BookingHistory) {
        val list = getAllHistory().toMutableList()
        // Check if already exists and update, otherwise add
        val existingIndex = list.indexOfFirst { it.reservationId == history.reservationId }
        if (existingIndex != -1) {
            list[existingIndex] = history
        } else {
            list.add(0, history) // Add to top
        }
        saveList(list)
    }
    
    fun getAllHistory(): List<BookingHistory> {
        val json = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        val type = object : TypeToken<List<BookingHistory>>() {}.type
        return gson.fromJson(json, type)
    }
    
    fun getTodayHistory(): List<BookingHistory> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        
        return getAllHistory().filter { it.scanTime >= startOfDay }
    }
    
    fun getByStatus(status: String): List<BookingHistory> {
        return when (status.lowercase()) {
            "completed" -> getAllHistory().filter { it.isCompleted }
            "cancelled" -> getAllHistory().filter { it.isCancelled }
            "in-progress" -> getAllHistory().filter { !it.isCompleted && !it.isCancelled }
            else -> getAllHistory()
        }
    }
    
    fun updateHistory(reservationId: String, isCompleted: Boolean = false, isCancelled: Boolean = false) {
        val list = getAllHistory().toMutableList()
        val index = list.indexOfFirst { it.reservationId == reservationId }
        if (index != -1) {
            list[index] = list[index].copy(
                isCompleted = isCompleted,
                isCancelled = isCancelled
            )
            saveList(list)
        }
    }
    
    fun deleteHistory(reservationId: String) {
        val list = getAllHistory().toMutableList()
        list.removeAll { it.reservationId == reservationId }
        saveList(list)
    }
    
    fun clearAll() {
        prefs.edit().clear().apply()
    }
    
    private fun saveList(list: List<BookingHistory>) {
        val json = gson.toJson(list)
        prefs.edit().putString(KEY_HISTORY, json).apply()
    }
}
