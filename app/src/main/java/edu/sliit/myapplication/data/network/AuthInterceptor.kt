package edu.sliit.myapplication.data.network

import android.content.Context
import android.util.Log
import edu.sliit.myapplication.utils.UserPreferences
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {
    
    companion object {
        private const val TAG = "AuthInterceptor"
    }
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val url = originalRequest.url.toString()
        
        // Skip adding token for login endpoint
        if (url.contains("/api/Auth/login")) {
            Log.d(TAG, "Skipping auth header for login endpoint")
            return chain.proceed(originalRequest)
        }
        
        // Get token from UserPreferences
        val userPreferences = UserPreferences(context)
        val token = userPreferences.getToken()
        
        // Add token to request if available
        val newRequest = if (token != null) {
            Log.d(TAG, "Adding Authorization header with token: ${token.take(20)}...")
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            Log.w(TAG, "No token found, proceeding without Authorization header")
            originalRequest
        }
        
        return chain.proceed(newRequest)
    }
}
