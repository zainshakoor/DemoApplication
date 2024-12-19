package com.dev.demoapplication.network

import com.dev.demoapplication.data.FcmAuthRequest
import com.dev.demoapplication.data.FcmAuthResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    //move link to constant
    @POST("local-gateway/app-identity/api/v1/fcm_authenticate")
    fun authenticateFcm(
        @Body requestBody: FcmAuthRequest
    ): Call<FcmAuthResponse>

}

