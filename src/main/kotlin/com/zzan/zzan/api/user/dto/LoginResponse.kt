package com.zzan.zzan.api.user.dto

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
)
