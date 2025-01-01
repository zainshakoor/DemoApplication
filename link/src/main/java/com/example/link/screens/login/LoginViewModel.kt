package com.example.link.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.link.encrypt.ExtEncryptionDecryption
import com.example.link.model.RequestModel
import com.example.link.model.ResponseModel
import com.example.link.model.VerifyAuthModel
import com.example.link.model.generateDeviceInfo
import com.example.link.network.RetrofitClient
import com.example.link.screens.login.LoginActivity
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.IOException
import java.net.URL
import java.util.concurrent.CancellationException

class LoginViewModel : ViewModel() {

    private val TAG = "LoginViewModel"
    private var decryptedChallenge: String? = null

    private val _challengeKeyFlow =
        MutableSharedFlow<String>(replay = 1) // replay the last emitted value
    val challengeKeyFlow: SharedFlow<String> = _challengeKeyFlow

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _messageFlow = MutableSharedFlow<String>(replay = 1)
    val messageFlow: SharedFlow<String> = _messageFlow

    // IP Address


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

    private fun saveSignedTokenVerifyToSharedPreferences(context: Context, signedtoken: String) {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(LoginActivity.signed_token, signedtoken)
            apply()
        }
        Log.d(TAG, "FCM Token saved to SharedPreferences.")
    }

    private fun getSignedTokenFromSharedPreferences(context: Context): String? {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(LoginActivity.signed_token, null)
    }

    // Perform the full login flow including receiving challenge keys
    @RequiresApi(Build.VERSION_CODES.R)
    fun performLoginFlow(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true

            try {
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
                    Log.e(TAG, "Register Device failed.")
                    return@launch
                }

                Log.d(TAG, "Register Device successful.")

                // Check for signed token in the response
                val responseBody = registerResponse.body()
                if (responseBody?.signed_token != null) {
                    Log.d(TAG, "Signed token received. Proceeding to next screen.")
                    _messageFlow.emit("Device is already registered. Proceeding...")
                    // Navigate to the next screen
                    _isLoading.value = false
                    return@launch
                }

                // Step 2: Wait for Challenge Key
                val challengeKey1 = try {
                    Log.d(TAG, "Waiting for first Challenge Key...")
                    _challengeKeyFlow.first() // Wait for the challenge key
                } catch (e: Exception) {
                    throw Exception("Error waiting for Challenge Key: ${e.message}", e)
                }

                if (challengeKey1.isNullOrEmpty()) {
                    throw Exception("First Challenge Key not received. Stopping the flow.")
                }

                Log.d(TAG, "Received first Challenge Key: $challengeKey1")

                // Step 3: Decrypt Challenge Key
                decryptedChallenge = decryptChallengeKey(challengeKey1)
                if (decryptedChallenge == null) {
                    throw Exception("Failed to decrypt Challenge Key. Stopping the flow.")
                }

                Log.d(TAG, "Decrypted Challenge Key: $decryptedChallenge")

                // Step 4: Verify Authentication
                val verifyAuthResponse = verifyAuthSuspend(fcmToken, decryptedChallenge!!, context)
                if (verifyAuthResponse == null || !verifyAuthResponse.isSuccessful) {
                    throw Exception("Verify Authentication failed.")
                }

                Log.d(TAG, "Verify Authentication successful.")
                _messageFlow.emit("Device Registration Successful!")

            } catch (e: CancellationException) {
                Log.e(TAG, "Login Flow was cancelled: ${e.message}", e)
                _messageFlow.emit("Login process was interrupted. Please try again.")
            } catch (e: Exception) {
                Log.e(TAG, "Error during login flow: ${e.message}", e)
                _messageFlow.emit("An error occurred during login. Please try again.")
            } finally {
                _isLoading.value = false
            }
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

        val signedToken = getSignedTokenFromSharedPreferences(context) ?: ""

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
            projectKuid = deviceInfo.projectKuid,
            signed_token = signedToken
        )

        try {
            val response = RetrofitClient.instance.register(request)
            val statusCode = response.code()

            if (response.isSuccessful) {
                val responseBody = response.body()
                Log.d(
                    TAG,
                    "Register API Success: ${responseBody?.status} - ${responseBody?.message}"
                )

                val newSignedToken = responseBody?.signed_token
                if (!newSignedToken.isNullOrEmpty()) {
                    saveSignedTokenVerifyToSharedPreferences(context, newSignedToken)
                    Log.d(TAG, "Register Signed token saved to SharedPreferences: $newSignedToken")
                    _messageFlow.emit("Device is already registered. Proceeding...")
                }

                return response
            } else {
                Log.e(TAG, "Register Device API Error: Status Code: $statusCode")

                when (statusCode) {
                    400 -> {
                        Log.e(TAG, "Device already registered with the same credentials.")
                        _messageFlow.emit("Device is already registered. Proceeding...")
                        // Navigate to the dashboard
                    }

                    404 -> {
                        Log.e(TAG, "Device Registration Required")
                        _messageFlow.emit("Device Registration Required")
                    }

                    403 -> {
                        Log.e(TAG, "403 Error: Access forbidden.")
                        _messageFlow.emit("Access forbidden. Please contact support.")
                    }

                    else -> {
                        Log.e(TAG, "Unhandled API Error: Status Code: $statusCode")
                        _messageFlow.emit("Unexpected error occurred. Please try again.")
                    }
                }
                return null
            }
        } catch (e: IOException) {
            Log.e(TAG, "Register Device Network Failure: ${e.message}", e)
            _messageFlow.emit("Network error occurred. Please check your connection.")
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error: ${e.message}", e)
            _messageFlow.emit("An unexpected error occurred. Please try again.")
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
            challengeKey = challengeKey)

        try {
            val response = RetrofitClient.verifyAuthInstance.verifyAuth(verifyAuth)
            if (response.isSuccessful) {
                val responseBody = response.body()
                Log.d(
                    TAG,
                    "VerifyAuth API Success: ${responseBody?.status} - ${responseBody?.message}"
                )

                val signedToken = responseBody?.signed_token
                if (!signedToken.isNullOrEmpty()) {
                    saveSignedTokenVerifyToSharedPreferences(context, signedToken)
                    Log.d(TAG, "Signed token saved to SharedPreferences: $signedToken")
                } else {
                    Log.w(TAG, "Signed token is null or empty in the response.")
                }
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
