package com.example.mywiselaundrylife.data.laundry

import com.example.mywiselaundrylife.data.response.Laundry
import com.example.mywiselaundrylife.data.response.Room
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface LaundryService {
    @GET("/wash/rooms/gender")
    suspend fun getRooms(
        @Header("Authorization") token: String
    ) : Response<ArrayList<Room>>

    @GET("/wash/washers")
    suspend fun getWashers(
        @Query("roomid") roomId : String
    ) : Response<ArrayList<Laundry>>
}