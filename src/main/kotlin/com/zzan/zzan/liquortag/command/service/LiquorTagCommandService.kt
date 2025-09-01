package com.zzan.zzan.liquortag.command.service

import com.zzan.zzan.api.liquortag.dto.CreateLiquorTagRequest
import com.zzan.zzan.api.liquortag.dto.TagInFeedRequest
import com.zzan.zzan.feed.command.domain.FeedImage

interface LiquorTagCommandService {
    fun createTag(request: CreateLiquorTagRequest): String
    fun deleteTag(tagId: String)
    fun deleteTagsByFeedId(feedId: String)
    fun deleteTagsByImageId(imageId: String)
    fun createTagsForFeed(feedId: String, images: List<FeedImage>, tagRequests: List<TagInFeedRequest>)
}
