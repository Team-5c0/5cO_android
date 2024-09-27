package com.example.mywiselaundrylife.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mywiselaundrylife.data.LaundryRoom
import com.example.mywiselaundrylife.databinding.ItemLaundryRoomBinding

class LaundryRoomAdapter(private val roomLst : ArrayList<LaundryRoom>) : RecyclerView.Adapter<LaundryRoomAdapter.LaundryRoomViewHolder>() {

    inner class LaundryRoomViewHolder(val binding : ItemLaundryRoomBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LaundryRoomViewHolder {
        val binding = ItemLaundryRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LaundryRoomViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return roomLst.size
    }

    override fun onBindViewHolder(holder: LaundryRoomViewHolder, position: Int) {
        val binding = holder.binding
    }
}