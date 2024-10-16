package com.example.mywiselaundrylife.act

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mywiselaundrylife.R
import com.example.mywiselaundrylife.databinding.ActivityMainBinding
import com.example.mywiselaundrylife.frag.FragMain
import com.example.mywiselaundrylife.serve.OnItemClickListener
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging

class FCMActivity : AppCompatActivity(), OnItemClickListener {

    private var backPressedTime: Long = 0
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityMainBinding

    companion object {
        private const val PERMISSION_REQUEST_CODE = 5000
        private const val TAG = "FCMActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        permissionCheck()
        setToken()

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
    override fun onItemClicked(itemName: String) {
        if("세탁기" in itemName){
            binding.laundryTime.text = "1시간 30분" // TextView 업데이트
        } else{
            binding.dryerTime.text = "1시간 30분" // TextView 업데이트
        }
        Toast.makeText(this, "$itemName 선택됨", Toast.LENGTH_SHORT).show()
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

    private fun setToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            val token = task.result
            val msg = "FCM Registration token: $token"
            Log.d(TAG, msg)
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
