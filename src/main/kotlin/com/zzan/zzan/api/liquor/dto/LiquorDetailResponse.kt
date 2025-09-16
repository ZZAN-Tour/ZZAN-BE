package com.zzan.zzan.api.liquor.dto

data class LiquorDetailResponse(
    val id: String,
    val name: String,
    val type: String,
    val score: Double?,
    val description: String?,
    val foodPairing: String?,
    val volume: String?,
    val content: String?,
    val awards: String?,
    val etc: String?,
    val imageUrl: String?,
    val brewery: String?,
    val isScrap: Boolean = false,
)

