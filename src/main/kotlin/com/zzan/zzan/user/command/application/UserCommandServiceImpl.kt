package com.zzan.zzan.user.command.application

import com.zzan.zzan.api.user.dto.ScrapResponse
import com.zzan.zzan.common.exception.CustomException
import com.zzan.zzan.user.command.domain.FeedScrap
import com.zzan.zzan.user.command.domain.LiquorScrap
import com.zzan.zzan.user.command.domain.User
import com.zzan.zzan.user.command.infrastructure.FeedScrapRepository
import com.zzan.zzan.user.command.infrastructure.LiquorScrapRepository
import com.zzan.zzan.user.command.infrastructure.UserRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserCommandServiceImpl(
    private val userRepository: UserRepository,
    private val feedScrapRepository: FeedScrapRepository,
    private val liquorScrapRepository: LiquorScrapRepository
) : UserCommandService {

    @CacheEvict(cacheNames = ["userByKakaoId"], key = "#user.kakaoId")
    override fun createUser(user: User): User {
        return userRepository.save(user)
    }

    override fun createFeedScrap(userId: String, feedId: String): ScrapResponse {
        if (feedScrapRepository.existsByUserIdAndFeedId(userId, feedId)) {
            throw CustomException(HttpStatus.CONFLICT, "이미 스크랩된 피드입니다.")
        }

        return ScrapResponse(
            feedScrapRepository.save(FeedScrap.of(userId, feedId)).id
        )
    }

    override fun createLiquorScrap(userId: String, liquorId: String): ScrapResponse {
        if (liquorScrapRepository.existsByUserIdAndLiquorId(userId, liquorId)) {
            throw CustomException(HttpStatus.CONFLICT, "이미 스크랩된 전통주입니다.")
        }

        return ScrapResponse(
            liquorScrapRepository.save(LiquorScrap.of(userId, liquorId)).id
        )
    }

    override fun deleteFeedScrap(userId: String, feedId: String): ScrapResponse {
        val feedScrap = feedScrapRepository.findByUserIdAndFeedId(userId, feedId)
            ?: throw CustomException(HttpStatus.NOT_FOUND, "스크랩이 존재하지 않습니다.")

        feedScrapRepository.delete(feedScrap)
        return ScrapResponse(feedScrap.id)
    }

    override fun deleteLiquorScrap(userId: String, liquorId: String): ScrapResponse {
        val liquorScrap = liquorScrapRepository.findByUserIdAndLiquorId(userId, liquorId)
            ?: throw CustomException(HttpStatus.NOT_FOUND, "스크랩이 존재하지 않습니다.")

        liquorScrapRepository.delete(liquorScrap)
        return ScrapResponse(liquorScrap.id)
    }

}
