package com.example.mywiselaundrylife.data.auth

import com.example.mywiselaundrylife.data.base.FCMResponse
import com.example.mywiselaundrylife.data.base.LoginResponse
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthService {
    @POST("/login")
    suspend fun login(
        @Query("userId") userId : Int
    ) : Response<LoginResponse>

    @POST("/register-fcm-token")
    suspend fun fcmToken(
        @Query("userId") userId: Int,
        @Query("fcmToken") fcmToken : String
    ) : Response<FCMResponse>

}