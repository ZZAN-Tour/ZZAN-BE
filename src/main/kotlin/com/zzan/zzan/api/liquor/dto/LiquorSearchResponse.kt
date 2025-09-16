// src/main/kotlin/com/zzan/zzan/api/liquor/dto/LiquorSearchResponse.kt (수정된 버전)
package com.zzan.zzan.api.liquor.dto

data class LiquorSearchResponse(
    val id: String,
    val name: String,
    val type: String?,
    val brewery: String?,
    val imageUrl: String?,
    val averageScore: Double?,     // 평균 평점 (1.0~5.0)
    val ratingCount: Int,           // 전체 평점 개수
)
