package com.zzan.zzan.user.query.handler

import com.zzan.zzan.api.user.dto.FeedScrapPageResponse
import com.zzan.zzan.api.user.dto.GetScrapsRequest
import com.zzan.zzan.api.user.dto.LiquorScrapPageResponse
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

    override fun getFeedScraps(request: GetScrapsRequest, userId: String): FeedScrapPageResponse {
        val pageable = PageRequest.of(0, request.size + 1)
        val items = userRepository.findFeedScrapsByUserIdWithCursor(
            userId = userId,
            cursor = request.cursor,
            pageable = pageable
        )

        val hasNext = items.size > request.size

        return FeedScrapPageResponse(
            scraps = if (hasNext) items.take(request.size) else items,
            nextCursor = if (hasNext) items.last().scrapId else null,
            hasNext = hasNext
        )
    }

    override fun getLiquorScraps(request: GetScrapsRequest, userId: String): LiquorScrapPageResponse {
        val pageable = PageRequest.of(0, request.size + 1)
        val items = userRepository.findLiquorScrapsByUserIdWithCursor(
            userId = userId,
            cursor = request.cursor,
            pageable = pageable
        )

        val hasNext = items.size > request.size

        return LiquorScrapPageResponse(
            scraps = if (hasNext) items.take(request.size) else items,
            nextCursor = if (hasNext) items.last().scrapId else null,
            hasNext = hasNext
        )
    }
}
