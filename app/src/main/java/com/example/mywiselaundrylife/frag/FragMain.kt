package com.example.mywiselaundrylife.frag

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.mywiselaundrylife.R
import com.example.mywiselaundrylife.adapter.LaundryRoomAdapter
import com.example.mywiselaundrylife.data.auth.AuthRequestManager.fcmTokenRequest
import com.example.mywiselaundrylife.data.laundry.LaundryRequestMananger
import com.example.mywiselaundrylife.data.laundry.ListData
import com.example.mywiselaundrylife.data.user.UserInfo
import com.example.mywiselaundrylife.databinding.FragmentLaundryRoomBinding
import kotlinx.coroutines.launch

class FragMain : Fragment() {

    private lateinit var binding: FragmentLaundryRoomBinding

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLaundryRoomBinding.inflate(inflater, container, false)

        setupRecyclerView()

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupRecyclerView() {
        Log.d("mine", "${ListData.roomLst}")
        binding.recyclerview.layoutManager = GridLayoutManager(context, 2)
        binding.recyclerview.setHasFixedSize(true)
        binding.recyclerview.adapter =
            LaundryRoomAdapter(ListData.roomLst) { Room -> goToRoom(Room.roomid) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun goToRoom(roomId: String) {
        UserInfo.currentRoom = roomId

        if (refreshRoom(roomId)){
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame, FragInRoom())
                .addToBackStack(null)
                .commit()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshRoom(roomId: String) : Boolean {
        return true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        setupRecyclerView() // RecyclerView 갱신
    }
}
