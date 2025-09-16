
// FeedUpdatedEvent.kt
package com.zzan.zzan.feed.command.event

data class FeedUpdatedEvent(
    val feedId: String,
    val updatedFields: Map<String, Any?>
)
