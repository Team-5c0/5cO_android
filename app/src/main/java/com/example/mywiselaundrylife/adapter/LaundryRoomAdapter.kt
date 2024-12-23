package com.example.mywiselaundrylife.adapter

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.mywiselaundrylife.R
import com.example.mywiselaundrylife.data.laundry.ListData
import com.example.mywiselaundrylife.data.base.Room
import com.example.mywiselaundrylife.data.user.UserInfo
import com.example.mywiselaundrylife.databinding.ItemLaundryRoomBinding

class LaundryRoomAdapter(
    private val roomLst: ArrayList<Room>,
    private val onItemClick: (Room) -> Unit
) : RecyclerView.Adapter<LaundryRoomAdapter.LaundryRoomViewHolder>() {

    private var selectedPosition: Int = 0

    inner class LaundryRoomViewHolder(val binding: ItemLaundryRoomBinding) : RecyclerView.ViewHolder(binding.root){

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(room: Room, position: Int){

            if(room.roomid == UserInfo.currentRoom){
                val color = ContextCompat.getColor(itemView.context, R.color.notSelectBtn)
                binding.laundryRoom.setTextColor(color)
                binding.view.isSelected = true
            } else{
                val color = ContextCompat.getColor(itemView.context, R.color.selectBtn)
                binding.laundryRoom.setTextColor(color)
                binding.view.isSelected = false
            }

            binding.view.isSelected = (selectedPosition == position)

            val remainLaundry = remainSum(room, "WASHER")
            val remainDryer = remainSum(room, "DRYER")

            binding.view.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = if (selectedPosition == position){
                    return@setOnClickListener
                }else {
                    position
                }
                notifyItemChanged(previousPosition)
                notifyItemChanged(position)
                onItemClick(room)
            }

            with(binding) {
                laundryRoom.text = room.roomid
                remainLaundryTxt.setText(remainLaundry.toString())
                remainDryerTxt.setText(remainDryer.toString())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LaundryRoomViewHolder {
        val binding =
            ItemLaundryRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return LaundryRoomViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return roomLst.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: LaundryRoomViewHolder, position: Int) {
        val room = roomLst[position]

        holder.bind(room, position)
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