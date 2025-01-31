package com.example.mywiselaundrylife.activity

import android.util.Log
import com.example.mywiselaundrylife.data.laundry.LaundryRequestManager
import com.example.mywiselaundrylife.data.user.UserInfo
import com.example.mywiselaundrylife.data.user.UserInfo.lstData

object RefreshData {

    suspend fun roomRequest() {
        val roomResponse = LaundryRequestManager.roomsRequest("Bearer ${UserInfo.token!!}")
        lstData.roomLst = roomResponse!!

        Log.d("room", "${lstData.roomLst}")

        lstData.laundryLst = ArrayList()

        lstData.roomLst.forEach { i ->
            washerRequest(i.roomid)
        }
    }

    private suspend fun washerRequest(roomId: String) {
        val washerResponse = LaundryRequestManager.laundryRequest(roomId)
        washerResponse!!.forEach { washer ->
            washer.roomId = roomId
            if (washer.user == UserInfo.userId) {
                when (washer.washerType) {
                    "WASHER" -> UserInfo.useLaundry = washer
                    "DRYER" -> UserInfo.useDry = washer
                }
            }
        }

        // 같은 데이터 갱신하기
        washerResponse.forEach { washer ->
            val index =
                lstData.laundryLst.indexOfFirst { it.roomId == roomId && it.washerId == washer.washerId }
            if (index != -1) {
                lstData.laundryLst[index] = washer
            } else {
                lstData.laundryLst.add(washer)
            }
        }

        lstData.laundryLst = ArrayList(lstData.laundryLst)
        Log.d("laundryLst", "${lstData.laundryLst}")
    }

}