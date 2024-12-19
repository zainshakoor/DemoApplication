package com.dev.demoapplication.presentation

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dev.demoapplication.utils.FireBaseHelper
import com.dev.demoapplication.utils.StaticDialog
import com.dev.demoapplication.utils.generateUUID
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

lateinit var verifyingStatus: MutableState<String>
lateinit var showVerifyingDialog: MutableState<Boolean>

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun HomeScreen() {


    StaticDialog()
//    ShowDialogButton()
    // State to hold Firebase token
    val firebaseToken = remember { mutableStateOf<String>("") }

    // State for showing the button text or waiting state
    val isFetchingToken = remember { mutableStateOf(false) }
    verifyingStatus =
        remember { mutableStateOf("Sending") } // Status: "Sending", "Receiving", "Verifying"

    @Composable
    fun VerifyingDialog(ellipsis: String) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(text = "Verifying$ellipsis") },
            text = { Text("${verifyingStatus.value} hash Message") },
            confirmButton = {},
            dismissButton = {}
        )
    }


    Column {
        val context = LocalContext.current

        // MutableState to control when the biometric prompt should be shown
        val showBiometricPrompt = remember { mutableStateOf(false) }
        showVerifyingDialog = remember { mutableStateOf(false) }
        var ellipsis by remember { mutableStateOf("") }



        Text(
            text = "Two Factor Authentication",
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()   // Fill the full width to center horizontally
                .padding(top = 32.dp) // Add padding from the top (adjust the value as needed)
        )


        // Only show the biometric prompt when the state is true
        if (showBiometricPrompt.value) {
            BiometricPromptDialog(
                context = context,
                onAuthenticated = {
                    showBiometricPrompt.value = false
                    showVerifyingDialog.value = true
                    verifyingStatus.value = "Sending"
                    isFetchingToken.value =
                        true // Indicate that the token fetch process has started
                    getFirebaseTokenAndSave(context) { token ->
                        firebaseToken.value = token ?: "Failed to fetch token"
                        isFetchingToken.value = false // Reset the fetching state
                        //callig api her
                        FireBaseHelper.shaMsg = "xef55ymmj66732mqdsfsadidsnfsaewrlkn"
                        FireBaseHelper.devInfo =
                            "xefmo55ymmj6673elsfsadidsnfewrplknidsfsadxhhfyyfdejl"
                        FireBaseHelper.uuID = generateUUID()
                        FcmAuthenticator().authenticateFcm(
                            deviceInfo = FireBaseHelper.devInfo,
                            hashMessage = FireBaseHelper.shaMsg,
                            fcmToken = token,
//                                    uuid = FireBaseHelper.uuID
                        )
                    }
                },
                onError = {
                    showBiometricPrompt.value = false
                    //toat failed to auth biomtic
                }
            )
        }

        if (showVerifyingDialog.value) {
            VerifyingDialog(ellipsis)

            // Start the loading animation and status updates
            LaunchedEffect(Unit) {
                // Ellipsis animation - loops until the verification process completes
                launch {
                    while (showVerifyingDialog.value) {
                        repeat(3) { dots ->
                            ellipsis = ".".repeat(dots + 1)
                            delay(700)
                        }
                    }
                }
                // Simulate process completion
            }
        }

        Spacer(modifier = Modifier.height(240.dp))
        Button(modifier = Modifier
            .fillMaxWidth(0.5f)
            .align(Alignment.CenterHorizontally),

            onClick = {
                // Trigger the biometric prompt when button is clicked
                showBiometricPrompt.value = true

            }) {
            Text("Start Authentication")
        }
    }

}


fun getFirebaseTokenAndSave(context: Context, onTokenReceived: (String?) -> Unit) {
    FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
        Log.d("Firebase", "Token: $token")
        // Save token to SharedPreferences
        saveTokenToSharedPreferences(context, token)
        // Pass the token to the callback function
        onTokenReceived(token)
    }.addOnFailureListener {
        Log.e("Firebase", "Failed to get token", it)
        onTokenReceived(null)
    }
}

fun saveTokenToSharedPreferences(context: Context, token: String) {
    val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putString("firebase_token", token)
    editor.apply()
}


@RequiresApi(Build.VERSION_CODES.P)
@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
    HomeScreen() // Pass the context
}

