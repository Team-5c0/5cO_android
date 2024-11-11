package com.example.mywiselaundrylife.act

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.example.mywiselaundrylife.R
import com.example.mywiselaundrylife.adapter.LaundryRoomAdapter
import com.example.mywiselaundrylife.data.user.UserInfo
import com.example.mywiselaundrylife.data.base.Laundry
import com.example.mywiselaundrylife.data.laundry.ListData
import com.example.mywiselaundrylife.databinding.ActivityMainBinding
import com.example.mywiselaundrylife.frag.FragInRoom
import com.example.mywiselaundrylife.serve.OnItemClickListener
import java.time.Duration
import java.time.LocalDateTime

class FCMActivity : AppCompatActivity(), OnItemClickListener {

    private var backPressedTime: Long = 0
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityMainBinding

    private val handler = Handler(Looper.getMainLooper())

    var runnableLaundry : Runnable? = null
    var runnableDryer : Runnable? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (UserInfo.useLaundry != null) {
            startTimer(UserInfo.useLaundry!!)
            Log.d("userId", "${UserInfo.useLaundry}")
        }
        if (UserInfo.useDry != null){
            startTimer(UserInfo.useDry!!)
        }

        UserInfo.currentRoom = ListData.roomLst[0].roomid

        binding.roomFrame.layoutManager = GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false)
        binding.roomFrame.setHasFixedSize(true)
        binding.roomFrame.adapter = LaundryRoomAdapter(ListData.roomLst){ getRoom ->
            UserInfo.currentRoom = getRoom.roomid
            supportFragmentManager.beginTransaction()
                .replace(R.id.laundryFrame, FragInRoom())
                .commit()
        }

        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.roomFrame)

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        binding.logOutBtn.setOnClickListener {
            editor.remove("isLogin")
            editor.apply()

            // 내가 사용하고 있는 세탁기 종료하기
            if(UserInfo.useLaundry != null){
                timerStop(UserInfo.useLaundry!!)
                UserInfo.useLaundry = null
            }
            if(UserInfo.useDry != null){
                timerStop(UserInfo.useDry!!)
                UserInfo.useDry = null
            }

            UserInfo.userId = null

            startActivity(Intent(this, StartActivity::class.java))
            finish()
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.laundryFrame, FragInRoom())
                .commit()
        }
    }

    // 인터페이스 구현: 아이템 클릭 시 TextView 업데이트
    override fun onItemClicked(item : Laundry) { // startTimer
        startTimer(item)  // TextView 업데이트
        Toast.makeText(this, "${item.washerType} 선택됨", Toast.LENGTH_SHORT).show()
    }

    fun startTimer(selectLaundry : Laundry){
        when(selectLaundry.washerType){
            "WASHER" -> {startLaundry(selectLaundry)}
            "DRYER" -> startDryer(selectLaundry)
        }
    }

    fun startLaundry(selectLaundry: Laundry){
        timerStop(selectLaundry)
        @RequiresApi(VERSION_CODES.O)
        runnableLaundry = object : Runnable{
            override fun run() {
                try {
                    val now = LocalDateTime.now()

                    // endTime이 null인지 확인
                    if (selectLaundry.endTime == null) {
                        binding.laundryTime.text = "-- : --"
                        timerStop(selectLaundry)
                        return
                    }

                    val duration = Duration.between(now, LocalDateTime.parse(selectLaundry.endTime))

                    // 시간이 종료된 경우
                    if (duration.isNegative || duration.isZero) {
                        onTimerEnd(selectLaundry, selectLaundry.washerType)
                    } else {
                        updateTimer(selectLaundry, duration)  // 타이머 업데이트
                        handler.postDelayed(this, 1000)  // 1초 후에 다시 실행
                    }
                } catch (e: Exception) {
                    Log.e("mine", "Error in timer: ${e.message}")
                }
            }
        }
        handler.post(runnableLaundry!!)
    }

    fun startDryer(selectLaundry: Laundry){
        timerStop(selectLaundry)
        runnableDryer = object : Runnable{
            @RequiresApi(VERSION_CODES.O)
            override fun run() {
                try {
                    val now = LocalDateTime.now().withNano(0)

                    // endTime이 null인지 확인
                    if (selectLaundry.endTime == null) {
                        binding.dryerTime.text = "-- : --"
                        timerStop(selectLaundry)
                        return
                    }

                    val duration = Duration.between(now, LocalDateTime.parse(selectLaundry.endTime))

                    // 시간이 종료된 경우
                    if (duration.isNegative || duration.isZero) {
                        onTimerEnd(selectLaundry, selectLaundry.washerType)
                    } else {
                        updateTimer(selectLaundry, duration)  // 타이머 업데이트
                        handler.postDelayed(this, 1000)  // 1초 후에 다시 실행
                    }
                } catch (e: Exception) {
                    Log.e("mine", "Error in timer: ${e.message}")
                }
            }
        }
        handler.post(runnableDryer!!)
    }

    fun timerStop(selectLaundry: Laundry){
        when(selectLaundry.washerType){
            "WASHER" -> {
                runnableLaundry?.let{
                    handler.removeCallbacks(it)
                    runnableLaundry = null
                }
            }
            "DRYER" -> {
                runnableDryer?.let{
                    handler.removeCallbacks(it)
                    runnableDryer = null
                }
            }
        }
    }

    @RequiresApi(VERSION_CODES.O)
    private fun updateTimer(selectLaundry: Laundry, duration: Duration) {
        val hours = duration.toHours()
        val minutes = (duration.toMinutes() % 60)

        when (selectLaundry.washerType) {
            "WASHER" -> binding.laundryTime.text = String.format("%02d : %02d", hours, minutes)
            "DRYER" -> binding.dryerTime.text = String.format("%02d : %02d", hours, minutes)
        }
    }

    private fun onTimerEnd(selectLaundry: Laundry, laundryType: String) {
        timerStop(selectLaundry)  // 타이머 정지

        when(laundryType) {
            "WASHER" ->{
                UserInfo.useLaundry = null
                binding.laundryTime.setText("-- : --")
            }
            "DRYER" -> {
                UserInfo.useDry = null
                binding.dryerTime.setText("-- : --")
            }
        }

        selectLaundry.endTime = null
        selectLaundry.user = null
    }

    override fun onBackPressed() {
        Log.d("mine", "${supportFragmentManager.backStackEntryCount}")
        if (backPressedTime + 2000 > System.currentTimeMillis() && supportFragmentManager.backStackEntryCount <= 0) {

            // 내가 사용하고 있는 세탁기 종료하기
            if(UserInfo.useLaundry != null){
                timerStop(UserInfo.useLaundry!!)
                UserInfo.useLaundry = null
            }
            if(UserInfo.useDry != null){
                timerStop(UserInfo.useDry!!)
                UserInfo.useDry = null
            }

            UserInfo.userId = null

            super.onBackPressed()
            return
        } else if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            Toast.makeText(this, "뒤로 버튼을 한 번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show()
        }
        backPressedTime = System.currentTimeMillis()
    }

    fun updateView(){
        if (UserInfo.useLaundry != null) {
            startTimer(UserInfo.useLaundry!!)
        }
        if (UserInfo.useDry != null){
            startTimer(UserInfo.useDry!!)
        }
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
