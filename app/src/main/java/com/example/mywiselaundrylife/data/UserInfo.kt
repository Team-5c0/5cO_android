package com.example.mywiselaundrylife.data

import android.media.session.MediaSession

object UserInfo {
    var userId : Int? = null
    var useLaundry : Laundry? = null
    var useDry : Laundry? = null

    var token : String? = null

    var userLaundryLst = arrayListOf<Laundry>()
    var currentRoom : LaundryRoom? = null
}