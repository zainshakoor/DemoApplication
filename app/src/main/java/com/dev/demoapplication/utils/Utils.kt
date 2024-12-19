package com.dev.demoapplication.utils

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import java.util.UUID

@SuppressLint("HardwareIds")
fun getDeviceId(activity: Context): String {
    return Settings.Secure.getString(
        activity.contentResolver,
        Settings.Secure.ANDROID_ID
    )
}

fun generateUUID(): String {
    val uuid = UUID.randomUUID()
    return uuid.toString()  // Returns the UUID as a String
}

