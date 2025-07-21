package com.example.mywiselaundrylife.data.auth

import android.util.Log
import com.example.mywiselaundrylife.data.response.FCMResponse
import com.example.mywiselaundrylife.data.response.LoginResponse
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object AuthRequestManager {

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://43.201.18.90:8081/")
        .client(
            OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
        )
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val authService : AuthService = retrofit.create(AuthService::class.java)

    suspend fun loginRequest(userId : Int) : Response<LoginResponse>{
        val response = authService.login(userId)
        Log.d("response", "$response")
        if (!response.isSuccessful){
            throw retrofit2.HttpException(response)
        }
        return response
    }

    suspend fun fcmTokenRequest(userId: Int, fcmToken : String) : Response<FCMResponse>{
        val response = authService.fcmToken(userId, fcmToken)
        Log.d("response", "$response")
        if (!response.isSuccessful){
            throw retrofit2.HttpException(response)
        }
        return response
    }

}