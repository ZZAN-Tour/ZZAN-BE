package com.zzan.zzan.user.query.handler

import com.zzan.zzan.api.common.dto.CursorPageRequest
import com.zzan.zzan.api.common.dto.CursorPageResponse
import com.zzan.zzan.api.user.dto.FeedScrapResponse
import com.zzan.zzan.api.user.dto.LiquorScrapResponse
import com.zzan.zzan.api.user.dto.UserFeedResponse
import com.zzan.zzan.user.command.domain.User

interface UserQueryService {
    fun findUserByKakaoId(kakaoId: String): User?

    fun getMyFeed(request: CursorPageRequest, userId: String): CursorPageResponse<UserFeedResponse>

    fun getFeedScraps(request: CursorPageRequest, userId: String): CursorPageResponse<FeedScrapResponse>

    fun getLiquorScraps(request: CursorPageRequest, userId: String): CursorPageResponse<LiquorScrapResponse>
}
