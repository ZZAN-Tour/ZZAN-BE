package com.zzan.zzan.api.user.dto

data class FeedScrapResponse(
    val scrapId: String,
    val feedId: String,
    val feedImageUrl: String,
    val placeId: String,
    val placeName: String,
    val placeAddress: String,
)
