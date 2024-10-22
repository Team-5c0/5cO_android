package com.example.mywiselaundrylife.frag

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.mywiselaundrylife.R
import com.example.mywiselaundrylife.adapter.LaundryRoomAdapter
import com.example.mywiselaundrylife.data.ListData
import com.example.mywiselaundrylife.data.UserInfo
import com.example.mywiselaundrylife.databinding.FragmentLaundryRoomBinding

class FragMain : Fragment() {

    private lateinit var binding: FragmentLaundryRoomBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLaundryRoomBinding.inflate(inflater, container, false)

        setupRecyclerView()

        return binding.root
    }

    private fun setupRecyclerView() {
        Log.d("mine", "${ListData.roomLst}")
        binding.recyclerview.layoutManager = GridLayoutManager(context, 2)
        binding.recyclerview.setHasFixedSize(true)
        binding.recyclerview.adapter =
            LaundryRoomAdapter(ListData.roomLst) { Room -> goToRoom(Room.roomid) }
    }

    private fun goToRoom(roomId: Int) {
        UserInfo.currentRoom = roomId
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame, FragInRoom())
            .addToBackStack(null)
            .commit()
    }

    override fun onResume() {
        super.onResume()
        setupRecyclerView() // RecyclerView 갱신
    }
}
