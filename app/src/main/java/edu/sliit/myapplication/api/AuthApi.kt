package edu.sliit.myapplication.api

import edu.sliit.myapplication.models.LoginRequest
import edu.sliit.myapplication.models.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface AuthApi {
    @Headers("Content-Type: application/json")
    @POST("api/Auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>
}
