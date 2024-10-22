package com.example.mywiselaundrylife.data

import com.example.mywiselaundrylife.data.base.Laundry

object UserInfo {
    var userId : Int? = null
    var useLaundry : Laundry? = null
    var useDry : Laundry? = null

    var token : String? = null

    var userLaundryLst = arrayListOf<Laundry>()
    var currentRoom : Int = 0

    var seconds = 20
}