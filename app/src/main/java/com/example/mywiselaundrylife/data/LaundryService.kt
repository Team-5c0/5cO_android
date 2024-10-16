package com.example.mywiselaundrylife.data

import com.example.mywiselaundrylife.data.base.RoomsResponse
import retrofit2.Response
import retrofit2.http.GET

interface LaundryService {
    @GET("/wash/rooms")
    suspend fun getRooms() : Response<RoomsResponse>
}