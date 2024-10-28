package com.example.mywiselaundrylife.data

import com.example.mywiselaundrylife.data.base.Laundry
import com.example.mywiselaundrylife.data.base.Room
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface LaundryService {
    @GET("/wash/rooms")
    suspend fun getRooms() : Response<ArrayList<Room>>

    @GET("/wash/washers")
    suspend fun getWashers(
        @Query("roomid") roomId : String
    ) : Response<ArrayList<Laundry>>
}