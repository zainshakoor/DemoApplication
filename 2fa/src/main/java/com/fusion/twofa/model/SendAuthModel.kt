package com.fusion.twofa.model

data class SendAuthModel(
    val fcmToken: String,
    val deviceId: String,
    val deviceModel: String,
    val deviceOS: String,
    val hostAppVersion: String,
    val isRooted: Boolean,
    val screenResolution: String,
    val userIdentifier: String,
    val sdkVersion: String,
    val workspaceKuid: String,
    val projectKuid: String,
    val journey: String)
