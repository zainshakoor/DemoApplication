package com.example.link.model

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import androidx.annotation.RequiresApi
import com.example.link.screens.login.LoginActivity
import java.io.File


private fun getDeviceId(context: Context): String {
    return android.provider.Settings.Secure.getString(
        context.contentResolver,
        android.provider.Settings.Secure.ANDROID_ID
    )
}

data class DeviceInfo(
    val deviceId: String,
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

@RequiresApi(Build.VERSION_CODES.R)
fun generateDeviceInfo(context: Context): DeviceInfo {
    val deviceId = getDeviceId(context)
    // val deviceId="6tutgxtr452saedffd327624"
    // Get device model

    val deviceModel = Build.MODEL ?: "Unknown"

    // Get device OS version
    val deviceOS = "Android ${Build.VERSION.RELEASE}"

    // Get app version
    val hostAppVersion = context.packageManager.getPackageInfo(context.packageName, 0).versionName

    // Check if the device is rooted
    val isRooted = checkIfRooted()

    // Get screen resolution
    val screenResolution = getScreenResolution(context)

    // Get user identifier (using a UUID as an example)
    val userIdentifier = getUserIdentifier()

    // Get SDK version
    val sdkVersion = Build.VERSION.SDK_INT.toString()

    // Generate workspaceKuid and projectKuid (example UUIDs for now)
    val workspaceKuid = "5fd236e39fdb44f7aaa11ad5271917fd"
    val projectKuid = "B078DB4F79A44E2FBAAE99E88CC3D893"

    return DeviceInfo(
        deviceId = deviceId,
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

fun checkIfRooted(): Boolean {
    // Simple check for root (this can be enhanced)
    return (
            System.getProperty("ro.debuggable") == "1" ||
                    File("/system/app/Superuser.apk").exists() ||
                    File("/system/xbin/su").exists()
            )
}

@RequiresApi(Build.VERSION_CODES.R)
fun getScreenResolution(context: Context): String {
    val metrics = DisplayMetrics()
    val display = context.display
    display?.getRealMetrics(metrics)
    return "${metrics.widthPixels}x${metrics.heightPixels}"
}

fun getUserIdentifier(): String {
    return LoginActivity.USER_NAME // Example UUID
}
