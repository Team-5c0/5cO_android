package com.example.mywiselaundrylife.activity

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.mywiselaundrylife.R
import com.example.mywiselaundrylife.data.user.UserInfo
import com.example.mywiselaundrylife.data.auth.AuthRequestManager
import com.example.mywiselaundrylife.data.laundry.LaundryDataUpdater
import com.example.mywiselaundrylife.databinding.ActivityStartBinding
import com.example.mywiselaundrylife.viewmodel.StartActVM
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException

class StartActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityStartBinding
    private lateinit var editor: SharedPreferences.Editor
    private val viewModel: StartActVM by viewModels()

    companion object {
        private const val PERMISSION_REQUEST_CODE = 5000
        private const val TAG = "FCMActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        val splashScreen = installSplashScreen()

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        editor = sharedPreferences.edit()

        if (sharedPreferences.getBoolean("isLogin", false)) {
            loginRequest(userId = sharedPreferences.getInt("myId", 0)) { success ->
                if (success) {
                    Log.d("login", "${UserInfo.userId}")
                }
            }
        }

        splashScreen.setKeepOnScreenCondition { false }
        binding = ActivityStartBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setFCMToken()
        permissionCheck()

        lifecycleScope.launch {
            viewModel.errorTxt.collect { errorMsg ->
                binding.errorText.text = errorMsg
                binding.errorText.visibility =
                    if (errorMsg.isNotEmpty()) View.VISIBLE else View.GONE
            }
        }

        binding.inputId.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.inputId.hint = ""
            } else {
                binding.inputId.hint = "ex) 1101"
            }
        }

        binding.toInBtn.setOnClickListener {
            binding.toInBtn.isEnabled = false
            when {
                binding.inputId.text.isNullOrBlank() -> {
                    binding.toInBtn.isEnabled = true
                    Log.d("myTag", "빈칸 입력해줘")
                    viewModel.setErrorTxt("빈칸을 입력해주세요")
                }

                else -> {
                    val inputId = binding.inputId.text.toString().toInt()
                    loginRequest(inputId) { success ->
                        if (success) {
                            editor.putInt("myId", inputId)
                            editor.putBoolean("isLogin", true)
                            editor.apply()
                        }
                    }
                }
            }
        }
    }

    private fun loginRequest(userId: Int, callback: (Boolean) -> Unit) {
        lifecycleScope.launch {
            kotlin.runCatching {
                val loginResponse = AuthRequestManager.loginRequest(userId)

                UserInfo.token = loginResponse.body()?.token
                UserInfo.userId = userId
                Log.d("userId", "${UserInfo.userId}")

                roomRequest()
                binding.errorText.visibility = View.GONE
                callback(true)

            }.onFailure { e ->
                when (e) {
                    is retrofit2.HttpException -> {
                        binding.toInBtn.isEnabled = true
                        viewModel.setErrorTxt("로그인에 실패하였습니다")
                    }

                    is SocketTimeoutException -> {
                        binding.toInBtn.isEnabled = true
                        viewModel.setErrorTxt("요청 시간이 초과되었습니다")
                    }

                    else -> {
                        binding.toInBtn.isEnabled = true
                        viewModel.setErrorTxt("알 수 없는 오류가 발생하였습니다")
                    }
                }
                callback(false)
            }
        }
    }

    private suspend fun roomRequest() {
        LaundryDataUpdater.roomRequest()
        fcmTokenRequest(UserInfo.userId!!, UserInfo.FCMToken!!)
    }

    private suspend fun fcmTokenRequest(userId: Int, fcmToken: String) {
        AuthRequestManager.fcmTokenRequest(userId, fcmToken)
        binding.toInBtn.isEnabled = true
        startMainActivity()
    }

    private fun startMainActivity() {

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
                if (grantResults.isEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
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
            UserInfo.FCMToken = token
            val msg = "FCM Registration token: $token"
            Log.d("mine", msg)
        })
    }

    private fun permissionCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), PERMISSION_REQUEST_CODE
                )
            }
        }
    }
}