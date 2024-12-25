package com.example.link.viewmodel

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.link.encrypt.ExtEncryptionDecryption
import com.example.link.model.ResponseModel
import com.example.link.model.SendAuthModel
import com.example.link.model.VerifyAuthModel
import com.example.link.network.RetrofitClient
import com.example.link.screens.amount.AmountActivity
import com.example.link.model.generateDeviceInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import retrofit2.Response

class AmountViewModel : ViewModel() {

    private val TAG = "AmountViewModel"
    private var decryptedChallenge: String? = null

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _message: MutableLiveData<String> = MutableLiveData()
    val message: LiveData<String> = _message

    // SharedFlow for emitting the challenge key
    private val _challengeKeyFlow = MutableSharedFlow<String>()
    val challengeKeyFlow: SharedFlow<String> = _challengeKeyFlow


    companion object

    {

    val PREFS_NAME = "MyAppPreferences"
    val KEY_FCM_TOKEN = "firebase_token"
    val KEY_CHALLENGE = "challenge"


    }

    // Function to process the amount flow
    @RequiresApi(Build.VERSION_CODES.R)
    fun processAmountFlow(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)

            val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val fcmToken = sharedPreferences.getString(KEY_FCM_TOKEN, null)
            if (fcmToken.isNullOrEmpty()) {
                _message.postValue("FCM Token is missing")
                _isLoading.postValue(false)
                return@launch
            }

            // Step 1: Send Authentication Request for Amount
            val sendAuthResponse = sendAuthSuspend(fcmToken, context)
            if (sendAuthResponse == null || !sendAuthResponse.isSuccessful) {
                _message.postValue("Send Auth failed.")
                _isLoading.postValue(false)
                return@launch
            }

            // Step 2: Collect the Challenge Key from the SharedFlow
            val challengeKey1 = try {
                Log.d(TAG, "Waiting for Challenge Key...")
                challengeKeyFlow.first() // Wait for the challenge key from the flow
            } catch (e: Exception) {
                _message.postValue("Error waiting for Challenge Key: ${e.message}")
                _isLoading.postValue(false)
                return@launch
            }

            if (challengeKey1.isNullOrEmpty()) {
                _message.postValue("Challenge Key missing.")
                _isLoading.postValue(false)
                return@launch
            }

            Log.d(TAG, "Received Challenge Key: $challengeKey1")

            // Step 3: Decrypt Challenge Key
            decryptedChallenge = decryptChallengeKey(challengeKey1)
            if (decryptedChallenge == null) {
                _message.postValue("Failed to decrypt Challenge Key.")
                _isLoading.postValue(false)
                return@launch
            }

            Log.d(TAG, "Decrypted Challenge Key: $decryptedChallenge")

            // Step 4: Verify Authentication after decryption
            val verifyAuthResponse = verifyAuthSuspend(fcmToken, decryptedChallenge!!, context)
            if (verifyAuthResponse == null || !verifyAuthResponse.isSuccessful) {
                _message.postValue("Verify Authentication failed.")
                _isLoading.postValue(false)
                return@launch
            }

            // If all steps are successful, post message and set loading state to false
            _message.postValue("Verification Successful!")
            _isLoading.postValue(false)
        }
    }

    // Function to emit the challenge key (you will call this function when you receive the key from another part of your app)
    fun receiveChallengeKey(challengeKey: String) {
        viewModelScope.launch {
            // Log the received challenge key before emitting it
            Log.d(TAG, "Emitting Challenge Key: $challengeKey")
            _challengeKeyFlow.emit(challengeKey) // Emit the key to the SharedFlow
        }
    }

    // Function to decrypt the challenge key using your existing decryption method
    private fun decryptChallengeKey(encryptedKey: String): String? {
        return try {
            Log.d(TAG, "Attempting to decrypt challenge key: $encryptedKey")
            val decrypted = ExtEncryptionDecryption.decryptDataFromBase64(encryptedKey)
            // Log the decrypted value
            Log.d(TAG, "Decrypted Data: $decrypted")
            decrypted
        } catch (e: Exception) {
            Log.e(TAG, "Decryption error: ${e.message}")
            null
        }
    }

    // Function to send authentication request
    @RequiresApi(Build.VERSION_CODES.R)
    private suspend fun sendAuthSuspend(
        fcmToken: String,
        context: Context
    ): Response<ResponseModel>? {
        val deviceInfo = generateDeviceInfo(context)

        val sendAuthRequest = SendAuthModel(
            fcmToken,
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
            journey = AmountActivity::class.simpleName.toString()
        )
        return try {
            val response = RetrofitClient.sendAuthInstance.sendRequest(sendAuthRequest)
            if (response.isSuccessful) {
                response
            } else {
                Log.e(TAG, "SendAuth failed: ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "SendAuth network failure: ${e.message}", e)
            null
        }
    }

    // Function to verify authentication
    @RequiresApi(Build.VERSION_CODES.R)
    private suspend fun verifyAuthSuspend(
        fcmToken: String,
        challengeKey: String,
        context: Context
    ): Response<ResponseModel>? {
        val deviceInfo = generateDeviceInfo(context)
        val verifyAuth = VerifyAuthModel(
            fcmToken = fcmToken,
            deviceId = deviceInfo.deviceId,
            challengeKey = challengeKey
        )

        return try {
            val response = RetrofitClient.verifyAuthInstance.verifyAuth(verifyAuth)
            if (response.isSuccessful) {
                Log.d(TAG, "VerifyAuth success: ${response.body()?.status} - ${response.body()?.message}")
                response
            } else {
                Log.e(TAG, "VerifyAuth error: ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "VerifyAuth network failure: ${e.message}", e)
            null
        }
    }
}
