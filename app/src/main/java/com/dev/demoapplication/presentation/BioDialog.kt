package com.dev.demoapplication.presentation

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun BiometricPromptDialog(
    context: Context,
    onAuthenticated: () -> Unit,
    onError: () -> Unit
) {
    val activity = context as? FragmentActivity

    LaunchedEffect(Unit) {
        // Only trigger if the activity context is valid
        activity?.let { startBiometricPrompt(it, onAuthenticated, onError) }
    }
}

fun startBiometricPrompt(
    activity: FragmentActivity,
    onAuthenticated: () -> Unit,
    onError: () -> Unit
) {
    Log.d("Bio", "startBiometricPrompt: ")
    val executor: Executor = Executors.newSingleThreadExecutor()

    val biometricPrompt =
        BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onAuthenticated()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onError()
            }

            override fun onAuthenticationFailed() {
                onError()
            }
        })

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Biometric Authentication")
        .setSubtitle("Use fingerprint to authenticate")
        .setNegativeButtonText("Cancel")
        .build()

    biometricPrompt.authenticate(promptInfo)
}

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun BiometricAuthenticationButton(onAuthenticated: () -> Unit, onError: () -> Unit) {
    Column {
        val context = LocalContext.current

        // MutableState to control when the biometric prompt should be shown
        val showBiometricPrompt = remember { mutableStateOf(false) }

        Text(text = "Biometric Authentication")

        // Only show the biometric prompt when the state is true
        if (showBiometricPrompt.value) {
            BiometricPromptDialog(
                context = context,
                onAuthenticated = onAuthenticated,
                onError = onError
            )
        }

        Button(onClick = {
            // Trigger the biometric prompt when button is clicked
            showBiometricPrompt.value = true
        }) {
            Text("Helllo")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.P)
@Preview
@Composable
fun PreviewBiometricAuthenticationButton() {
    BiometricAuthenticationButton(
        onAuthenticated = { /* Handle Success */
        },
        onError = { /* Handle Error */ }
    )
}
