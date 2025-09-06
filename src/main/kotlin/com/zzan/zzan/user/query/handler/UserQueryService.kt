package com.zzan.zzan.user.query.handler

import com.zzan.zzan.api.user.dto.FeedScrapPageResponse
import com.zzan.zzan.api.user.dto.GetScrapsRequest
import com.zzan.zzan.api.user.dto.LiquorScrapPageResponse
import com.zzan.zzan.user.command.domain.User

interface UserQueryService {
    fun findUserByKakaoId(kakaoId: String): User?

    fun getFeedScraps(request: GetScrapsRequest, userId: String): FeedScrapPageResponse

    fun getLiquorScraps(request: GetScrapsRequest, userId: String): LiquorScrapPageResponse
}
