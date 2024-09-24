package com.example.mywiselaundrylife.act

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mywiselaundrylife.data.LaundryData
import com.example.mywiselaundrylife.databinding.ActivityMainBinding
import com.example.mywiselaundrylife.databinding.ItemLaundryBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging

class FCMActivity : AppCompatActivity() {

    private var backPressedTime: Long = 0
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityMainBinding

    private val lnd1 = LaundryData("세탁기1", null)
    private val lnd2 = LaundryData("세탁기2", null)
    private val lnd3 = LaundryData("세탁기3", null)
    private val lnd4 = LaundryData("세탁기4", null)
    private val DEFAULT_TIME : Long = 5400

    companion object {
        private val PERMISSION_REQUEST_CODE = 5000
        private val TAG = "FCMActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 임시저장한거 불러오기
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        permissionCheck()
        setToken()

        binding.laundry1.useBtn.setOnClickListener { startTime(lnd1, binding.laundry1.laundTime) }
        binding.laundry2.useBtn.setOnClickListener { startTime(lnd2, binding.laundry2.laundTime) }
        binding.laundry3.useBtn.setOnClickListener { startTime(lnd3, binding.laundry3.laundTime) }
        binding.laundry4.useBtn.setOnClickListener { startTime(lnd4, binding.laundry4.laundTime) }

        binding.logOutBtn.setOnClickListener {
            editor.remove("isLogin")
            editor.apply()
            val intent = Intent(this, StartActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun startTime(laundry: LaundryData, timeText: TextView) {
        if (laundry.time == null) {
            laundry.time = DEFAULT_TIME // 상수로 설정한 시간 사용
            laundry.userId = sharedPreferences.getString("myId", "누구세요")
            updateTimeText(laundry.time!!, timeText)
        } else {
            Log.d("myTag", "${laundry.name} 사용중") // 세탁기 이름 추가
        }
    }

    private fun updateTimeText(time: Long, timeText: TextView) {
        val hours = time / 3600
        val minutes = (time % 3600) / 60
        timeText.text = "${hours}시간 ${minutes}분"
    }

    override fun onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed() // 앱 종료
            return
        } else {
            // 첫 번째 뒤로가기 클릭 시 메시지 표시
            Toast.makeText(this, "뒤로 버튼을 한 번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show()
        }
        // 마지막으로 뒤로가기 버튼을 누른 시간 기록
        backPressedTime = System.currentTimeMillis()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode){
            PERMISSION_REQUEST_CODE -> {
                if(grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(applicationContext, "권한이 허용되지 않음", Toast.LENGTH_SHORT)
                        .show()
                } else{
                    Toast.makeText(applicationContext, "권한이 허용됨", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun setToken(){
        // token 가져오기
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // FCM 등록 토큰 가져오기
            val token = task.result

            val msg = "FCM Registration token: " + token;
            Log.d(TAG, msg)
            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
        })
    }

    private fun permissionCheck(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            val permissionCheck = ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    PERMISSION_REQUEST_CODE
                )
            }
        }
    }
}