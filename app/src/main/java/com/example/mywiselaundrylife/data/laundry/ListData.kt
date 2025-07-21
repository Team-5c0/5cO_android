package com.example.mywiselaundrylife.data.laundry

import com.example.mywiselaundrylife.data.response.Laundry
import com.example.mywiselaundrylife.data.response.Room

data class ListData (
    var roomLst : ArrayList<Room> = ArrayList(),
    var laundryLst : ArrayList<Laundry> = ArrayList()
)