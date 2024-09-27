package com.example.mywiselaundrylife.data

data class LaundryRoom(
    val roomId : Int,
    val roomName : String,
    var remainLaundry : Int,
    var remaingDryer : Int,
    val laundryLst : ArrayList<Laundry>
)

data class Laundry(
    var name : String,
    var time : Long? = null,
    var userId : String? = null
)