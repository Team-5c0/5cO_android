package com.example.mywiselaundrylife.act

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.mywiselaundrylife.data.base.Laundry
import com.example.mywiselaundrylife.data.laundry.LaundryRequestMananger
import com.example.mywiselaundrylife.data.laundry.ListData
import com.example.mywiselaundrylife.data.user.UserInfo

object RefreshData {

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun roomRequest(){
        val roomResponse = LaundryRequestMananger.roomsRequest("Bearer ${UserInfo.token!!}")
        ListData.roomLst = roomResponse!!

        Log.d("room", "${ListData.roomLst}")

        ListData.laundryLst = ArrayList()

        for(i in ListData.roomLst){
            washerRequest(i.roomid)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun washerRequest(roomId : String){
        val washerResponse = LaundryRequestMananger.laundryRequest(roomId)
        for(i in washerResponse!!){
            i.roomId = roomId
            if(i.user == UserInfo.userId){
                when(i.washerType){
                    "WASHER" -> UserInfo.useLaundry = i
                    "DRYER" -> UserInfo.useDry = i
                }
            }
        }

        // 같은 데이터 갱신하기
        washerResponse.forEach { washer ->
            val index = ListData.laundryLst.indexOfFirst{it.roomId == roomId && it.washerId == washer.washerId}
            if(index != -1){
                ListData.laundryLst[index] = washer
            } else{
                ListData.laundryLst.add(washer)
            }
        }

        ListData.laundryLst = ArrayList(ListData.laundryLst)
        Log.d("laundryLst", "${ListData.laundryLst}")
    }

}