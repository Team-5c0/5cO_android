package com.example.mywiselaundrylife.frag

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.mywiselaundrylife.R
import com.example.mywiselaundrylife.act.FCMActivity
import com.example.mywiselaundrylife.act.RefreshData
import com.example.mywiselaundrylife.adapter.LaundryRoomAdapter
import com.example.mywiselaundrylife.data.auth.AuthRequestManager.fcmTokenRequest
import com.example.mywiselaundrylife.data.laundry.LaundryRequestMananger
import com.example.mywiselaundrylife.data.laundry.ListData
import com.example.mywiselaundrylife.data.user.UserInfo
import com.example.mywiselaundrylife.databinding.FragmentLaundryRoomBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.launch
import retrofit2.HttpException

class FragMain : Fragment() {

    private lateinit var binding: FragmentLaundryRoomBinding
    private var job: Job? = null

    @RequiresApi(Build.VERSION_CODES.O)
    fun startCoroutineTimer() {
        Log.d("requireTimer", "timer 시작")
        job = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                // 실행할 작업

                val result = async { RefreshData.roomRequest() }
                awaitAll(result)

                // fragment ResuclerView 갱신
                setupRecyclerView()

                // MainActivity 뷰 갱신
                (activity as FCMActivity).updateView()

                Log.d("requireTimer", "timer 갱신")

                // 1분 대기
                delay(10000)
            }
        }
    }

    fun stopCoroutineTimer() {
        job?.cancel() // 코루틴 중지
        Log.d("requireTimer", "timer 정지")
    }

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
        Log.d("mine", "갱신 ${ListData.roomLst}")
        binding.recyclerview.layoutManager = GridLayoutManager(context, 2)
        binding.recyclerview.setHasFixedSize(true)
        binding.recyclerview.adapter =
            LaundryRoomAdapter(ListData.roomLst) { Room -> goToRoom(Room.roomid) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun goToRoom(roomId: String) {
        UserInfo.currentRoom = roomId

        refreshRoom(roomId, true)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshRoom(roomId: String, boolean: Boolean) : Boolean {

        lifecycleScope.launch {
            try {
                RefreshData.washerRequest(roomId)
                if(boolean){
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.frame, FragInRoom())
                        .addToBackStack(null)
                        .commit()
                } else{
                    setupRecyclerView()
                }
            } catch (e : HttpException){
                Toast.makeText(requireActivity(), "서버에서 값을 받아올 수 없음", Toast.LENGTH_SHORT).show()
            } catch (e : Exception){
                Toast.makeText(requireActivity(), "알 수 없는 버그 발생", Toast.LENGTH_SHORT).show()
            }
        }

        return true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
        startCoroutineTimer()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        setupRecyclerView() // RecyclerView 갱신
    }

    override fun onPause() {
        super.onPause()
        stopCoroutineTimer()
    }
}
