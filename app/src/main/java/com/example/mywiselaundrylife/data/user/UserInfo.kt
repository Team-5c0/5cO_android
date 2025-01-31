package com.example.mywiselaundrylife.data.user

import com.example.mywiselaundrylife.data.base.Laundry
import com.example.mywiselaundrylife.data.laundry.ListData

object UserInfo {
    var userId : Int? = null
    var useLaundry : Laundry? = null
    var useDry : Laundry? = null

    var token : String? = null
    var FCMToken : String? = null

    var userLaundryLst = arrayListOf<Laundry>()
    var currentRoom : String = ""

    var lstData = ListData()

    fun resetUserInfo(){
        userId = null
        useLaundry = null
        useDry = null
        token = null
        FCMToken = null
        userLaundryLst = arrayListOf()
        currentRoom = ""
        lstData = ListData()
    }
}