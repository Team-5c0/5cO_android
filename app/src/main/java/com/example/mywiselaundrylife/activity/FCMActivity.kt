package com.example.mywiselaundrylife.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mywiselaundrylife.R
import com.example.mywiselaundrylife.adapter.LaundryRoomAdapter
import com.example.mywiselaundrylife.data.user.UserInfo
import com.example.mywiselaundrylife.databinding.ActivityMainBinding
import com.example.mywiselaundrylife.fragment.FragmentInRoom
import com.example.mywiselaundrylife.service.CustomSnapHelper
import com.example.mywiselaundrylife.viewmodel.FCMActVM
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FCMActivity : AppCompatActivity() {

    private var backPressedTime: Long = 0
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityMainBinding
    private val viewModel: FCMActVM by viewModels()

    private val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        Log.d("login", "${UserInfo.useLaundry}")

        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        UserInfo.currentRoom = UserInfo.lstData.roomLst[0].roomid

        binding.roomFrame.layoutManager = layoutManager
        binding.roomFrame.setHasFixedSize(true)
        binding.roomFrame.adapter = LaundryRoomAdapter(UserInfo.lstData.roomLst,
            viewModel = viewModel,
            onItemClick = { getRoom ->
                UserInfo.currentRoom = getRoom.roomid

                // fragment 재호출
                supportFragmentManager.beginTransaction()
                    .replace(R.id.laundryFrame, FragmentInRoom()).commit()
            })

        val pagerSnapHelper = CustomSnapHelper()
        pagerSnapHelper.attachToRecyclerView(binding.roomFrame)

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        binding.logOutBtn.setOnClickListener {
            editor.remove("isLogin")
            editor.remove("myId")
            editor.apply()

            logOut()

            startActivity(Intent(this, StartActivity::class.java))
            finish()
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.laundryFrame, FragmentInRoom())
                .commit()
        }

        lifecycleScope.launch {
            viewModel.remainWasherSum()

            viewModel.laundryTimeTxt.collectLatest { value ->
                binding.laundryTime.text = value
            }
            viewModel.dryerTimeTxt.collectLatest { value ->
                binding.dryerTime.text = value
            }
            viewModel.remainWasherTxt.collectLatest { value ->
                binding.remainWashersTxt.text = value
            }

            UserInfo.useLaundry?.let {
                viewModel.startLaundry(it)
            }
            UserInfo.useDry?.let {
                viewModel.startLaundry(it)
            }
        }
    }

    override fun onBackPressed() {
        Log.d("mine", "${supportFragmentManager.backStackEntryCount}")
        when {
            backPressedTime + 2000 > System.currentTimeMillis() && supportFragmentManager.backStackEntryCount <= 0 -> {

                logOut()

                super.onBackPressed()
                return
            }

            supportFragmentManager.backStackEntryCount > 0 -> {
                supportFragmentManager.popBackStack()
            }

            else -> Toast.makeText(this, "뒤로 버튼을 한 번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show()
        }
        backPressedTime = System.currentTimeMillis()
    }

    fun updateView() {
        listOf(UserInfo.useLaundry, UserInfo.useDry).forEach { washer ->
            washer?.let {
                viewModel.startLaundry(it)
            }
        }
        viewModel.remainWasherSum()
    }

    private fun logOut() {
        UserInfo.useLaundry?.let {
            viewModel.timerStop(it)
        }
        UserInfo.useDry?.let {
            viewModel.timerStop(it)
        }

        UserInfo.resetUserInfo()
    }

    override fun onPause() {
        super.onPause()
        Log.d("lifeTime", "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d("lifeTime", "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("lifeTime", "onDestroy")
    }
}