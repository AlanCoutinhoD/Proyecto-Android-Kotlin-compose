package com.example.myapplication.data

data class LoginResponse(
    val token: String,
    val userId: String
    // Otros campos que el servidor pueda devolver
) 