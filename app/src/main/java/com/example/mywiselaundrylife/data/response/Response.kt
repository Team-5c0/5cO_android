package com.example.mywiselaundrylife.data.response

data class LoginResponse(
    val token : String,
    val tokenType : String
)

data class Room(
    val roomid : String
)

data class FCMResponse(
    val userId : Int,
    val fcmToken : String
)

data class Laundry(
    var available : Boolean,
    val washerId : Int,
    var startTime : String?,
    var endTime : String?,
    var user : Int?,
    val washerType : String,
    var roomId : String? = null
)