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
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.example.mywiselaundrylife.R
import com.example.mywiselaundrylife.adapter.LaundryRoomAdapter
import com.example.mywiselaundrylife.data.user.UserInfo
import com.example.mywiselaundrylife.data.base.Laundry
import com.example.mywiselaundrylife.data.laundry.ListData
import com.example.mywiselaundrylife.databinding.ActivityMainBinding
import com.example.mywiselaundrylife.frag.FragInRoom
import com.example.mywiselaundrylife.serve.CustomSnapHelper
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

        Log.d("login", "${UserInfo.useLaundry}")

        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        UserInfo.useLaundry?.let {
            startTimer(it)
        }
        UserInfo.useDry?.let{
            startTimer(it)
        }

        binding.remainWashersTxt.text = remainWasherSum()
        UserInfo.currentRoom = ListData.roomLst[0].roomid

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.roomFrame.layoutManager = layoutManager
        binding.roomFrame.setHasFixedSize(true)
        binding.roomFrame.adapter = LaundryRoomAdapter(ListData.roomLst){ getRoom ->
            UserInfo.currentRoom = getRoom.roomid
            supportFragmentManager.beginTransaction()
                .replace(R.id.laundryFrame, FragInRoom())
                .commit()
        }

        val pagerSnapHelper = CustomSnapHelper()
        pagerSnapHelper.attachToRecyclerView(binding.roomFrame)

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        binding.logOutBtn.setOnClickListener {
            editor.remove("isLogin")
            editor.remove("myId")
            editor.apply()

            // 내가 사용하고 있는 세탁기 종료하기
            UserInfo.useLaundry?.let{
                timerStop(it)
                UserInfo.useLaundry = null
            }
            UserInfo.useDry?.let{
                timerStop(it)
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

    @RequiresApi(VERSION_CODES.O)
    private fun remainWasherSum() : String {
        // 0 : 세탁기, 1 : 건조기
        val remainWasherLst = arrayOfNulls<String>(2) // String? 배열 초기화
        val remainSumLst = IntArray(2) // Int 배열 초기화

        // 데이터 전처리
        ListData.laundryLst.filter { it.available }.forEach { wash ->
            when (wash.washerType) {
                "WASHER" -> {
                    if (remainWasherLst[0] == null) {
                        remainWasherLst[0] = "${wash.roomId} 세탁기${wash.washerId}"
                    }else{
                        remainSumLst[0]++
                    }
                }
                "DRYER" -> {
                    if(remainWasherLst[1]== null) {
                        remainWasherLst[1] = "${wash.roomId} 건조기${wash.washerId}"
                    }else{
                        remainSumLst[1]++
                    }
                }
            }
        }

        val (washer, dryer) = remainWasherLst
        val (washerSum, dryerSum) = remainSumLst

        return washerTxt(washer, dryer, washerSum, dryerSum)
    }

    private fun washerTxt(washer : String?, dryer : String?, washerSum : Int, dryerSum : Int) : String{

        Log.d("washerSum", "$washer $dryer $washerSum $dryerSum")

        return buildString {
            when{
                washer != null && dryer != null -> append("$washer, $dryer")
                washer != null -> append(washer)
                dryer != null -> append(dryer)
                else -> return "사용가능한 세탁 & 건조기가 없어요"
            }

            when{
                washerSum != 0 && dryerSum != 0 -> append(" 이외에도 세탁기 ${washerSum}대 건조기 ${dryerSum}대를 사용가능해요")
                washerSum != 0 -> append(" 이외에도 세탁기 ${washerSum}대를 사용가능해요")
                dryerSum != 0 -> append(" 이외에도 건조기 ${dryerSum}대를 사용가능해요")
                else -> append("을 사용가능해요")
            }
        }
    }

    override fun onItemClicked(item : Laundry) { // startTimer
        startTimer(item)  // TextView 업데이트
        Toast.makeText(this, "${item.washerType} 선택됨", Toast.LENGTH_SHORT).show()
    }

    fun startTimer(selectLaundry : Laundry){
        Log.d("startTimer", "start : ${selectLaundry}")
        when(selectLaundry.washerType){
            "WASHER" -> startLaundry(selectLaundry)
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
                    if(selectLaundry.endTime == null) {
                        Log.d("startTimer", "stop : ${selectLaundry}")
                        binding.laundryTime.text = "-- : --"
                        timerStop(selectLaundry)
                        return
                    }

                    val duration = Duration.between(now, LocalDateTime.parse(selectLaundry.endTime))

                    // 시간이 종료된 경우
                    if (duration.isNegative || duration.isZero) {
                        onTimerEnd(selectLaundry, selectLaundry.washerType)
                    } else {
                        Log.d("startTimer", "update : ${selectLaundry}")
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
                    if(selectLaundry.endTime == null) {
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
        when{
            backPressedTime + 2000 > System.currentTimeMillis() && supportFragmentManager.backStackEntryCount <= 0 ->{
                // 내가 사용하고 있는 세탁기 종료하기
                UserInfo.useLaundry?.let{
                    timerStop(it)
                    UserInfo.useLaundry = null
                }
                UserInfo.useDry?.let{
                    timerStop(it)
                    UserInfo.useDry = null
                }

                UserInfo.userId = null

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

    @RequiresApi(VERSION_CODES.O)
    fun updateView(){
        listOf(UserInfo.useLaundry, UserInfo.useDry).forEach { washer ->
            washer?.let{
                startTimer(it)
            }
        }
        binding.remainWashersTxt.text = remainWasherSum()
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