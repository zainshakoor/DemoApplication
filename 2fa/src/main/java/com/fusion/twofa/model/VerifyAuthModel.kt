package com.fusion.twofa.model

data  class VerifyAuthModel (
    val fcmToken: String,
    val deviceId: String,
    val challengeKey: String
)