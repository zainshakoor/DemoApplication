package com.dev.demoapplication.data

data class FcmAuthRequest(
    val device_info: String,
    val hash_message: String,
    val fcm_token: String?,
//    val uuid: String?
)

data class FcmAuthResponse(
    val status: Boolean
)
