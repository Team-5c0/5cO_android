package com.example.mywiselaundrylife.data

import java.time.LocalDateTime

data class LaundryRoom(
    val roomId : String,
    val roomName : String
)

data class Laundry(
    val laundryId : String,
    val roomId : String,
    val name : String,
    var endTime : LocalDateTime? = null,
    var userId : Int? = null
)