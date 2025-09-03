package com.zzan.zzan.liquorrating.command.service

import com.zzan.zzan.api.liquorrating.dto.CreateLiquorRatingRequest
import com.zzan.zzan.api.liquorrating.dto.UpdateLiquorRatingRequest
import com.zzan.zzan.common.exception.CustomException
import com.zzan.zzan.feed.command.domain.Feed
import com.zzan.zzan.feed.command.repository.FeedRepository
import com.zzan.zzan.liquor.command.repository.LiquorRepository
import com.zzan.zzan.liquorrating.command.domain.LiquorRating
import com.zzan.zzan.liquorrating.command.event.LiquorRatingEventPublisher
import com.zzan.zzan.liquorrating.command.repository.LiquorRatingRepository
import com.zzan.zzan.liquortag.command.repository.LiquorTagRepository
import jakarta.transaction.Transactional
import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Service
@Transactional
class LiquorRatingCommandServiceImpl(
    private val liquorRatingRepository: LiquorRatingRepository,
    private val feedRepository: FeedRepository,
    private val liquorTagRepository: LiquorTagRepository,
    private val liquorRepository: LiquorRepository,
    private val eventPublisher: LiquorRatingEventPublisher
) : LiquorRatingCommandService {

    companion object : KLogging()

    override fun createRating(userId: String, request: CreateLiquorRatingRequest): String {
        // 검증 로직
        val feed = validateRatingEligibility(userId, request.liquorId, request.sourceFeedId)

        // 평점 점수 검증
        validateScore(request.score)

        // 엔티티 생성
        val rating = LiquorRating(
            userId = userId,
            liquorId = request.liquorId,
            sourceFeedId = request.sourceFeedId,
            placeId = feed.placeId,
            score = request.score,
            comment = request.comment
        )

        val savedRating = liquorRatingRepository.save(rating)
        eventPublisher.markLiquorForUpdate(request.liquorId)

        logger.info("Created liquor rating: ${savedRating.id}")
        return savedRating.id
    }

    override fun updateRating(userId: String, ratingId: String, request: UpdateLiquorRatingRequest) {
        val rating = liquorRatingRepository.findById(ratingId).orElseThrow {
            CustomException(HttpStatus.NOT_FOUND, "평점을 찾을 수 없습니다.")
        }

        // 권한 및 기간 검증
        validateRatingUpdate(rating, userId)
        validateScore(request.score)

        // 직접 필드 업데이트
        rating.score = request.score
        rating.comment = request.comment
        rating.updatedAt = LocalDateTime.now()

        eventPublisher.markLiquorForUpdate(rating.liquorId)
        logger.info("Updated liquor rating: $ratingId")
    }

    override fun deleteRating(userId: String, ratingId: String) {
        val rating = liquorRatingRepository.findById(ratingId).orElseThrow {
            CustomException(HttpStatus.NOT_FOUND, "평점을 찾을 수 없습니다.")
        }

        // 권한 검증
        if (rating.userId != userId) {
            throw CustomException(HttpStatus.FORBIDDEN, "본인이 작성한 평점만 삭제할 수 있습니다.")
        }

        liquorRatingRepository.delete(rating)
        eventPublisher.markLiquorForUpdate(rating.liquorId)

        logger.info("Deleted liquor rating: $ratingId")
    }

    override fun updateScoreOnly(userId: String, ratingId: String, newScore: Double) {
        logger.info("Updating rating score only - userId: $userId, ratingId: $ratingId, newScore: $newScore")

        val rating = liquorRatingRepository.findById(ratingId).orElseThrow {
            CustomException(HttpStatus.NOT_FOUND, "평점을 찾을 수 없습니다.")
        }

        validateRatingUpdate(rating, userId)
        validateScore(newScore)

        // 점수만 업데이트
        rating.score = newScore
        rating.updatedAt = LocalDateTime.now()

        eventPublisher.markLiquorForUpdate(rating.liquorId)
        logger.info("Updated rating score: $ratingId")
    }

    override fun updateCommentOnly(userId: String, ratingId: String, newComment: String?) {
        logger.info("Updating rating comment only - userId: $userId, ratingId: $ratingId")

        val rating = liquorRatingRepository.findById(ratingId).orElseThrow {
            CustomException(HttpStatus.NOT_FOUND, "평점을 찾을 수 없습니다.")
        }

        validateRatingUpdate(rating, userId)

        // 코멘트만 업데이트
        rating.comment = newComment?.takeIf { it.isNotBlank() }
        rating.updatedAt = LocalDateTime.now()

        eventPublisher.markLiquorForUpdate(rating.liquorId)
        logger.info("Updated rating comment: $ratingId")
    }

    /**
     * 평점 수정 권한 및 기간 검증
     */
    private fun validateRatingUpdate(rating: LiquorRating, userId: String) {
        if (rating.userId != userId) {
            throw CustomException(HttpStatus.FORBIDDEN, "본인이 작성한 평점만 수정할 수 있습니다.")
        }

        val daysSinceCreated = ChronoUnit.DAYS.between(rating.createdAt, LocalDateTime.now())
        if (daysSinceCreated > 7) {
            throw CustomException(HttpStatus.BAD_REQUEST, "평점은 작성 후 7일 이내에만 수정할 수 있습니다.")
        }
    }

    /**
     * 평점 점수 검증
     */
    private fun validateScore(score: Double) {
        if (score < 1.0 || score > 5.0) {
            throw CustomException(HttpStatus.BAD_REQUEST, "평점은 1.0에서 5.0 사이여야 합니다: $score")
        }
    }

    /**
     * 피드 기반 인증 검증
     */
    private fun validateRatingEligibility(userId: String, liquorId: String, feedId: String): Feed {
        logger.debug("Validating rating eligibility - userId: $userId, liquorId: $liquorId, feedId: $feedId")

        // 1. 피드 존재 및 소유권 확인
        val feed = feedRepository.findByIdAndDeletedAtIsNull(feedId)
            ?: throw CustomException(HttpStatus.NOT_FOUND, "해당 피드를 찾을 수 없습니다.")

        if (feed.userId != userId) {
            logger.warn("Feed ownership mismatch - userId: $userId, feedUserId: ${feed.userId}, feedId: $feedId")
            throw CustomException(HttpStatus.FORBIDDEN, "본인이 작성한 피드의 전통주만 평점을 줄 수 있습니다.")
        }

        // 2. 피드에서 전통주 태그 확인
        val feedTags = liquorTagRepository.findByFeedIdOrderByImageId(feedId)
        val hasLiquorTag = feedTags.any { it.liquorId == liquorId }

        if (!hasLiquorTag) {
            logger.warn("Liquor not tagged in feed - liquorId: $liquorId, feedId: $feedId, availableTags: ${feedTags.map { it.liquorId }}")
            throw CustomException(HttpStatus.BAD_REQUEST, "해당 피드에서 태그하지 않은 전통주입니다.")
        }

        // 3. 전통주 존재 확인
        if (!liquorRepository.existsById(liquorId)) {
            logger.warn("Liquor not found - liquorId: $liquorId")
            throw CustomException(HttpStatus.NOT_FOUND, "존재하지 않는 전통주입니다.")
        }

        logger.debug("Rating eligibility validated successfully")
        return feed
    }
}
