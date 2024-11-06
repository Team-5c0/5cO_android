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
    private val laundryLst: ArrayList<Laundry>,
    private val onItemClick: (Laundry) -> Unit
) : RecyclerView.Adapter<LaundryAdapter.LaundryViewHolder>() {

    var pos = -1
    private val viewHolders = mutableListOf<LaundryViewHolder>()

    inner class LaundryViewHolder(val binding: ItemLaundryBinding) : RecyclerView.ViewHolder(binding.root) {
        private val handler = Handler(Looper.getMainLooper())
        var runnable: Runnable? = null

        fun timerStart(selectLaundry: Laundry) {
            // 이전 타이머 정지
            Log.d("timer", "${selectLaundry}")
            timerStop()

            runnable = object : Runnable {
                @RequiresApi(VERSION_CODES.O)
                override fun run() {
                    Log.d("timer", "run")
                    try {
                        val currentTime = LocalDateTime.now().withNano(0)

                        // endTime이 null인지 확인
                        if (selectLaundry.endTime == null) {
                            Log.d("timer", "${selectLaundry.endTime}")
                            binding.remainTimeTxt.text = "사용자 없음"
//                            binding.view.setBackgroundResource(R.drawable.null_color)
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
                            updateTimer(duration, elapsedDuration, totalDuration)  // 타이머 업데이트
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
            runnable?.let {
                handler.removeCallbacks(it)
                binding?.prgBar?.progress = 0  // binding이 null이 아닌 경우에만 설정
            }
            runnable = null
        }


        @RequiresApi(VERSION_CODES.O)
        private fun updateTimer(duration: Duration, elapsedDuration: Long, totalDuration: Long) {
//            binding.view.setBackgroundResource(R.drawable.used_color)

            val progress = ((elapsedDuration.toDouble() / totalDuration) * 1000).toInt().coerceIn(0, 1000)

            val hours = duration.toHours()
            val minutes = (duration.toMinutes() % 60)

            binding.prgBar.progress = progress
            binding.remainTimeTxt.text = String.format("%02d : %02d", hours, minutes)

            Log.d("progress", "${binding.prgBar.progress}")
            Log.d("mine", String.format("%02d : %02d", hours, minutes))
        }

        private fun onTimerEnd(selectLaundry: Laundry, laundryType: String) {
            binding.remainTimeTxt.text = "사용자 없음"
//            binding.view.setBackgroundResource(R.drawable.null_color)
            timerStop()  // 타이머 정지

            if(selectLaundry.user == UserInfo.userId){
                when {
                    laundryType.contains("WASHER") -> UserInfo.useLaundry = null
                    laundryType.contains("DRYER") -> UserInfo.useDry = null
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

        binding.bigCir.text = laundry.washerId.toString()
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
        } else {
//            binding.view.setBackgroundResource(R.drawable.null_color)
        }

        binding.prgBar.startAnimation()
        binding.laundTitle.text = laundry.washerType

        binding.view.setOnClickListener { view ->
            laundryClick(view, laundry, pos, holder)
        }
    }

    @RequiresApi(VERSION_CODES.O)
    private fun laundryClick(view: View, laundry: Laundry, pos: Int, holder: LaundryViewHolder) {
        val binding = holder.binding

        if (isUsedLaundry(view, laundry, binding)) return

        if (pos != -1) {
            val selectLaundry = ListData.laundryLst[pos]
            updateUserAndEndTime(selectLaundry)

            // 사용자 정보 업데이트
            when {
                selectLaundry.washerType.contains("WASHER") -> UserInfo.useLaundry = selectLaundry
                selectLaundry.washerType.contains("DRYER") -> UserInfo.useDry = selectLaundry
            }

            onItemClick(selectLaundry)
            holder.timerStart(selectLaundry) // 타이머 시작
        }
    }

    private fun isUsedLaundry(view: View, laundry: Laundry, binding: ItemLaundryBinding): Boolean {
        val context = view.context

        return when {
            laundry.user != null -> {
                Toast.makeText(context, "이미 사용중인 유저가 있습니다", Toast.LENGTH_SHORT).show()
                true
            }
            binding.laundTitle.text.contains("WASHER") && UserInfo.useLaundry != null -> {
                Toast.makeText(context, "이미 사용중인 세탁기가 있습니다", Toast.LENGTH_SHORT).show()
                true
            }
            binding.laundTitle.text.contains("DRYER") && UserInfo.useDry != null -> {
                Toast.makeText(context, "이미 사용중인 건조기가 있습니다", Toast.LENGTH_SHORT).show()
                true
            }
            else -> false
        }
    }

    @RequiresApi(VERSION_CODES.O)
    private fun updateUserAndEndTime(selectLaundry: Laundry) {
        // 현재 시간에 사용자 지정 초를 더하는 방식으로 설정
        selectLaundry.startTime = LocalDateTime.now().withNano(0).toString()
        selectLaundry.endTime = LocalDateTime.now().withNano(0).plusSeconds(UserInfo.seconds.toLong()).toString()
        selectLaundry.available = false
        selectLaundry.user = UserInfo.userId
    }

    override fun onViewRecycled(holder: LaundryViewHolder) {
        super.onViewRecycled(holder)
        Log.d("timer", "viewRecycled")
        holder.timerStop()  // 뷰홀더가 재활용될 때 타이머 정지
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
}