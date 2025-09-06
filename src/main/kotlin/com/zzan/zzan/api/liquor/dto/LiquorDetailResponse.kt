package com.zzan.zzan.api.liquor.dto
import com.zzan.zzan.api.liquorrating.dto.LiquorRatingStatsResponse

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
    val brewery: String?
)

/**
 * 전통주 상세 페이지 기본 정보 응답 DTO
 * 변경 빈도가 낮은 정적 데이터만 포함
 */
data class LiquorDetailPageResponse(
    // 기본 전통주 정보
    val liquorInfo: LiquorDetailResponse,

    // 평점 통계 정보 (배치로 업데이트되므로 상대적으로 정적)
    val ratingStats: LiquorRatingStatsResponse,

    // 메타 정보
    val meta: LiquorDetailPageMeta
)

data class LiquorDetailPageMeta(
    val totalFeedsCount: Long,        // 전체 피드 개수
    val totalRatingsCount: Long,      // 전체 평점 개수
    val averageScore: Double?,        // 평균 점수
    val isRecommendable: Boolean,     // 추천 가능 여부
    val popularityRank: Int?,         // 인기 순위
    val recommendationScore: Int      // 추천 점수 (0-100)
)

// 커서 기반 페이징을 위한 공통 DTO
data class CursorPageRequest(
    val limit: Int = 20,
    val cursor: String? = null // 마지막 항목의 ID 또는 timestamp
)

data class CursorPageResponse<T>(
    val content: List<T>,
    val hasNext: Boolean,
    val nextCursor: String?
)

