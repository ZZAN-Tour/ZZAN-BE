package com.zzan.zzan.api.liquorrating.dto

import java.time.LocalDateTime

data class LiquorRatingResponse(
    val id: String,
    val userId: String,
    val userNickname: String,
    val liquorId: String,
    val liquorName: String,
    val score: Double,
    val comment: String?,
    val placeName: String,
    val createdAt: LocalDateTime
)
