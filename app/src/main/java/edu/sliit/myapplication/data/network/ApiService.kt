package edu.sliit.myapplication.data.network

import edu.sliit.myapplication.data.model.ConfirmRequest
import edu.sliit.myapplication.data.model.ScanRequest
import edu.sliit.myapplication.data.model.ScanResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    
    @POST("api/Operator/scan")
    suspend fun scanQRCode(
        @Body request: ScanRequest
    ): Response<ScanResponse>
    
    @POST("api/Operator/scan/confirm")
    suspend fun confirmAction(
        @Body request: ConfirmRequest
    ): Response<ScanResponse>
}
