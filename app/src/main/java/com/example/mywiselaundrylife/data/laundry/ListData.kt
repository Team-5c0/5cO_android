package com.example.mywiselaundrylife.data.laundry

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.mywiselaundrylife.data.base.Laundry
import com.example.mywiselaundrylife.data.base.Room

object ListData {
    var roomLst = ArrayList<Room>()
    @RequiresApi(Build.VERSION_CODES.O)
    var laundryLst = ArrayList<Laundry>()
}