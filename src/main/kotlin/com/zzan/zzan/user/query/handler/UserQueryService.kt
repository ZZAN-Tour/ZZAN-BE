package com.zzan.zzan.user.query.handler

import com.zzan.zzan.api.common.dto.CommonPageRequest
import com.zzan.zzan.api.common.dto.CommonPageResponse
import com.zzan.zzan.api.user.dto.FeedScrapResponse
import com.zzan.zzan.api.user.dto.LiquorScrapResponse
import com.zzan.zzan.api.user.dto.UserFeedResponse
import com.zzan.zzan.user.command.domain.User

interface UserQueryService {
    fun findUserByKakaoId(kakaoId: String): User?

    fun getMyFeed(request: CommonPageRequest, userId: String): CommonPageResponse<UserFeedResponse>

    fun getFeedScraps(request: CommonPageRequest, userId: String): CommonPageResponse<FeedScrapResponse>

    fun getLiquorScraps(request: CommonPageRequest, userId: String): CommonPageResponse<LiquorScrapResponse>

    fun isFeedScrap(userId: String, feedId: String): Boolean
}
