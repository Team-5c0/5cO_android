package com.example.mywiselaundrylife.data

import android.os.Build
import androidx.annotation.RequiresApi

object ListData {
    val roomLst = createLaundryRooms()
    @RequiresApi(Build.VERSION_CODES.O)
    val laundryLst = createLaundry()

    private fun createLaundryRooms() : ArrayList<LaundryRoom>{
        val laundryRoomLst = arrayListOf<LaundryRoom>()



        return laundryRoomLst
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createLaundry(): ArrayList<Laundry> {
        val laundryLst = arrayListOf<Laundry>()

        for (roomId in 1..7) {
            for (laundryId in 1..5) {
                if (laundryId <= 3) {
                    laundryLst.add(
                        Laundry(
                            laundryId.toString(),
                            roomId.toString(),
                            "세탁기",
                            null,
                            null
                        )
                    )
                } else {
                    laundryLst.add(
                        Laundry(
                            laundryId.toString(),
                            roomId.toString(),
                            "건조기",
                            null,
                            null
                        )
                    )
                }
            }
        }

        return laundryLst
    }
}