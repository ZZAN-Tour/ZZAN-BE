package com.zzan.zzan.api.user.dto

data class LiquorScrapPageResponse(
    val scraps: List<LiquorScrapResponse>,
    val nextCursor: String?,
    val hasNext: Boolean
)
