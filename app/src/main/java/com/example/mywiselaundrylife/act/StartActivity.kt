package com.example.mywiselaundrylife.act

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mywiselaundrylife.data.UserInfo
import com.example.mywiselaundrylife.data.auth.AuthRequestManager
import com.example.mywiselaundrylife.data.auth.LoginRequest
import com.example.mywiselaundrylife.databinding.ActivityStartBinding
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class StartActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityStartBinding
    private val inputRegex: Pattern = Pattern.compile("[1-3][1-4][0-1][0-9]")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)

        if (sharedPreferences.getBoolean("isLogin", false)){
            UserInfo.userId = sharedPreferences.getInt("myId", 1000)
            startMainActivity()
        }

        binding.toInBtn.setOnClickListener{
            when{
                binding.inputId.text.isNullOrBlank()->{
                    Log.d("myTag", "빈칸 입력해줘")
                    showError("빈칸을 입력해주세요")
                }
//                !inputRegex.matcher(binding.inputId.text.toString()).matches()->{
//                    Log.d("myTag", "입력형식이 맞지 않음")
//                    showError("입력형식이 맞지않습니다")
//                }
                else->{
                    loginRequest()
                }
            }
        }
    }

    private fun loginRequest(){
        lifecycleScope.launch {
            try{
                // sharedPreference 편집
                val editor = sharedPreferences.edit()

                val response = AuthRequestManager.loginRequest(binding.inputId.text.toString().toInt())

                UserInfo.token = response.body()?.token
                UserInfo.userId = binding.inputId.text.toString().toInt()

                editor.putInt("myId", binding.inputId.text.toString().toInt())
                editor.putString("myToken", response.body()?.token)
                editor.putBoolean("isLogin", true)
                editor.apply()

                binding.errorText.visibility = View.GONE
                startMainActivity()

            } catch (e : retrofit2.HttpException){
                Log.e("mine", "${e.message}")
                showError("잘못된 번호입니다.")
            } catch (e : Exception){
                Log.e("mine", "${e.message}")
                showError("대부분 버그이무니다")
            }
        }
    }

    private fun showError(msg : String){
        binding.errorText.text =msg
        binding.errorText.visibility = View.VISIBLE
    }

    private fun startMainActivity(){
        val intent = Intent(this, FCMActivity::class.java)
        startActivity(intent)
        finish()
    }

}