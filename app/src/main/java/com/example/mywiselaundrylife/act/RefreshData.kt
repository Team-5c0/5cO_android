package com.example.mywiselaundrylife.act

import android.util.Log
import com.example.mywiselaundrylife.data.laundry.LaundryRequestManager
import com.example.mywiselaundrylife.data.laundry.ListData
import com.example.mywiselaundrylife.data.user.UserInfo

object RefreshData {

    suspend fun roomRequest(){
        val roomResponse = LaundryRequestManager.roomsRequest("Bearer ${UserInfo.token!!}")
        ListData.roomLst = roomResponse!!

        Log.d("room", "${ListData.roomLst}")

        ListData.laundryLst = ArrayList()

        ListData.roomLst.forEach{ i ->
            washerRequest(i.roomid)
        }
    }

    suspend fun washerRequest(roomId : String){
        val washerResponse = LaundryRequestManager.laundryRequest(roomId)
        washerResponse!!.forEach { washer->
            washer.roomId = roomId
            if(washer.user == UserInfo.userId){
                when(washer.washerType){
                    "WASHER" -> UserInfo.useLaundry = washer
                    "DRYER" -> UserInfo.useDry = washer
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