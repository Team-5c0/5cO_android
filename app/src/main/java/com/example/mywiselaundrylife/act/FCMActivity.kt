package com.example.mywiselaundrylife.act

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mywiselaundrylife.R
import com.example.mywiselaundrylife.data.UserInfo
import com.example.mywiselaundrylife.data.base.Laundry
import com.example.mywiselaundrylife.databinding.ActivityMainBinding
import com.example.mywiselaundrylife.frag.FragMain
import com.example.mywiselaundrylife.serve.OnItemClickListener
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import java.time.Duration
import java.time.LocalDateTime

class FCMActivity : AppCompatActivity(), OnItemClickListener {

    private var backPressedTime: Long = 0
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityMainBinding
    private val handler = Handler(Looper.getMainLooper())
    var runnableLaundry : Runnable? = null
    var runnableDryer : Runnable? = null

    companion object {
        private const val PERMISSION_REQUEST_CODE = 5000
        private const val TAG = "FCMActivity"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (UserInfo.useLaundry != null) {
            startTimer(UserInfo.useLaundry!!)
        }
        if (UserInfo.useDry != null){
            startTimer(UserInfo.useDry!!)
        }

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        permissionCheck()
        setFCMToken()

        binding.logOutBtn.setOnClickListener {
            editor.remove("isLogin")
            editor.apply()
            startActivity(Intent(this, StartActivity::class.java))
            finish()
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.frame, FragMain())
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
                    if (selectLaundry.endTime == null) {
                        binding.laundryTime.text = "사용자 없음"
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
                        binding.dryerTime.text = "없음"
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
        val seconds = (duration.seconds % 60)

        when (selectLaundry.washerType) {
            "WASHER" -> binding.laundryTime.text = String.format("%02d시간 %02d분 %02d초 남음", hours, minutes, seconds)
            "DRYER" -> binding.dryerTime.text = String.format("%02d시간 %02d분 %02d초 남음", hours, minutes, seconds)
        }
    }

    private fun onTimerEnd(selectLaundry: Laundry, laundryType: String) {
        timerStop(selectLaundry)  // 타이머 정지

        when(laundryType) {
            "WASHER" ->{
                UserInfo.useLaundry = null
                binding.laundryTime.setText("없음")
            }
            "DRYER" -> {
                UserInfo.useDry = null
                binding.dryerTime.setText("없음")
            }
        }

        selectLaundry.endTime = null
        selectLaundry.user = null
    }

    override fun onBackPressed() {
        Log.d("mine", "${supportFragmentManager.backStackEntryCount}")
        if (backPressedTime + 2000 > System.currentTimeMillis() && supportFragmentManager.backStackEntryCount <= 0) {
            super.onBackPressed()
            return
        } else if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            Toast.makeText(this, "뒤로 버튼을 한 번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show()
        }
        backPressedTime = System.currentTimeMillis()
    }

    // FireBase
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(applicationContext, "권한이 허용되지 않음", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(applicationContext, "권한이 허용됨", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            val token = task.result
            val msg = "FCM Registration token: $token"
            Log.d("mine", msg)
            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
        })
    }

    private fun permissionCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), PERMISSION_REQUEST_CODE
                )
            }
        }
    }
}
