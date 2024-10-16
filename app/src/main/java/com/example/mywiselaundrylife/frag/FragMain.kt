package com.example.mywiselaundrylife.frag

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mywiselaundrylife.R
import com.example.mywiselaundrylife.adapter.LaundryRoomAdapter
import com.example.mywiselaundrylife.data.Laundry
import com.example.mywiselaundrylife.data.LaundryRoom
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toFastLaundry.setOnClickListener { goFast("세탁기") }
        binding.toFastDryer.setOnClickListener { goFast("건조기") }
    }

    private fun setupRecyclerView() {
        binding.recyclerview.layoutManager = GridLayoutManager(context, 2)
        binding.recyclerview.setHasFixedSize(true)
        binding.recyclerview.adapter =
            LaundryRoomAdapter(ListData.roomLst) { laundryRoom -> goToRoom(laundryRoom) }
    }

    private fun goFast(laundryType: String) {
//        val pos = ListData.laundryLst.filter {
//            laundryType in it.name && it.setTime >= 0
//        }.minByOrNull { it.setTime }

//        pos?.let {
//            goToRoom(ListData.roomLst[it.roomId.toInt() - 1])
//        } ?: Toast.makeText(requireActivity(), "이미 모든 세탁기가 사용중입니다", Toast.LENGTH_SHORT).show()
    }

    private fun goToRoom(laundryRoom: LaundryRoom) {
        UserInfo.currentRoom = laundryRoom
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
