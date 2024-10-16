package com.example.mywiselaundrylife.frag

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.annotation.RequiresApi
import com.example.mywiselaundrylife.serve.OnItemClickListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.mywiselaundrylife.adapter.LaundryAdapter
import com.example.mywiselaundrylife.data.Laundry
import com.example.mywiselaundrylife.data.ListData
import com.example.mywiselaundrylife.data.UserInfo
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
            it.roomId == UserInfo.currentRoom?.roomId
        } as ArrayList<Laundry>

        binding.recyclerview.layoutManager = GridLayoutManager(context, 2)
        binding.recyclerview.setHasFixedSize(true)

        laundryAdapter = LaundryAdapter(UserInfo.userLaundryLst){item ->
            listener?.onItemClicked(item.name) // 클릭된 아이템 이름 전달
        }
        binding.recyclerview.adapter = laundryAdapter

        binding.currentRoom.text = "현재 세탁실 : ${UserInfo.currentRoom?.roomName ?: "알 수 없음"}"
    }

    override fun onDetach() {
        super.onDetach()
        listener = null // 메모리 누수 방지
    }
}


