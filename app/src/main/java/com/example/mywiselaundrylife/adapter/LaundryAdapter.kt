package com.example.mywiselaundrylife.adapter

import android.os.Build.VERSION_CODES
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.mywiselaundrylife.R
import com.example.mywiselaundrylife.data.WasherDiffUtil
import com.example.mywiselaundrylife.data.laundry.ListData
import com.example.mywiselaundrylife.data.user.UserInfo
import com.example.mywiselaundrylife.data.base.Laundry
import com.example.mywiselaundrylife.databinding.ItemLaundryBinding
import java.time.Duration
import java.time.LocalDateTime

class LaundryAdapter(
    private val laundryLst: ArrayList<Laundry>
) : RecyclerView.Adapter<LaundryAdapter.LaundryViewHolder>() {

    var pos = -1
    private val viewHolders = mutableListOf<LaundryViewHolder>()

    inner class LaundryViewHolder(val binding: ItemLaundryBinding) : RecyclerView.ViewHolder(binding.root) {
        private val handler = Handler(Looper.getMainLooper())
        var runnable: Runnable? = null

        fun timerStart(selectLaundry: Laundry) {
            // 이전 타이머 정지
            timerStop()

            Log.d("timer", "${selectLaundry}")
            if(selectLaundry.washerType == "DRYER"){
                binding.windLottie.setAnimation(R.raw.using_animation)
                binding.windLottie.playAnimation()
            }

            runnable = object : Runnable {
                @RequiresApi(VERSION_CODES.O)
                override fun run() {
                    Log.d("timer", "run")
                    try {
                        val currentTime = LocalDateTime.now().withNano(0)

                        // endTime이 null인지 확인
                        if (selectLaundry.endTime == null) {
                            Log.d("timer", "${selectLaundry.endTime}")
                            binding.remainTimeTxt.text = "00 : 00"
                            timerStop()
                            return
                        }

                        val startTime = LocalDateTime.parse(selectLaundry.startTime)
                        val endTime = LocalDateTime.parse(selectLaundry.endTime)

                        val duration = Duration.between(currentTime, endTime)
                        val elapsedDuration = Duration.between(startTime, currentTime).toMillis()
                        val totalDuration = Duration.between(startTime, endTime).toMillis()

                        // 시간이 종료된 경우
                        if (duration.isNegative || duration.isZero) {
                            Log.d("timer", "onTimerEnd")
                            onTimerEnd(selectLaundry, selectLaundry.washerType)
                        } else {
                            Log.d("timer", "update")
                            updateTimer(duration, elapsedDuration, totalDuration) // 타이머 업데이트
                            handler.postDelayed(this, 1000)  // 1초 후에 다시 실행
                        }
                    } catch (e: Exception) {
                        Log.e("timer", "Error in timer: ${e.message}")
                    }
                }
            }
            handler.post(runnable!!)  // runnable 시작
        }

        fun timerStop() {

            binding.windLottie.cancelAnimation()
            binding.windLottie.setAnimation(R.raw.not_using_animation)

            runnable?.let {
                handler.removeCallbacks(it)
                binding?.prgBar?.progress = 0  // binding이 null이 아닌 경우에만 설정
            }
            runnable = null
        }


        @RequiresApi(VERSION_CODES.O)
        private fun updateTimer(duration: Duration, elapsedDuration: Long, totalDuration: Long) {

            val progress = ((elapsedDuration.toDouble() / totalDuration) * 1000).toInt().coerceIn(0, 1000)

            val hours = duration.toHours()
            val minutes = (duration.toMinutes() % 60)

            binding.prgBar.progress = progress
            binding.remainTimeTxt.text = String.format("%02d : %02d", hours, minutes)

            Log.d("timer", "${binding.prgBar.progress}")
            Log.d("timer", String.format("%02d : %02d", hours, minutes))
        }

        private fun onTimerEnd(selectLaundry: Laundry, laundryType: String) {
            binding.remainTimeTxt.text = "00 : 00"
//            binding.view.setBackgroundResource(R.drawable.null_color)
            timerStop()  // 타이머 정지

            if(selectLaundry.user == UserInfo.userId){
                when {
                    laundryType.contains("WASHER") -> UserInfo.useLaundry = null
                    laundryType.contains("DRYER") -> {
                        UserInfo.useDry = null
                        binding.windLottie.cancelAnimation()
                        binding.windLottie.setAnimation(R.raw.not_using_animation)
                    }
                }
            }
            selectLaundry.startTime = null
            selectLaundry.endTime = null
            selectLaundry.user = null
        }
    }

    override fun getItemCount(): Int = laundryLst.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LaundryViewHolder {
        val binding = ItemLaundryBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        // 생성된 뷰홀더를 리스트에 추가
        val holder = LaundryViewHolder(binding)
        viewHolders.add(holder)

        return holder
    }

    @RequiresApi(VERSION_CODES.O)
    override fun onBindViewHolder(holder: LaundryViewHolder, position: Int) {
        pos = position

        val binding = holder.binding
        val laundry = laundryLst[position]

        binding.washerId.text = laundry.washerId.toString()

        val pos = ListData.laundryLst.indexOfFirst {
            it.roomId == laundry.roomId && it.washerId == laundry.washerId
        }

        if (laundry.available == false) {
//            binding.view.setBackgroundResource(R.drawable.used_color)
            Log.d("timer", "available false")
            if(holder.runnable == null){
                holder.timerStart(ListData.laundryLst[pos])
            } else{
                Log.d("timer", "available true")
                holder.timerStop()
            }
        }

        binding.laundTitle.text = laundry.washerType

        if(binding.laundTitle.text == "WASHER"){
            binding.windLottie.visibility = View.INVISIBLE
            binding.prgBar.visibility = View.VISIBLE
        }else if(binding.laundTitle.text == "DRYER") {
            binding.prgBar.visibility = View.INVISIBLE
            binding.windLottie.visibility = View.VISIBLE
        }
    }

    override fun onViewRecycled(holder: LaundryViewHolder) {
        super.onViewRecycled(holder)
        Log.d("timer", "viewRecycled")
        holder.timerStop() // 뷰홀더가 재활용될 때 타이머 정지
        holder.binding.windLottie.cancelAnimation()
    }

    fun stopAllTimers() {
        viewHolders.forEach { it.timerStop() }
    }

    fun updateData(){
        val diffCallback = WasherDiffUtil(laundryLst, UserInfo.userLaundryLst)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        Log.d("updateData", "${laundryLst}")
        Log.d("updateData", "${UserInfo.userLaundryLst}")

        laundryLst.clear()
        laundryLst.addAll(UserInfo.userLaundryLst)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onViewAttachedToWindow(holder: LaundryViewHolder) {
        super.onViewAttachedToWindow(holder)
        Log.d("visible", "visible")
    }
}