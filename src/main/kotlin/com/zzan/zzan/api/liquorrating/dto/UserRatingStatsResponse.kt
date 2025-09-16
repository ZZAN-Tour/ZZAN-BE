package com.zzan.zzan.api.liquorrating.dto

/**
 * 사용자 평점 통계 응답 DTO
 *
 * 사용 사례: 마이 페이지에서 "내가 작성한 평점 통계" 표시
 */
data class UserRatingStatsResponse(
    val userId: String,
    val userNickname: String,
    val totalRatings: Int,
    val averageScore: Double,
    val scoreDistribution: Map<String, Int>, // "1": 2개, "2": 5개, ...
    val favoriteTypes: List<String>, // 자주 평가한 전통주 타입들 (탁주, 약주 등)
    val ratingsByMonth: Map<String, Int> // "2025-01": 5개, "2025-02": 3개, ...
)
