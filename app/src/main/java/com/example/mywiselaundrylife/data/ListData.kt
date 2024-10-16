package com.example.mywiselaundrylife.data

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.LocalDateTime

object ListData {
    val roomLst = createLaundryRooms()
    @RequiresApi(Build.VERSION_CODES.O)
    val laundryLst = createLaundry()

    private fun createLaundryRooms() : ArrayList<LaundryRoom>{
        val laundryRoomLst = arrayListOf<LaundryRoom>()

        for(floor in 2..5){
            for(room in 1..2){
                if(floor == 2 && room == 2){
                    continue
                }
                laundryRoomLst.add(LaundryRoom((laundryRoomLst.size + 1).toString(), "세탁실 ${floor}층-${room}"))
            }
        }

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
                            "세탁기${laundryId}",
                            null,
                            null
                        )
                    )
                } else {
                    laundryLst.add(
                        Laundry(
                            laundryId.toString(),
                            roomId.toString(),
                            "건조기${laundryId % 3}",
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