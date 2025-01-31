package com.example.mywiselaundrylife.data.user

import com.example.mywiselaundrylife.data.base.Laundry

object UserInfo {
    var userId : Int? = null
    var useLaundry : Laundry? = null
    var useDry : Laundry? = null

    var token : String? = null
    var FCMtoken : String? = null

    var userLaundryLst = arrayListOf<Laundry>()
    var currentRoom : String = ""

    fun resetUserInfo(){
        userId = null
        useLaundry = null
        useDry = null
        token = null
        FCMtoken = null
        userLaundryLst = arrayListOf<Laundry>()
        currentRoom = ""
    }
}