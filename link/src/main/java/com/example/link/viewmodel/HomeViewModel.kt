package com.example.link.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import checkIfRooted
import com.example.link.encrypt.ExtEncryptionDecryption
import com.example.link.model.RequestModel
import com.example.link.model.ResponseModel
import com.example.link.model.SendAuthModel
import com.example.link.model.VerifyAuthModel
import com.example.link.network.RetrofitClient
import com.example.link.screens.HomeActivity
import com.google.firebase.messaging.FirebaseMessaging
import getScreenResolution
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.Response
import java.util.UUID

class HomeViewModel : ViewModel() {

    private val TAG = "HomeViewModel"

    private var decryptedChallenge: String? = null

//    val deviceId="kj982hdwqwqkjois"

    // SharedFlow to receive challenge keys
    private val _challengeKeyFlow = MutableSharedFlow<String>()
    val challengeKeyFlow: SharedFlow<String> = _challengeKeyFlow

    private val _message: MutableLiveData<String> = MutableLiveData()
    val message: LiveData<String> = _message

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun retrieveAndStoreFCMToken(context: Context) {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    return@addOnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result
                Log.d(TAG, "FCM Token: $token")

                // Save token to SharedPreferences
                saveTokenToSharedPreferences(context, token)
            }
    }

    private fun saveTokenToSharedPreferences(context: Context, token: String) {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(HomeActivity.PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(HomeActivity.KEY_FCM_TOKEN, token)
            apply()
        }
        Log.d(TAG, "FCM Token saved to SharedPreferences.")
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun performRegisterVerifySendFlow(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            val sharedPreferences =
                context.getSharedPreferences(HomeActivity.PREFS_NAME, Context.MODE_PRIVATE)
            val fcmToken = sharedPreferences.getString(HomeActivity.KEY_FCM_TOKEN, null)

            if (fcmToken.isNullOrEmpty()) {
                Log.e(TAG, "FCM Token is not available. Cannot proceed with the flow.")
                return@launch
            }

            // Get device id
         val deviceId = getDeviceId(context)
//         Log.d(TAG, "Starting Register -> VerifyAuth -> SendAuth -> VerifyAuth flow.")

            // Step 1: Register Device
            val registerResponse = registerDeviceSuspend(fcmToken, deviceId, context)
            if (registerResponse == null || !registerResponse.isSuccessful) {
                Log.e(TAG, "Register Device failed. Stopping the flow.")
                return@launch
            }

            Log.d(TAG, "Register Device successful.")

            // Step 2: Wait for first Challenge Key via SharedFlow
            val challengeKey1 = try {
                Log.d(TAG, "Waiting for first Challenge Key...")
                _challengeKeyFlow.first()
            } catch (e: Exception) {
                Log.e(TAG, "Error waiting for Challenge Key: ${e.message}", e)
                null
            }

            if (challengeKey1 == null) {
                Log.e(TAG, "First Challenge Key not received. Stopping the flow.")
                return@launch
            }

            Log.d(TAG, "Received first Challenge Key: $challengeKey1")

            // Step 3: Decrypt Challenge Key using the decryptDataFromBase64 function
            decryptedChallenge = decryptChallengeKey(challengeKey1)
            if (decryptedChallenge == null) {
                Log.e(TAG, "Failed to decrypt first Challenge Key. Stopping the flow.")
                return@launch
            }

            Log.d(TAG, "Decrypted first Challenge Key: $decryptedChallenge")

            // Step 4: Verify Authentication
            val verifyAuthResponse1 = verifyAuthSuspend(fcmToken, deviceId, decryptedChallenge!!)
            if (verifyAuthResponse1 == null || !verifyAuthResponse1.isSuccessful) {
                Log.e(TAG, "VerifyAuth failed after Register. Stopping the flow.")
                return@launch
            }

            Log.d(TAG, "VerifyAuth after Register successful.")

            // Step 5: Send Authentication Request
            val sendAuthResponse = sendAuthSuspend(fcmToken, deviceId,context)
            if (sendAuthResponse == null || !sendAuthResponse.isSuccessful) {
                Log.e(TAG, "SendAuth failed. Stopping the flow.")
                return@launch
            }

            Log.d(TAG, "SendAuth successful.")

            // Step 6: Wait for second Challenge Key via SharedFlow
            val challengeKey2 = try {
                Log.d(TAG, "Waiting for second Challenge Key...")
                _challengeKeyFlow.first()
            } catch (e: Exception) {
                Log.e(TAG, "Error waiting for second Challenge Key: ${e.message}", e)
                null
            }

            if (challengeKey2 == null) {
                Log.e(TAG, "Second Challenge Key not received. Stopping the flow.")
                return@launch
            }

            Log.d(TAG, "Received second Challenge Key: $challengeKey2")

            // Step 7: Decrypt Challenge Key using the decryptDataFromBase64 function
            decryptedChallenge = decryptChallengeKey(challengeKey2)
            if (decryptedChallenge == null) {
                Log.e(TAG, "Failed to decrypt second Challenge Key. Stopping the flow.")
                return@launch
            }

            Log.d(TAG, "Decrypted second Challenge Key: $decryptedChallenge")

            // Step 8: Final Verify Authentication
            val verifyAuthResponse2 = verifyAuthSuspend(fcmToken, deviceId, decryptedChallenge!!)
            if (verifyAuthResponse2 == null || !verifyAuthResponse2.isSuccessful) {
                Log.e(TAG, "Final VerifyAuth failed. Stopping the flow.")
                return@launch
            }

            Log.d(TAG, "Final VerifyAuth successful. Flow completed.")
            _isLoading.value = false
            _message.postValue(verifyAuthResponse2.body()?.message.toString())
        }
    }

    fun receiveChallengeKey(challengeKey: String) {
        viewModelScope.launch {
            _challengeKeyFlow.emit(challengeKey)
            Log.d(TAG, "Challenge Key emitted to flow: $challengeKey")
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private suspend fun registerDeviceSuspend(
        fcmToken: String,
        deviceId: String,
        context: Context
    ): Response<ResponseModel>? {
        val deviceInfo = generateDeviceInfo(context)

        val formattedkey=ExtEncryptionDecryption.mPublicKeyPEM.toString()
        val request = RequestModel(
            fcmToken = fcmToken,
            publicKey =formattedkey,  // Ensure this is in PEM format
            deviceId = deviceId,
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

        return try {
            val response = RetrofitClient.instance.register(request)
            if (response.isSuccessful) {
                Log.d(TAG, "Register Device API Success: ${response.body()?.status} - ${response.body()?.message}")
            } else {
                _isLoading.value = false
                Log.e(TAG, "Register Device API Error: ${response.errorBody()?.string()}")
                Log.e(TAG, "HTTP Error Code: ${response.code()} - ${response.message()}")
            }
            response
        } catch (e: Exception) {
            Log.e(TAG, "Register Device Network Failure: ${e.message}", e)
            null
        }
    }

    private suspend fun verifyAuthSuspend(
        fcmToken: String,
        deviceId: String,
        challengeKey: String
    ): Response<ResponseModel>? {
        val verifyAuth = VerifyAuthModel(
            fcmToken = fcmToken,
            deviceId = deviceId,
            challengeKey = challengeKey
        )

        return try {
            val response = RetrofitClient.verifyAuthInstance.verifyAuth(verifyAuth)
            if (response.isSuccessful) {
                Log.d(TAG, "VerifyAuth API Success: ${response.body()?.status} - ${response.body()?.message}")
            } else {
                Log.e(TAG, "VerifyAuth API Error: ${response.errorBody()?.string()}")
                Log.e(TAG, "HTTP Error Code: ${response.code()} - ${response.message()}")
            }
            response
        } catch (e: Exception) {
            Log.e(TAG, "VerifyAuth Network Failure: ${e.message}", e)
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private suspend fun sendAuthSuspend(
        fcmToken: String,
        deviceId: String,
        context: Context
    ): Response<ResponseModel>? {
        val deviceInfo = generateDeviceInfo(context)

        val sendAuthRequest = SendAuthModel(
            fcmToken = fcmToken,
            deviceId = deviceId,
            deviceModel = deviceInfo.deviceModel,
            deviceOS = deviceInfo.deviceOS,
            hostAppVersion = deviceInfo.hostAppVersion,
            isRooted = deviceInfo.isRooted,
            screenResolution = deviceInfo.screenResolution,
            userIdentifier = deviceInfo.userIdentifier,
            sdkVersion = deviceInfo.sdkVersion,
            workspaceKuid = deviceInfo.workspaceKuid,
            projectKuid = deviceInfo.projectKuid,
            journey = HomeActivity::class.simpleName.toString()

        )

        return try {
            val response = RetrofitClient.sendAuthInstance.sendRequest(sendAuthRequest)
            if (response.isSuccessful) {
                Log.d(TAG, "SendAuth API Success: ${response.body()?.status} - ${response.body()?.message}")
            } else {
                Log.e(TAG, "SendAuth API Error: ${response.errorBody()?.string()}")
                Log.e(TAG, "HTTP Error Code: ${response.code()} - ${response.message()}")
            }
            response
        } catch (e: Exception) {
            Log.e(TAG, "SendAuth Network Failure: ${e.message}", e)
            null
        }
    }

    private fun decryptChallengeKey(encryptedKey: String): String? {
        return try {
            ExtEncryptionDecryption.decryptDataFromBase64(encryptedKey)
        } catch (e: Exception) {
            Log.e(TAG, "Error decrypting challenge key: ${e.message}", e)
            null
        }
    }

    private fun getDeviceId(context: Context): String {
        return android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )
    }

    // Helper function to generate dynamic device info
    @RequiresApi(Build.VERSION_CODES.R)
    fun generateDeviceInfo(context: Context): DeviceInfo {
        val deviceModel = Build.MODEL ?: "Unknown"
        val deviceOS = "Android ${Build.VERSION.RELEASE}"
        val hostAppVersion = context.packageManager.getPackageInfo(context.packageName, 0).versionName
        val isRooted = checkIfRooted()
        val screenResolution = getScreenResolution(context)
        val userIdentifier = UUID.randomUUID().toString()
        val sdkVersion = Build.VERSION.SDK_INT.toString()
        val workspaceKuid = "5fd236e39fdb44f7aaa11ad5271917fd"
        val projectKuid = "B078DB4F79A44E2FBAAE99E88CC3D893"

        return DeviceInfo(
            deviceModel = deviceModel,
            deviceOS = deviceOS,
            hostAppVersion = hostAppVersion,
            isRooted = isRooted,
            screenResolution = screenResolution,
            userIdentifier = userIdentifier,
            sdkVersion = sdkVersion,
            workspaceKuid = workspaceKuid,
            projectKuid = projectKuid
        )
    }

    data class DeviceInfo(
        val deviceModel: String,
        val deviceOS: String,
        val hostAppVersion: String,
        val isRooted: Boolean,
        val screenResolution: String,
        val userIdentifier: String,
        val sdkVersion: String,
        val workspaceKuid: String,
        val projectKuid: String
    )
}
