package edu.sliit.myapplication.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import edu.sliit.myapplication.models.LoginResponse

class UserPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "user_prefs"
        private const val KEY_LOGIN_RESPONSE = "login_response"
        private const val KEY_TOKEN = "token"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    fun saveLoginResponse(loginResponse: LoginResponse) {
        val json = gson.toJson(loginResponse)
        android.util.Log.d("UserPreferences", "========== SAVING LOGIN RESPONSE ==========")
        android.util.Log.d("UserPreferences", "Username: ${loginResponse.username}")
        android.util.Log.d("UserPreferences", "Role: ${loginResponse.role}")
        android.util.Log.d("UserPreferences", "UserId: ${loginResponse.userId}")
        android.util.Log.d("UserPreferences", "UserData: ${loginResponse.userData}")
        android.util.Log.d("UserPreferences", "Station Name: ${loginResponse.userData?.stationName}")
        android.util.Log.d("UserPreferences", "Email: ${loginResponse.userData?.email}")
        android.util.Log.d("UserPreferences", "Phone: ${loginResponse.userData?.phone}")
        android.util.Log.d("UserPreferences", "JSON to save: $json")
        
        val editor = sharedPreferences.edit()
        editor.putString(KEY_LOGIN_RESPONSE, json)
        editor.putString(KEY_TOKEN, loginResponse.token)
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        val success = editor.commit() // Use commit() instead of apply() to ensure immediate save
        
        android.util.Log.d("UserPreferences", "Save successful: $success")
        android.util.Log.d("UserPreferences", "=========================================")
    }

    fun getLoginResponse(): LoginResponse? {
        val json = sharedPreferences.getString(KEY_LOGIN_RESPONSE, null)
        android.util.Log.d("UserPreferences", "========== RETRIEVING LOGIN RESPONSE ==========")
        android.util.Log.d("UserPreferences", "Retrieved JSON: $json")
        
        return if (json != null) {
            try {
                val response = gson.fromJson(json, LoginResponse::class.java)
                android.util.Log.d("UserPreferences", "Parsed Username: ${response.username}")
                android.util.Log.d("UserPreferences", "Parsed Role: ${response.role}")
                android.util.Log.d("UserPreferences", "Parsed UserData: ${response.userData}")
                android.util.Log.d("UserPreferences", "Parsed Station Name: ${response.userData?.stationName}")
                android.util.Log.d("UserPreferences", "Parsed Email: ${response.userData?.email}")
                android.util.Log.d("UserPreferences", "Parsed Phone: ${response.userData?.phone}")
                android.util.Log.d("UserPreferences", "==============================================")
                response
            } catch (e: Exception) {
                android.util.Log.e("UserPreferences", "Error parsing JSON: ${e.message}", e)
                null
            }
        } else {
            android.util.Log.w("UserPreferences", "No login response found in SharedPreferences")
            android.util.Log.d("UserPreferences", "==============================================")
            null
        }
    }

    fun getToken(): String? {
        return sharedPreferences.getString(KEY_TOKEN, null)
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun clearUserData() {
        sharedPreferences.edit().clear().apply()
    }
}
