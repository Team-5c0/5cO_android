package com.example.mywiselaundrylife.vw

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.mywiselaundrylife.data.base.Laundry
import com.example.mywiselaundrylife.data.laundry.ListData
import com.example.mywiselaundrylife.data.user.UserInfo
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Duration
import java.time.LocalDateTime

class FCMActVM : ViewModel() {

    private val timerMap = mutableMapOf<Laundry, Runnable>()

    private val _laundryTimeTxt = MutableStateFlow("--:--")
    val laundryTimeTxt = _laundryTimeTxt.asStateFlow()

    private val _dryerTimeTxt = MutableStateFlow("--:--")
    val dryerTimeTxt = _dryerTimeTxt.asStateFlow()

    private val _remainWasherTxt = MutableStateFlow("")
    val remainWasherTxt = _remainWasherTxt.asStateFlow()

    private val _selectPosition = MutableStateFlow(0)
    val selectPosition = _selectPosition.asStateFlow()

    private val handler = Handler(Looper.getMainLooper())

    fun remainWasherSum(){
        // 0 : 세탁기, 1 : 건조기
        val remainWasherLst = arrayOfNulls<String>(2) // String? 배열 초기화
        val remainSumLst = IntArray(2) // Int 배열 초기화

        // 데이터 전처리
        ListData.laundryLst.filter { it.available }.forEach { wash ->
            when (wash.washerType) {
                "WASHER" -> {
                    if (remainWasherLst[0] == null) {
                        remainWasherLst[0] = "${wash.roomId} 세탁기${wash.washerId}"
                    }else{
                        remainSumLst[0]++
                    }
                }
                "DRYER" -> {
                    if(remainWasherLst[1]== null) {
                        remainWasherLst[1] = "${wash.roomId} 건조기${wash.washerId}"
                    }else{
                        remainSumLst[1]++
                    }
                }
            }
        }

        val (washer, dryer) = remainWasherLst
        val (washerSum, dryerSum) = remainSumLst

        washerTxt(washer, dryer, washerSum, dryerSum)
    }

    private fun washerTxt(washer : String?, dryer : String?, washerSum : Int, dryerSum : Int) {

        Log.d("washerSum", "$washer $dryer $washerSum $dryerSum")

        _remainWasherTxt.value = buildString {
            when{
                washer != null && dryer != null -> append("$washer, $dryer")
                washer != null -> append(washer)
                dryer != null -> append(dryer)
                else -> "사용가능한 세탁 & 건조기가 없어요"
            }

            when{
                washerSum != 0 && dryerSum != 0 -> append(" 이외에도 세탁기 ${washerSum}대 건조기 ${dryerSum}대를 사용가능해요")
                washerSum != 0 -> append(" 이외에도 세탁기 ${washerSum}대를 사용가능해요")
                dryerSum != 0 -> append(" 이외에도 건조기 ${dryerSum}대를 사용가능해요")
                else -> append("을 사용가능해요")
            }
        }
    }

    fun startLaundry(selectLaundry: Laundry){
        if(timerMap.contains(selectLaundry)) return

        val runnableLaundry = object : java.lang.Runnable {
            override fun run() {
                try {
                    val now = LocalDateTime.now()

                    // endTime이 null인지 확인
                    if(selectLaundry.endTime == null) {
                        when(selectLaundry.washerType){
                            "WASHER" -> { _laundryTimeTxt.value = "--:--" }
                            else -> { _dryerTimeTxt.value = "--:--" }
                        }
                        timerStop(selectLaundry)
                        return
                    }

                    val duration = Duration.between(now, LocalDateTime.parse(selectLaundry.endTime))

                    // 시간이 종료된 경우
                    if (duration.isNegative || duration.isZero) {
                        onTimerEnd(selectLaundry, selectLaundry.washerType)

                    } else {
                        Log.d("startTimer", "update : ${selectLaundry}")
                        updateTimer(selectLaundry, duration)  // 타이머 업데이트
                        handler.postDelayed(this, 1000)  // 1초 후에 다시 실행
                    }
                } catch (e: Exception) {
                    Log.e("mine", "Error in timer: ${e.message}")
                }
            }
        }
        handler.post(runnableLaundry)
    }

    fun timerStop(selectLaundry: Laundry){
        timerMap[selectLaundry]?.let {
            handler.removeCallbacks(it)
            timerMap.remove(selectLaundry)
        }
    }

    fun updateTimer(selectLaundry: Laundry, duration: Duration) {
        val hours = duration.toHours()
        val minutes = (duration.toMinutes() % 60)

        when (selectLaundry.washerType) {
            "WASHER" -> _laundryTimeTxt.value = String.format("%02d : %02d", hours, minutes)
            "DRYER" -> _dryerTimeTxt.value = String.format("%02d : %02d", hours, minutes)
        }
    }

    fun onTimerEnd(selectLaundry: Laundry, laundryType: String) {
        timerStop(selectLaundry)  // 타이머 정지

        when(laundryType) {
            "WASHER" ->{
                UserInfo.useLaundry = null
                _laundryTimeTxt.value = "--:--"
            }
            "DRYER" -> {
                UserInfo.useDry = null
                _dryerTimeTxt.value = "--:--"
            }
        }

        selectLaundry.endTime = null
        selectLaundry.user = null
    }

    fun changePosition(newPosition : Int){
        _selectPosition.value = newPosition
    }
}