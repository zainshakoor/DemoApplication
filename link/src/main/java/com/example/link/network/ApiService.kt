// RetrofitService.kt
package com.example.link.network

import com.example.link.model.RequestModel
import com.example.link.model.ResponseModel
import com.example.link.model.SendAuthModel
import com.example.link.model.VerifyAuthModel
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface RetrofitService {
    @Headers("Content-Type: application/json")
    @POST("local-gateway/sdk-secure-link/api/v1/sdk/securelink-2fa/register")
    suspend fun register(
        @Body request: RequestModel
    ): Response<ResponseModel>
}


interface VerifyAuthService {
    @Headers("Content-Type: application/json")
    @POST("local-gateway/sdk-secure-link/api/v1/sdk/securelink-2fa/verify-auth")
    suspend fun verifyAuth(
        @Body request: VerifyAuthModel
    ): Response<ResponseModel>
}

interface SendAuthService {
    @Headers("Content-Type: application/json")
    @POST("local-gateway/sdk-secure-link/api/v1/sdk/securelink-2fa/send-auth")
    suspend fun sendRequest(
        @Body request: SendAuthModel
    ): Response<ResponseModel>
}
