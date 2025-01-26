package com.example.myapplication.api

import com.example.myapplication.data.LoginRequest
import com.example.myapplication.data.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginApi {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
} 