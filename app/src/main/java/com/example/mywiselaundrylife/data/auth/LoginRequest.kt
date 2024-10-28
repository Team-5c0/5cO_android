package com.example.mywiselaundrylife.data.auth

data class LoginRequest(
    val userId : Int
)

data class FCMRequest(
    val userId : Int,
    val fcmToken : String
)