package com.example.mywiselaundrylife.frag

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.example.mywiselaundrylife.serve.OnItemClickListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.mywiselaundrylife.act.FCMActivity
import com.example.mywiselaundrylife.act.RefreshData
import com.example.mywiselaundrylife.adapter.LaundryAdapter
import com.example.mywiselaundrylife.data.laundry.ListData
import com.example.mywiselaundrylife.data.user.UserInfo
import com.example.mywiselaundrylife.data.base.Laundry
import com.example.mywiselaundrylife.databinding.FragmentLaundryBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FragInRoom : Fragment() {

    private lateinit var binding: FragmentLaundryBinding
    private lateinit var laundryAdapter: LaundryAdapter
    private var listener: OnItemClickListener? = null
    private var job: Job? = null

    @RequiresApi(Build.VERSION_CODES.O)
    fun startCoroutineTimer() {
        Log.d("requireTimer", "timer 시작")
        job = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {

                setRecyclerView()

                RefreshData.roomRequest()

                // MainActivity 뷰 갱신
                (activity as? FCMActivity)?.updateView()

                Log.d("requireTimer", "timer 갱신")

                // 1분 대기
                delay(1000)
                Log.d("reset", "갱신")
            }
        }
    }

    fun stopCoroutineTimer() {
        job?.cancel() // 코루틴 중지
        Log.d("requireTimer", "timer 정지")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnItemClickListener) {
            listener = context // MainActivity와 연결
        } else {
            throw RuntimeException("$context must implement OnItemClickListener")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentLaundryBinding.inflate(inflater, container, false)

        setRecyclerView()

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setRecyclerView() {
        UserInfo.userLaundryLst = ListData.laundryLst.filter {
            it.roomId == UserInfo.currentRoom
        } as ArrayList<Laundry>

        if(!::laundryAdapter.isInitialized){
            binding.recyclerview.layoutManager = GridLayoutManager(context, 3)
            binding.recyclerview.setHasFixedSize(true)

            laundryAdapter = LaundryAdapter(UserInfo.userLaundryLst){ item ->
                listener?.onItemClicked(item)
            }
            binding.recyclerview.adapter = laundryAdapter
        } else{
            laundryAdapter.updateData()
        }
//        binding.currentRoom.text = "현재 세탁실 : ${UserInfo.currentRoom}"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        setRecyclerView()
    }

    override fun onDetach() {
        super.onDetach()
        listener = null // 메모리 누수 방지
    }

    override fun onDestroyView() {
        super.onDestroyView()
        laundryAdapter.stopAllTimers()  // View가 파괴될 때 타이머 정지
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
        startCoroutineTimer()
    }

    override fun onPause() {
        super.onPause()
        stopCoroutineTimer()
    }

    override fun onDestroy() {
        super.onDestroy()
        laundryAdapter.stopAllTimers()  // View가 파괴될 때 타이머 정지
    }

}