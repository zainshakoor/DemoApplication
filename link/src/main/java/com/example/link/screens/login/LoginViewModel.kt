package com.example.link.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.link.encrypt.ExtEncryptionDecryption
import com.example.link.model.RequestModel
import com.example.link.model.ResponseModel
import com.example.link.model.VerifyAuthModel
import com.example.link.network.RetrofitClient
import com.example.link.screens.login.LoginActivity
import com.google.firebase.messaging.FirebaseMessaging
import com.example.link.model.generateDeviceInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class LoginViewModel : ViewModel() {

    private val TAG = "LoginViewModel"
    private var decryptedChallenge: String? = null

    private val _challengeKeyFlow = MutableSharedFlow<String>()
    val challengeKeyFlow: SharedFlow<String> = _challengeKeyFlow

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _message: MutableLiveData<String> = MutableLiveData()
    val message: LiveData<String> = _message

    // Retrieve and store FCM Token
    fun retrieveAndStoreFCMToken(context: Context) {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    return@addOnCompleteListener
                }

                val token = task.result
                Log.d(TAG, "FCM Token: $token")

                saveTokenToSharedPreferences(context, token)
            }
    }

    private fun saveTokenToSharedPreferences(context: Context, token: String) {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(LoginActivity.KEY_FCM_TOKEN, token)
            apply()
        }
        Log.d(TAG, "FCM Token saved to SharedPreferences.")
    }

    // Perform the full login flow including receiving challenge keys
    @RequiresApi(Build.VERSION_CODES.R)
    fun performLoginFlow(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true

            // Retrieve the FCM Token from SharedPreferences
            val sharedPreferences =
                context.getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE)
            val fcmToken = sharedPreferences.getString(LoginActivity.KEY_FCM_TOKEN, null)

            if (fcmToken.isNullOrEmpty()) {
                Log.e(TAG, "FCM Token is not available. Cannot proceed with the flow.")
                _isLoading.value = false
                return@launch
            }

            // Step 1: Register Device
            val registerResponse = registerDeviceSuspend(fcmToken, context)
            if (registerResponse == null || !registerResponse.isSuccessful) {
                _isLoading.value = false
                Log.e(TAG, "Register Device failed. Stopping the flow.")
                return@launch
            }

            Log.d(TAG, "Register Device successful.")

            // Step 2: Wait for Challenge Key
            val challengeKey1 = try {
                Log.d(TAG, "Waiting for first Challenge Key...")
                _challengeKeyFlow.first() // Wait for the challenge key
            } catch (e: Exception) {
                _isLoading.value = false
                Log.e(TAG, "Error waiting for Challenge Key: ${e.message}", e)
                return@launch
            }

            if (challengeKey1 == null) {
                _isLoading.value = false
                Log.e(TAG, "First Challenge Key not received. Stopping the flow.")
                return@launch
            }

            Log.d(TAG, "Received first Challenge Key: $challengeKey1")

            // Step 3: Decrypt Challenge Key
            decryptedChallenge = decryptChallengeKey(challengeKey1)
            if (decryptedChallenge == null) {
                _isLoading.value = false
                Log.e(TAG, "Failed to decrypt Challenge Key. Stopping the flow.")
                return@launch
            }

            Log.d(TAG, "Decrypted Challenge Key: $decryptedChallenge")

            // Step 4: Verify Authentication
            val verifyAuthResponse = verifyAuthSuspend(fcmToken, decryptedChallenge!!, context)
            if (verifyAuthResponse == null || !verifyAuthResponse.isSuccessful) {
                _isLoading.value = false
                Log.e(TAG, "Verify Authentication failed. Stopping the flow.")
                return@launch
            }

            Log.d(TAG, "Verify Authentication successful.")

            _isLoading.value = false
            _message.postValue("Registration and Authentication successful!")
        }
    }

    // Function to receive the challenge key
    fun receiveChallengeKey(challengeKey: String) {
        viewModelScope.launch {
            _challengeKeyFlow.emit(challengeKey)
            Log.d(TAG, "Challenge Key emitted: $challengeKey")
        }
    }

    // Decrypt the challenge key using your decryption method
    private fun decryptChallengeKey(encryptedKey: String): String? {
        return try {
            ExtEncryptionDecryption.decryptDataFromBase64(encryptedKey)
        } catch (e: Exception) {
            Log.e(TAG, "Error decrypting challenge key: ${e.message}", e)
            null
        }
    }

    // Register the device
    @RequiresApi(Build.VERSION_CODES.R)
    private suspend fun registerDeviceSuspend(
        fcmToken: String,
        context: Context
    ): Response<ResponseModel>? {
        val deviceInfo = generateDeviceInfo(context)

        val formattedKey = ExtEncryptionDecryption.mPublicKeyPEM.toString()
        val request = RequestModel(
            fcmToken = fcmToken,
            publicKey = formattedKey,
            deviceId = deviceInfo.deviceId,
            deviceModel = deviceInfo.deviceModel,
            deviceOS = deviceInfo.deviceOS,
            hostAppVersion = deviceInfo.hostAppVersion,
            isRooted = deviceInfo.isRooted,
            screenResolution = deviceInfo.screenResolution,
            userIdentifier = deviceInfo.userIdentifier,
            sdkVersion = deviceInfo.sdkVersion,
            workspaceKuid = deviceInfo.workspaceKuid,
            projectKuid = deviceInfo.projectKuid
        )

        try {
            val response = RetrofitClient.instance.register(request)
            if (response.isSuccessful) {
                Log.d(
                    TAG,
                    "Register Device API Success: ${response.body()?.status} - ${response.body()?.message}"
                )
                return response
            } else {
                val errorBody = response.errorBody()?.string()
                if (errorBody?.contains("Device is already registered with same credentials.") == true) {
                    Log.d(TAG, "Device already registered. Proceeding to the next screen.")
                    _message.postValue("Device already registered. Proceeding to the next screen.")
                    _isLoading.value = false
                    return null
                } else {
                    Log.e(TAG, "Register Device API Error: $errorBody")
                    return null
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Register Device Network Failure: ${e.message}", e)
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error: ${e.message}", e)
            return null
        }
    }

    // Verify authentication
    @SuppressLint("NewApi")
    private suspend fun verifyAuthSuspend(
        fcmToken: String,
        challengeKey: String,
        context: Context,
    ): Response<ResponseModel>? {
        val deviceInfo = generateDeviceInfo(context)

        val verifyAuth = VerifyAuthModel(
            fcmToken = fcmToken,
            deviceId = deviceInfo.deviceId,
            challengeKey = challengeKey
        )

        try {
            val response = RetrofitClient.verifyAuthInstance.verifyAuth(verifyAuth)
            if (response.isSuccessful) {
                Log.d(
                    TAG,
                    "VerifyAuth API Success: ${response.body()?.status} - ${response.body()?.message}"
                )
                return response
            } else {
                Log.e(TAG, "VerifyAuth API Error: ${response.errorBody()?.string()}")
                return null
            }
        } catch (e: IOException) {
            Log.e(TAG, "VerifyAuth Network Failure: ${e.message}", e)
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error: ${e.message}", e)
            return null
        }
    }
}
