package com.example.mywiselaundrylife.data.base

import java.time.LocalDateTime

data class LoginResponse(
    val token : String,
    val tokenType : String
)

data class Room(
    val roomid : String
)

data class Laundry(
    var available : Boolean,
    val washerId : Int,
    val startTime : String?,
    var endTime : String?,
    var user : Int?,
    val washerType : String,
    var roomId : String? = null
)