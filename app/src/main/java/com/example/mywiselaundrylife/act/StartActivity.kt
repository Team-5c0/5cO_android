package com.example.mywiselaundrylife.act

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.mywiselaundrylife.databinding.ActivityStartBinding
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

        // sharedPreference 편집
        val editor = sharedPreferences.edit()

        if (sharedPreferences.getBoolean("isLogin", false)){
            startMainActivity()
        }

        binding.toInBtn.setOnClickListener{
            when{
                binding.inputId.text.isNullOrBlank()->
                    Log.d("myTag", "빈칸 입력해줘")
//                    showError("빈칸을 입력해주세요")

                !inputRegex.matcher(binding.inputId.text.toString()).matches()->
                    Log.d("myTag", "입력형식이 맞지 않음")
//                    showError("입력형식이 맞지않습니다")


                else->{

                    editor.putString("myId", binding.inputId.text.toString())
                    editor.putBoolean("isLogin", true)
                    editor.apply()

                    binding.errorText.visibility = View.GONE
                    startMainActivity()
                }
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