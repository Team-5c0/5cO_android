package com.example.mywiselaundrylife.data.laundry

import com.example.mywiselaundrylife.data.base.Laundry
import com.example.mywiselaundrylife.data.base.Room

data class ListData (
    var roomLst : ArrayList<Room> = ArrayList(),
    var laundryLst : ArrayList<Laundry> = ArrayList()
)