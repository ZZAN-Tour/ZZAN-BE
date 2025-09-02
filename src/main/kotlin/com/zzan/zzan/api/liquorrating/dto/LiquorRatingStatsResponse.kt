package com.zzan.zzan.api.liquorrating.dto

data class LiquorRatingStatsResponse(
    val liquorId: String,
    val liquorName: String,
    val averageScore: Double?,
    val totalRatings: Int,
    val scoreDistribution: Map<String, Int>, // "1": 5개, "2": 10개, ...
    val latestRatings: List<LiquorRatingResponse>
)
