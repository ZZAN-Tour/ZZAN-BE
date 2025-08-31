package com.zzan.zzan.liquortag.query.service
import com.zzan.zzan.api.liquortag.dto.LiquorTagResponse

interface LiquorTagQueryService {
    fun getTagsByFeedId(feedId: String): List<LiquorTagResponse>
    fun getTagsByImageId(imageId: String): List<LiquorTagResponse>
    fun getTagById(tagId: String): LiquorTagResponse?
}
