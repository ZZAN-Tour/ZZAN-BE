package com.zzan.zzan.api.user.dto

data class LiquorScrapResponse(
    val scrapId: String,
    val liquorId: String,
    val liquorName: String,
    val liquorScore: Double?,
    val liquorImageUrl: String,
    val liquorType: String?,
)
