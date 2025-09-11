package com.zzan.zzan.user.query.handler

import com.zzan.zzan.api.common.dto.CursorPageRequest
import com.zzan.zzan.api.common.dto.CursorPageResponse
import com.zzan.zzan.api.user.dto.FeedScrapResponse
import com.zzan.zzan.api.user.dto.LiquorScrapResponse
import com.zzan.zzan.api.user.dto.UserFeedResponse
import com.zzan.zzan.user.command.domain.User
import com.zzan.zzan.user.query.repository.UserQueryRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class UserQueryServiceImpl(
    private val userRepository: UserQueryRepository
) : UserQueryService {

    @Cacheable("userByKakaoId", key = "#kakaoId")
    override fun findUserByKakaoId(kakaoId: String): User? {
        return userRepository.findUserByKakaoId(kakaoId)
    }

    override fun getMyFeed(request: CursorPageRequest, userId: String): CursorPageResponse<UserFeedResponse> {
        val pageable = PageRequest.of(0, request.size + 1)
        val items = userRepository.findFeedByUserId(
            userId = userId,
            cursor = request.cursor,
            pageable = pageable
        )

        val hasNext = items.size > request.size

        return CursorPageResponse(
            items = if (hasNext) items.take(request.size) else items,
            nextCursor = if (hasNext) items.last().scrapId else null,
            hasNext = hasNext
        )
    }

    override fun getFeedScraps(request: CursorPageRequest, userId: String): CursorPageResponse<FeedScrapResponse> {
        val pageable = PageRequest.of(0, request.size + 1)
        val items = userRepository.findFeedScrapsByUserId(
            userId = userId,
            cursor = request.cursor,
            pageable = pageable
        )

        val hasNext = items.size > request.size

        return CursorPageResponse(
            items = if (hasNext) items.take(request.size) else items,
            nextCursor = if (hasNext) items.last().scrapId else null,
            hasNext = hasNext
        )
    }

    override fun getLiquorScraps(request: CursorPageRequest, userId: String): CursorPageResponse<LiquorScrapResponse> {
        val pageable = PageRequest.of(0, request.size + 1)
        val items = userRepository.findLiquorScrapsByUserId(
            userId = userId,
            cursor = request.cursor,
            pageable = pageable
        )

        val hasNext = items.size > request.size

        return CursorPageResponse(
            items = if (hasNext) items.take(request.size) else items,
            nextCursor = if (hasNext) items.last().scrapId else null,
            hasNext = hasNext
        )
    }
}
