package com.example.mywiselaundrylife.adapter

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.mywiselaundrylife.R
import com.example.mywiselaundrylife.diffutil.WasherDiffUtil
import com.example.mywiselaundrylife.data.user.UserInfo
import com.example.mywiselaundrylife.data.base.Laundry
import com.example.mywiselaundrylife.databinding.ItemLaundryBinding
import java.time.Duration
import java.time.LocalDateTime

class LaundryAdapter(
    private val laundryLst: ArrayList<Laundry>
) : RecyclerView.Adapter<LaundryAdapter.LaundryViewHolder>() {

    private val viewHolders = mutableListOf<LaundryViewHolder>()

    inner class LaundryViewHolder(val binding: ItemLaundryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val handler = Handler(Looper.getMainLooper())
        private var runnable: Runnable? = null

        fun timerStart(selectLaundry: Laundry) {
            // 이전 타이머 정지
            timerStop()

            // 애니메이션 상태 중복 호출 방지
            when (selectLaundry.washerType) {
                "DRYER" -> {
                    if (!binding.windLottie.isAnimating) {
                        binding.windLottie.setAnimation(R.raw.using_animation)
                        binding.windLottie.playAnimation()
                    }
                }
            }

            runnable = object : Runnable {
                override fun run() {
                    Log.d("timer${binding.washerId.text}", "run")
                    try {
                        val currentTime = LocalDateTime.now().withNano(0)

                        val startTime = LocalDateTime.parse(selectLaundry.startTime)
                        val endTime = LocalDateTime.parse(selectLaundry.endTime)

                        val duration = Duration.between(currentTime, endTime)
                        val elapsedDuration = Duration.between(startTime, currentTime).toMillis()
                        val totalDuration = Duration.between(startTime, endTime).toMillis()

                        // 시간이 종료된 경우
                        if (duration.isNegative || duration.isZero) {
                            onTimerEnd(selectLaundry, selectLaundry.washerType)
                        } else {
                            updateTimer(duration, elapsedDuration, totalDuration) // 타이머 업데이트
                            if (Looper.myLooper() != Looper.getMainLooper()) {
                                Handler(Looper.getMainLooper()).post {
                                    handler.post(this)
                                }
                                return
                            }
                            handler.postDelayed(this, 1000)  // 1초 후에 다시 실행
                        }
                    } catch (e: Exception) {
                        Log.e("timer${binding.washerId.text}", "Error in timer: ${e.message}")
                    }
                }
            }
            handler.post(runnable!!)
        }


        fun timerStop() {
            Log.d("timer${binding.washerId.text}", "timer Stop")

            // 핸들러 작업 제거
            runnable?.let {
                handler.removeCallbacks(it)
            }
            runnable = null

            // 애니메이션 취소
            binding.windLottie.cancelAnimation()
            binding.windLottie.setAnimation(R.raw.not_using_animation)

            binding.prgBar.clearAnimation()
            binding.prgBar.progress = 0
        }

        private fun updateTimer(duration: Duration, elapsedDuration: Long, totalDuration: Long) {
            val hours = duration.toHours()
            val minutes = (duration.toMinutes() % 60)

            val progress =
                ((elapsedDuration.toDouble() / totalDuration) * 1000).toInt().coerceIn(0, 1000)
            binding.prgBar.progress = progress
            binding.remainTimeTxt.text = String.format("%02d : %02d", hours, minutes)
        }

        private fun onTimerEnd(selectLaundry: Laundry, laundryType: String) {
            binding.remainTimeTxt.text = "00 : 00"
            timerStop()  // 타이머 정지

            if (selectLaundry.user == UserInfo.userId) {
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

    fun stopAllTimers() {
        viewHolders.forEach {
            it.timerStop()
        }
    }

    fun updateData() {
        val diffCallback = WasherDiffUtil(laundryLst, UserInfo.userLaundryLst)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        Log.d("updateData", "$laundryLst")
        Log.d("updateData", "${UserInfo.userLaundryLst}")

        laundryLst.clear()
        laundryLst.addAll(UserInfo.userLaundryLst)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemCount(): Int = laundryLst.size

    // 홀더 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LaundryViewHolder {
        val binding = ItemLaundryBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        // 생성된 뷰홀더를 리스트에 추가
        val holder = LaundryViewHolder(binding)
        Log.d("holder", "$holder")
        viewHolders.add(holder)

        return holder
    }

    // 홀더 바인딩
    override fun onBindViewHolder(holder: LaundryViewHolder, position: Int) {
        val binding = holder.binding
        val laundry = laundryLst[position]

        Log.d("bindView", "$laundry")

        binding.washerId.text = laundry.washerId.toString()

        // 뷰 초기화
        binding.prgBar.clearAnimation()
        binding.windLottie.cancelAnimation()

        // laundry.available == false
        if (!laundry.available) {
            holder.timerStart(laundry)
        } else {
            holder.timerStop()
        }

        when (laundry.washerType) {
            "WASHER" -> {
                binding.windLottie.visibility = View.INVISIBLE
                binding.prgBar.visibility = View.VISIBLE
            }

            "DRYER" -> {
                binding.prgBar.visibility = View.INVISIBLE
                binding.windLottie.visibility = View.VISIBLE
            }
        }
    }

    override fun onViewRecycled(holder: LaundryViewHolder) {
        super.onViewRecycled(holder)
        holder.timerStop()
        holder.binding.prgBar.clearAnimation()
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        stopAllTimers()
    }
}