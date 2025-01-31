package com.example.mywiselaundrylife.frag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FragInRoom : Fragment() {

    private lateinit var binding: FragmentLaundryBinding
    private lateinit var laundryAdapter: LaundryAdapter
    private var job: Job? = null

    fun startCoroutineTimer() {
        job = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {

                setRecyclerView()

                RefreshData.roomRequest()

                // MainActivity 뷰 갱신
                (activity as? FCMActivity)?.updateView()

                delay(1000)
            }
        }
    }

    fun stopCoroutineTimer() {
        job?.cancel() // 코루틴 중지
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentLaundryBinding.inflate(inflater, container, false)

        setRecyclerView()

        return binding.root
    }

    private fun setRecyclerView() {
        UserInfo.userLaundryLst = ListData.laundryLst.filter {
            it.roomId == UserInfo.currentRoom
        } as ArrayList<Laundry>

        if(!::laundryAdapter.isInitialized){
            binding.recyclerview.layoutManager = GridLayoutManager(context, 3)
            binding.recyclerview.setHasFixedSize(true)

            laundryAdapter = LaundryAdapter(UserInfo.userLaundryLst)
            binding.recyclerview.adapter = laundryAdapter
        } else{
            laundryAdapter.updateData()
        }
    }

    override fun onResume() {
        super.onResume()
        startCoroutineTimer()
    }

    override fun onStart() {
        super.onStart()
        setRecyclerView()
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