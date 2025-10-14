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
        sharedPreferences.edit().apply {
            putString(KEY_LOGIN_RESPONSE, json)
            putString(KEY_TOKEN, loginResponse.token)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    fun getLoginResponse(): LoginResponse? {
        val json = sharedPreferences.getString(KEY_LOGIN_RESPONSE, null)
        return if (json != null) {
            gson.fromJson(json, LoginResponse::class.java)
        } else {
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
