package com.example.mywiselaundrylife.adapter

import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.mywiselaundrylife.R
import com.example.mywiselaundrylife.data.Laundry
import com.example.mywiselaundrylife.data.ListData
import com.example.mywiselaundrylife.data.UserInfo
import com.example.mywiselaundrylife.databinding.ItemLaundryBinding
import java.time.Duration
import java.time.LocalDateTime

class LaundryAdapter(private val laundryLst: ArrayList<Laundry>, private val onItemClick: (Laundry) -> Unit) :
    RecyclerView.Adapter<LaundryAdapter.LaundryViewHolder>() {

    inner class LaundryViewHolder(val binding: ItemLaundryBinding) : RecyclerView.ViewHolder(binding.root){
        val handler = Handler(Looper.getMainLooper())
        var runnable : Runnable? = null

        fun timerStart(endTime : LocalDateTime, selectLaundry: Laundry){

            runnable?.let { handler.removeCallbacks(it) }

            runnable = object : Runnable{
                @RequiresApi(VERSION_CODES.O)
                override fun run() {
                    val duration = Duration.between(LocalDateTime.now(), endTime)

                    // 시간이 종료된 경우
                    if (duration.isNegative || duration.isZero) {
                        onTimerEnd(selectLaundry, selectLaundry.name)
                    } else {
                        updateTimer(duration)
                    }
                }
            }
            handler.post(runnable!!)
        }
        fun timerStop(){
            runnable?.let { handler.removeCallbacks(it) }
        }

        @RequiresApi(VERSION_CODES.O)
        private fun updateTimer(duration: Duration){
            binding.view.setBackgroundResource(R.drawable.used_color)
            val hours = duration.toHours()
            val minutes = (duration.toMinutes() % 60)
            val seconds = (duration.seconds % 60)
            binding.remainTimeTxt.text = String.format("%02d시간 %02d분 %02d초 남음", hours, minutes, seconds)
            runnable?.let{handler.postDelayed(it, 1000)}
        }

        private fun onTimerEnd(selectLaundry: Laundry, laundryType : String){
            binding.remainTimeTxt.text = "사용자 없음"
            binding.view.setBackgroundResource(R.drawable.null_color)
            runnable?.let { handler.removeCallbacks(it) }
            when{
                laundryType.contains("세탁기") -> UserInfo.useLaundry = null
                laundryType.contains("건조기") -> UserInfo.useDry = null
            }
            selectLaundry.endTime = null
            selectLaundry.userId = null
        }
    }

    override fun getItemCount(): Int {
        return laundryLst.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LaundryViewHolder {
        val binding = ItemLaundryBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        // 뷰의 높이를 너비와 동일하게 설정
        binding.view.viewTreeObserver.addOnGlobalLayoutListener {
            val width = binding.view.width
            binding.view.layoutParams.height = width
            binding.view.requestLayout()
        }

        return LaundryViewHolder(binding)
    }

    @RequiresApi(VERSION_CODES.O)
    override fun onBindViewHolder(holder: LaundryViewHolder, position: Int) {
        val binding = holder.binding
        val laundry = laundryLst[position]
        val pos = ListData.laundryLst.indexOfFirst {
            it.roomId == laundry.roomId && it.laundryId == laundry.laundryId
        }

        if (laundry.endTime != null) {
            holder.timerStart(laundry.endTime!!, ListData.laundryLst[pos])
        } else {
            binding.view.setBackgroundResource(R.drawable.null_color)
        }

        binding.laundTitle.setText(laundry.name)

        binding.view.setOnClickListener { view ->
            handleLaundryClick(view, laundry, pos, holder)
        }
    }

    @RequiresApi(VERSION_CODES.O)
    private fun handleLaundryClick(view: View, laundry: Laundry, pos: Int, holder: LaundryViewHolder) {
        val binding = holder.binding

        if (isUsedLaundry(view, laundry, binding) == true) return

        if (pos != -1) {
            val selectLaundry = ListData.laundryLst[pos]
            updateUserAndEndTime(selectLaundry)

            // 사용자 정보 업데이트
            when{
                selectLaundry.name.contains("세탁기") ->UserInfo.useLaundry = selectLaundry
                selectLaundry.name.contains("건조기") ->UserInfo.useDry = selectLaundry
            }

            holder.timerStart(selectLaundry.endTime!!, selectLaundry)
        }
    }

    private fun isUsedLaundry(view : View, laundry: Laundry, binding: ItemLaundryBinding):Boolean{
        when {
            laundry.userId != null -> {
                Toast.makeText(view.context, "이미 사용중인 유저가 있습니다", Toast.LENGTH_SHORT).show()
                return true
            }

            binding.laundTitle.text.contains("세탁기") && UserInfo.useLaundry != null -> {
                Toast.makeText(view.context, "이미 사용중인 세탁기가 있습니다", Toast.LENGTH_SHORT).show()
                return true
            }

            binding.laundTitle.text.contains("건조기") && UserInfo.useDry != null -> {
                Toast.makeText(view.context, "이미 사용중인 건조기가 있습니다", Toast.LENGTH_SHORT).show()
                return true
            }
             else -> return false
        }
    }

    @RequiresApi(VERSION_CODES.O)
    private fun updateUserAndEndTime(selectLaundry: Laundry) {
        selectLaundry.endTime = LocalDateTime.of(2024, 10, 16, 14, 42, 30)
        selectLaundry.userId = UserInfo.userId
    }

    override fun onViewRecycled(holder: LaundryViewHolder) {
        super.onViewRecycled(holder)
        holder.timerStop()
    }
}
