// src/main/kotlin/com/zzan/zzan/feed/command/service/FeedCommandServiceImpl.kt (업데이트)
package com.zzan.zzan.feed.command.service

import com.zzan.zzan.api.feed.dto.CreateFeedRequest
import com.zzan.zzan.api.feed.dto.UpdateFeedRequest
import com.zzan.zzan.common.exception.CustomException
import com.zzan.zzan.feed.command.domain.Feed
import com.zzan.zzan.feed.command.domain.FeedImage
import com.zzan.zzan.feed.command.event.FeedDeletedEvent
import com.zzan.zzan.feed.command.event.FeedUpdatedEvent
import com.zzan.zzan.feed.command.repository.FeedImageRepository
import com.zzan.zzan.feed.command.repository.FeedRepository
import com.zzan.zzan.liquortag.command.service.LiquorTagCommandService
import com.zzan.zzan.place.command.domain.Place
import com.zzan.zzan.place.command.repository.PlaceRepository
import com.zzan.zzan.user.command.infrastructure.UserRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class FeedCommandServiceImpl(
    private val feedRepository: FeedRepository,
    private val feedImageRepository: FeedImageRepository,
    private val userRepository: UserRepository,
    private val placeRepository: PlaceRepository,
    private val eventPublisher: ApplicationEventPublisher,
    private val liquorTagCommandService: LiquorTagCommandService
) : FeedCommandService {

    override fun createFeed(request: CreateFeedRequest): String {
        // 1. 유효성 검사
        validateFeedCreation(request)
        val placeRequest = request.placeInfo
        val buyPlaceRequest = request.buyPlaceInfo

        val place = placeRepository.findByKakaoPlaceId(placeRequest.kakaoPlaceId)
            ?: (placeRepository.save(
                Place.of(
                    name = placeRequest.name,
                    address = placeRequest.address,
                    phone = placeRequest.phone,
                    latitude = placeRequest.latitude,
                    longitude = placeRequest.longitude,
                    kakaoPlaceId = placeRequest.kakaoPlaceId
                )
            ))

        // count 값 증가시키기
        placeRepository.incrementFeedCountById(place.id)

        val buyPlaceId = buyPlaceRequest?.let {
            placeRepository.findByKakaoPlaceId(it.kakaoPlaceId) ?: (placeRepository.save(
                Place.of(
                    name = it.name,
                    address = it.address,
                    phone = it.phone,
                    latitude = it.latitude,
                    longitude = it.longitude,
                    kakaoPlaceId = it.kakaoPlaceId
                )
            ))
        }?.id

        // 2. Feed 엔티티 생성 및 저장
        val feed = Feed(
            userId = request.userId,
            imageUrl = request.imageUrl,
            score = request.score,
            text = request.text,
            placeId = place.id,
            buyPlaceId = buyPlaceId
        )
        val savedFeed = feedRepository.save(feed)

        // 3. FeedImage 엔티티들 생성 및 저장
        val feedImages = request.images.map { imageRequest ->
            FeedImage(
                feedId = savedFeed.id,
                imageUrl = imageRequest.imageUrl,
                orderNum = imageRequest.orderNum
            )
        }
        val savedImages = feedImageRepository.saveAll(feedImages)

        // 4. 🆕 태그 생성 (요구사항: 이미지 번호, 상대좌표, 전통주 id)
        if (request.tags.isNotEmpty()) {
            liquorTagCommandService.createTagsForFeed(savedFeed.id, savedImages, request.tags)
        }

        return savedFeed.id
    }

    override fun updateFeed(feedId: String, request: UpdateFeedRequest) {
        // 기존 로직 유지
        val existingFeed = feedRepository.findByIdAndDeletedAtIsNull(feedId)
            ?: throw CustomException(HttpStatus.NOT_FOUND, "해당 피드를 찾을 수 없습니다.")

        request.buyPlaceId?.let { buyPlaceId ->
            if (!placeRepository.existsById(buyPlaceId)) {
                throw CustomException(HttpStatus.BAD_REQUEST, "존재하지 않는 구매 장소입니다.")
            }
        }

        val updatedFeed = existingFeed.copy(
            score = request.score ?: existingFeed.score,
            text = request.text ?: existingFeed.text,
            buyPlaceId = request.buyPlaceId ?: existingFeed.buyPlaceId
        )

        feedRepository.save(updatedFeed)
        eventPublisher.publishEvent(
            FeedUpdatedEvent(
                feedId = feedId,
                updatedFields = mapOf(
                    "score" to request.score,
                    "text" to request.text,
                    "buyPlaceId" to request.buyPlaceId
                )
            )
        )
    }

    override fun deleteFeed(feedId: String) {
        val feed = feedRepository.findByIdAndDeletedAtIsNull(feedId)
            ?: throw CustomException(HttpStatus.NOT_FOUND, "해당 피드를 찾을 수 없습니다.")

        // 🆕 태그도 함께 삭제
        liquorTagCommandService.deleteTagsByFeedId(feedId)

        val deletedFeed = feed.copy(deletedAt = LocalDateTime.now())
        feedRepository.save(deletedFeed)

        feedImageRepository.deleteByFeedId(feedId)
        eventPublisher.publishEvent(FeedDeletedEvent(feedId = feedId))
    }

    private fun validateFeedCreation(request: CreateFeedRequest) {
        if (!userRepository.
            existsByIdAndDeletedAtIsNull(request.userId)) {
            throw CustomException(HttpStatus.BAD_REQUEST, "존재하지 않는 사용자입니다.")
        }

        val orderNums = request.images.map { it.orderNum }
        if (orderNums.size != orderNums.toSet().size) {
            throw CustomException(HttpStatus.BAD_REQUEST, "이미지 순서에 중복이 있습니다.")
        }

        request.score?.let { score ->
            if (score < 0.0 || score > 5.0) {
                throw CustomException(HttpStatus.BAD_REQUEST, "평점은 0.0에서 5.0 사이여야 합니다.")
            }
        }
    }
}
