package com.example.mywiselaundrylife.data

import android.util.Log
import com.example.mywiselaundrylife.data.base.Laundry
import com.example.mywiselaundrylife.data.base.Room
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object LaundryRequestMananger {
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://43.201.132.136:8081/")
        .client(
            OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
        )
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val laundryService : LaundryService = retrofit.create(LaundryService::class.java)

    suspend fun roomsRequest(): ArrayList<Room>? {
        val response = laundryService.getRooms()
        Log.d("response", "$response")
        if (response.isSuccessful) {
            return response.body() // List<Rooms>를 반환
        } else {
            throw retrofit2.HttpException(response)
        }
    }

    suspend fun laundryRequest(roomId : Int): ArrayList<Laundry>? {
        val response = laundryService.getWashers(roomId)
        Log.d("response", "$response")
        if (response.isSuccessful) {
            return response.body() // List<Rooms>를 반환
        } else {
            throw retrofit2.HttpException(response)
        }
    }

}