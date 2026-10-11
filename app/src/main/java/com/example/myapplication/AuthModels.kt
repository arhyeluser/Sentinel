package com.example.myapplication

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val access_token: String,
    val refresh_token: String,
    val user: UserData
)

data class UserData(
    val id: Int,
    val name: String,
    val email: String
)