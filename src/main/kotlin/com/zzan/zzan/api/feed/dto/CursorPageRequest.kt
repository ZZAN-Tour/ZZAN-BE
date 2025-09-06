package com.zzan.zzan.api.feed.dto

data class CursorPageRequest(
    val limit: Int = 20,
    val cursor: String? = null, // 마지막 항목의 ID 또는 timestamp
    val sortBy: String = "recent" // recent, score, popular
) {
    init {
        require(limit in 1..50) { "limit은 1~50 사이여야 합니다: $limit" }
        require(sortBy in listOf("recent", "score", "popular")) { "지원하지 않는 정렬 방식: $sortBy" }
    }
}

data class CursorPageResponse<T>(
    val content: List<T>,
    val hasNext: Boolean,
    val nextCursor: String?,
    val totalCount: Long? = null // 선택적, 성능 고려
)
