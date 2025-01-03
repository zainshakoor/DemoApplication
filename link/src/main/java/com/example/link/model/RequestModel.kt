package com.example.link.model

data class RequestModel(
    val fcmToken: String,
    val publicKey: String,
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

data class ResponseModel(
    val status: Boolean,
    val message: Any,
    val reason: String?,

)

data class Message(
    val code: String?,
    val title: String?,
    val description: String?
)
