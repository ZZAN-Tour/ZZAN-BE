package com.zzan.zzan.feed.command.service

import com.zzan.zzan.api.feed.dto.*

interface FeedCommandService {
    fun createFeed(request: CreateFeedRequest): String // feedId만 반환
    fun updateFeed(feedId: String, request: UpdateFeedRequest)
    fun deleteFeed(feedId: String)
}
