package com.example.mywiselaundrylife.data.base

data class LoginResponse(
    val token : String,
    val tokenType : String
)

data class RoomsResponse(
    val roooid : Int
)