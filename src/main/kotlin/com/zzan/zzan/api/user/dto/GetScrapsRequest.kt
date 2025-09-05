package com.zzan.zzan.api.user.dto

data class GetScrapsRequest(
    val size: Int = 10,
    val cursor: String? = null
)
