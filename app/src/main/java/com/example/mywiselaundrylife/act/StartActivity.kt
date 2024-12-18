package com.example.mywiselaundrylife.act

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.mywiselaundrylife.R
import com.example.mywiselaundrylife.data.laundry.LaundryRequestMananger
import com.example.mywiselaundrylife.data.laundry.ListData
import com.example.mywiselaundrylife.data.user.UserInfo
import com.example.mywiselaundrylife.data.auth.AuthRequestManager
import com.example.mywiselaundrylife.databinding.ActivityStartBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class StartActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityStartBinding
    private val inputRegex: Pattern = Pattern.compile("[1-3][1-4][0-1][0-9]")

    companion object {
        private const val PERMISSION_REQUEST_CODE = 5000
        private const val TAG = "FCMActivity"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStartBinding.inflate(layoutInflater)

        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setFCMToken()
        permissionCheck()

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)

        binding.toInBtn.setOnClickListener{
            when{
                binding.inputId.text.isNullOrBlank()->{
                    Log.d("myTag", "빈칸 입력해줘")
                    showError("빈칸을 입력해주세요")
                }
                else->{
                    loginRequest()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loginRequest(){
        lifecycleScope.launch {
            try{
                // sharedPreference 편집
//                val editor = sharedPreferences.edit()

                val loginResponse = AuthRequestManager.loginRequest(binding.inputId.text.toString().toInt())

                UserInfo.token = loginResponse.body()?.token
                UserInfo.userId = binding.inputId.text.toString().toInt()
                Log.d("userId", "${UserInfo.userId}")

//                editor.putInt("myId", binding.inputId.text.toString().toInt())
//                editor.putString("myToken", loginResponse.body()?.token)
//                editor.putBoolean("isLogin", true)
//                editor.apply()

                roomRequest()

                binding.errorText.visibility = View.GONE

            } catch (e : retrofit2.HttpException){
                Log.e("mine", "${e.message}")
                showError("로그인에 실패하였습니다")
            } catch (e : Exception){
                Log.e("mine", "${e.message}")
                showError("대부분 버그이무니다")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun roomRequest(){
        lifecycleScope.launch {
            try {

                RefreshData.roomRequest()

                fcmTokenRequest(UserInfo.userId!!, UserInfo.FCMtoken!!)

            } catch (e : retrofit2.HttpException){
                Log.e("mine", "${e.message}")
                showError("방 못 받아옴.")
            } catch (e : Exception){
                Log.e("mine", "${e.message}")
                showError("대부분 버그이무니다")
            }
        }
    }

    private fun fcmTokenRequest(userId : Int, fcmToken : String){
        lifecycleScope.launch {
            try {
                val FCMResponse = AuthRequestManager.fcmTokenRequest(userId, fcmToken)

                startMainActivity()

            } catch (e : retrofit2.HttpException){
                Log.e("mine", "${e.message}")
                showError("ㅋㅋ뭔가 버그남")
            } catch (e : Exception){
                Log.e("mine","${e.message}")
                showError("대부분 버그이무니다")
            }
        }
    }

    private fun showError(msg : String){
        binding.errorText.text = msg
        binding.errorText.visibility = View.VISIBLE
    }

    private fun startMainActivity(){

        Log.d("userId", "${UserInfo.useLaundry}")

        val intent = Intent(this, FCMActivity::class.java)
        startActivity(intent)
        finish()
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
            UserInfo.FCMtoken = token
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