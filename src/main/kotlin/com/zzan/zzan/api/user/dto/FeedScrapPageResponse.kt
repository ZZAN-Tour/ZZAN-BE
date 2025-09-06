package com.zzan.zzan.api.user.dto

data class FeedScrapPageResponse(
    val scraps: List<FeedScrapResponse>,
    val nextCursor: String?,
    val hasNext: Boolean
)


