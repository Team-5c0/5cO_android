package com.example.mywiselaundrylife.data

import com.example.mywiselaundrylife.data.base.Laundry

object UserInfo {
    var userId : Int? = null
    var useLaundry : Laundry? = null
    var useDry : Laundry? = null

    var token : String? = null
    var FCMtoken : String? = null

    var userLaundryLst = arrayListOf<Laundry>()
    var currentRoom : String = ""

    var seconds = 20
}