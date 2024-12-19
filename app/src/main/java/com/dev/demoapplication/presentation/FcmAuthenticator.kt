package com.dev.demoapplication.presentation



import android.util.Log
import com.dev.demoapplication.data.FcmAuthRequest
import com.dev.demoapplication.data.FcmAuthResponse
import com.dev.demoapplication.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FcmAuthenticator {

    private val apiClient = ApiClient()  // Retrofit instance

    fun authenticateFcm(deviceInfo: String, hashMessage: String, fcmToken: String?) {
        val request = FcmAuthRequest(device_info = deviceInfo, hash_message = hashMessage, fcm_token = fcmToken)

        // Make the API call
        apiClient.apiService.authenticateFcm(request)
            .enqueue(object : Callback<FcmAuthResponse> {
                override fun onResponse(call: Call<FcmAuthResponse>, response: Response<FcmAuthResponse>) {
                    if (response.isSuccessful) {
                        val status = response.body()?.status ?: false
                        Log.d("FCM", "Authentication successful: $status")
                    } else {
                        Log.e("FCM", "Authentication failed: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<FcmAuthResponse>, t: Throwable) {
                    Log.e("FCM", "Error occurred: ${t.message}")
                }
            })
    }
}
