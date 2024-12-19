package com.example.link.model

data  class VerifyAuthModel (
    val fcmToken: String,
    val deviceId: String,
    val challengeKey: String
)