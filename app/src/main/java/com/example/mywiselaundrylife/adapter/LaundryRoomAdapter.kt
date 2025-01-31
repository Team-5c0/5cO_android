package com.example.mywiselaundrylife.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.mywiselaundrylife.R
import com.example.mywiselaundrylife.data.base.Room
import com.example.mywiselaundrylife.data.user.UserInfo
import com.example.mywiselaundrylife.databinding.ItemLaundryRoomBinding
import com.example.mywiselaundrylife.vw.FCMActVM

class LaundryRoomAdapter(
    private val roomLst: ArrayList<Room>,
    private val onItemClick: (Room) -> Unit,
    private val viewModel : FCMActVM
) : RecyclerView.Adapter<LaundryRoomAdapter.LaundryRoomViewHolder>() {

    inner class LaundryRoomViewHolder(private val binding: ItemLaundryRoomBinding) : RecyclerView.ViewHolder(binding.root){

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

            binding.view.isSelected = viewModel.selectPosition.value == position

            binding.view.setOnClickListener {
                val previousPosition = viewModel.selectPosition.value

                viewModel.changePosition(position)
                if (previousPosition == viewModel.selectPosition.value){
                    return@setOnClickListener
                }else {
                    notifyItemChanged(previousPosition)
                    notifyItemChanged(position)
                    onItemClick(room)
                }
            }

            with(binding) {
                laundryRoom.text = room.roomid
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

    override fun onBindViewHolder(holder: LaundryRoomViewHolder, position: Int) {
        val room = roomLst[position]

        holder.bind(room, position)
    }
}