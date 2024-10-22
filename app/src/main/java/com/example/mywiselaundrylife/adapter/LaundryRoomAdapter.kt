package com.example.mywiselaundrylife.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.mywiselaundrylife.data.ListData
import com.example.mywiselaundrylife.data.base.Room
import com.example.mywiselaundrylife.databinding.ItemLaundryRoomBinding

class LaundryRoomAdapter(
    private val roomLst: ArrayList<Room>,
    private val onItemClick: (Room) -> Unit
) : RecyclerView.Adapter<LaundryRoomAdapter.LaundryRoomViewHolder>() {

    inner class LaundryRoomViewHolder(val binding: ItemLaundryRoomBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LaundryRoomViewHolder {
        val binding =
            ItemLaundryRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        binding.view.layoutParams.height = binding.view.width

        binding.view.viewTreeObserver.addOnGlobalLayoutListener {
            val width = binding.view.width
            binding.view.layoutParams.height = width // 높이를 너비로 설정
            binding.view.requestLayout() // 레이아웃 업데이트 요청
        }

        return LaundryRoomViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return roomLst.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: LaundryRoomViewHolder, position: Int) {
        val binding = holder.binding
        val room = roomLst[position]

        val remainLaundry = remainSum(room, "WASHER")
        val remainDryer = remainSum(room, "DRYER")

        binding.view.setOnClickListener {
            onItemClick(room)
        }

        with(binding) {
            laundryRoom.text = "세탁실 2층"
            remainLaundryTxt.setText(remainLaundry.toString())
            remainDryerTxt.setText(remainDryer.toString())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun remainSum(laundryRoom: Room, laundryType: String): Int {
        var sum = 0

        for (laundry in ListData.laundryLst) {
            if (laundryRoom.roomid == laundry.roomId && laundryType == laundry.washerType && laundry.available == true) {
                sum++
            }
        }

        return sum
    }
}