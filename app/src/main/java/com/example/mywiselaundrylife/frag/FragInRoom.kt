package com.example.mywiselaundrylife.frag

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.example.mywiselaundrylife.serve.OnItemClickListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.mywiselaundrylife.adapter.LaundryAdapter
import com.example.mywiselaundrylife.data.laundry.ListData
import com.example.mywiselaundrylife.data.user.UserInfo
import com.example.mywiselaundrylife.data.base.Laundry
import com.example.mywiselaundrylife.databinding.FragmentLaundryBinding

class FragInRoom : Fragment() {

    private lateinit var binding: FragmentLaundryBinding
    private lateinit var laundryAdapter: LaundryAdapter
    private var listener: OnItemClickListener? = null

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

        binding.backBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setRecyclerView() {
        UserInfo.userLaundryLst = ListData.laundryLst.filter {
            it.roomId == UserInfo.currentRoom
        } as ArrayList<Laundry>

        binding.recyclerview.layoutManager = GridLayoutManager(context, 2)
        binding.recyclerview.setHasFixedSize(true)

        laundryAdapter = LaundryAdapter(UserInfo.userLaundryLst){ item ->
            listener?.onItemClicked(item)
        }

        binding.recyclerview.adapter = laundryAdapter

        binding.currentRoom.text = "현재 세탁실 : ${UserInfo.currentRoom}"
    }

    override fun onDetach() {
        super.onDetach()
        listener = null // 메모리 누수 방지
    }

    override fun onPause() {
        super.onPause()
        laundryAdapter.stopAllTimers()  // 프래그먼트가 일시정지될 때 타이머 정지
    }

    override fun onDestroyView() {
        super.onDestroyView()
        laundryAdapter.stopAllTimers()  // View가 파괴될 때 타이머 정지
    }

}


