// FeedCreatedEvent.kt
package com.zzan.zzan.feed.command.event

data class FeedCreatedEvent(
    val feedId: String,
    val userId: String,
    val placeId: String,
    val buyPlaceId: String?,
    val imageIds: List<String>
)
