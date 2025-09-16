package com.zzan.zzan.feed.query.handler

import com.zzan.zzan.api.common.dto.CommonPageRequest
import com.zzan.zzan.api.common.dto.CommonPageResponse
import com.zzan.zzan.api.feed.dto.*
import com.zzan.zzan.common.exception.CustomException
import com.zzan.zzan.feed.command.domain.Feed
import com.zzan.zzan.feed.query.repository.FeedQueryRepository
import com.zzan.zzan.liquortag.command.repository.LiquorTagRepository
import com.zzan.zzan.liquortag.query.service.LiquorTagQueryService
import com.zzan.zzan.place.command.repository.PlaceRepository
import com.zzan.zzan.user.command.infrastructure.UserRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.time.LocalDateTime


@Service
class FeedQueryServiceImpl(
    private val feedQueryRepository: FeedQueryRepository,
    private val userRepository: UserRepository,
    private val placeRepository: PlaceRepository,
    private val liquorTagQueryService: LiquorTagQueryService,
    private val liquorTagRepository: LiquorTagRepository
) : FeedQueryService {

    @Cacheable("feed", key = "#feedId")
    override fun getFeedById(feedId: String): FeedDetailResponse {
        val feed = feedQueryRepository.getFeedDetailByIdWithoutImages(feedId)
            ?: throw CustomException(HttpStatus.NOT_FOUND, "해당 피드를 찾을 수 없습니다.")

        // User 정보 조회
        val user = userRepository.findByIdAndDeletedAtIsNull(feed.userId)
            ?: throw CustomException(HttpStatus.NOT_FOUND, "사용자 정보를 찾을 수 없습니다.")

        // Place 정보 조회
        val place = placeRepository.findById(feed.placeId).orElse(null)
            ?: throw CustomException(HttpStatus.NOT_FOUND, "장소 정보를 찾을 수 없습니다.")

        // Buy Place 정보 조회 (선택적)
        val buyPlace = feed.buyPlaceId?.let {
            placeRepository.findById(it).orElse(null)
        }

        // 이미지 정보 조회
        val images = feedQueryRepository.getFeedImages(feedId).map { image ->
            FeedImageInfo(
                id = image.id,
                imageUrl = image.imageUrl,
                orderNum = image.orderNum
            )
        }

        // 🆕 태그 정보 조회
        val tags = liquorTagQueryService.getTagsByFeedId(feedId).map { tag ->
            TagInfo(
                id = tag.id,
                imageId = tag.imageId,
                liquorId = tag.liquorId,
                liquorName = tag.liquorName,
                liquorType = tag.liquorType,
                liquorBrewery = tag.liquorBrewery,
                tagX = tag.tagX,
                tagY = tag.tagY
            )
        }

        return FeedDetailResponse(
            id = feed.id,
            userId = user.id,
            userNickname = user.nickname ?: "Unknown",
            userProfileImageUrl = user.profileImageUrl,
            imageUrl = feed.imageUrl,
            score = feed.score,
            text = feed.text,
            place = PlaceInfo(
                id = place.id,
                name = place.name,
                address = place.address,
                latitude = place.latitude,
                longitude = place.longitude
            ),
            buyPlace = buyPlace?.let { bp ->
                PlaceInfo(
                    id = bp.id,
                    name = bp.name,
                    address = bp.address,
                    latitude = bp.latitude,
                    longitude = bp.longitude
                )
            },
            images = images,
            tags = tags, // 🆕 태그 정보 포함
            createdAt = feed.createdAt ?: LocalDateTime.now()
        )
    }

    override fun getPlaceFeeds(placeId: String, request: CommonPageRequest): CommonPageResponse<FeedSummaryResponse> {
        val pageable = PageRequest.of(0, request.size + 1)
        val items = feedQueryRepository.findFeedByPlaceId(
            placeId = placeId,
            cursor = request.cursor,
            pageable = pageable
        )

        val hasNext = items.size > request.size

        return CommonPageResponse(
            items = if (hasNext) items.take(request.size) else items,
            nextCursor = if (hasNext) items.last().id else null,
            hasNext = hasNext
        )
    }


    override fun getFeeds(
        criteria: FeedSearchCriteria,
        pageRequest: com.zzan.zzan.api.feed.dto.PageRequest
    ): PageResponse<FeedSummaryResponse> {
        val sort = Sort.by(
            if (pageRequest.sortDirection.uppercase() == "DESC") Sort.Direction.DESC else Sort.Direction.ASC,
            pageRequest.sortBy
        )

        val pageable = PageRequest.of(pageRequest.page, pageRequest.size, sort)
        val feedPage = feedQueryRepository.getFeeds(criteria, pageable)

        val feedSummaries = feedPage.content.map { feed ->
            val user = userRepository.findByIdAndDeletedAtIsNull(feed.userId)
            val place = placeRepository.findById(feed.placeId).orElse(null)

            FeedSummaryResponse(
                id = feed.id,
                userId = feed.userId,
                userNickname = user?.nickname ?: "Unknown",
                userProfileImageUrl = user?.profileImageUrl,
                imageUrl = feed.imageUrl,
                score = feed.score,
                text = feed.text,
                placeName = place?.name ?: "Unknown Place",
                createdAt = feed.createdAt ?: LocalDateTime.now()
            )
        }

        return PageResponse(
            content = feedSummaries,
            page = feedPage.number,
            size = feedPage.size,
            totalElements = feedPage.totalElements,
            totalPages = feedPage.totalPages,
            hasNext = feedPage.hasNext(),
            hasPrevious = feedPage.hasPrevious()
        )
    }

    override fun existsFeed(feedId: String): Boolean {
        return feedQueryRepository.existsByIdAndDeletedAtIsNull(feedId)
    }

    override fun getFeedCountByUser(userId: String): Long {
        return feedQueryRepository.countByUserIdAndDeletedAtIsNull(userId)
    }

    override fun getFeedCountByPlace(placeId: String): Long {
        return feedQueryRepository.countByPlaceIdAndDeletedAtIsNull(placeId)
    }


    override fun searchFeeds(
        query: String,
        pageRequest: com.zzan.zzan.api.feed.dto.PageRequest
    ): PageResponse<FeedSummaryResponse> {
        val sort = Sort.by(
            if (pageRequest.sortDirection.uppercase() == "DESC") Sort.Direction.DESC else Sort.Direction.ASC,
            pageRequest.sortBy
        )

        val pageable = PageRequest.of(pageRequest.page, pageRequest.size, sort)
        val feedPage = feedQueryRepository.searchFeedsByText(query, pageable)

        val feedSummaries = feedPage.content.map { feed ->
            val user = userRepository.findByIdAndDeletedAtIsNull(feed.userId)
            val place = placeRepository.findById(feed.placeId).orElse(null)

            FeedSummaryResponse(
                id = feed.id,
                userId = feed.userId,
                userNickname = user?.nickname ?: "Unknown",
                userProfileImageUrl = user?.profileImageUrl,
                imageUrl = feed.imageUrl,
                score = feed.score,
                text = feed.text,
                placeName = place?.name ?: "Unknown Place",
                createdAt = feed.createdAt ?: LocalDateTime.now()
            )
        }

        return PageResponse(
            content = feedSummaries,
            page = feedPage.number,
            size = feedPage.size,
            totalElements = feedPage.totalElements,
            totalPages = feedPage.totalPages,
            hasNext = feedPage.hasNext(),
            hasPrevious = feedPage.hasPrevious()
        )
    }

    override fun getFeedsByLiquorTagWithCursor(
        liquorId: String,
        pageRequest: CursorPageRequest
    ): CursorPageResponse<FeedSummaryResponse> {

        // 1. 태그된 피드 ID 목록 조회 (커서 기반)
        val limit = pageRequest.limit + 1 // hasNext 판단용 +1
        val taggedFeedIds = liquorTagRepository.findDistinctFeedIdsByLiquorIdWithCursorRecent(
            liquorId = liquorId,
            cursor = pageRequest.cursor,
            limit = limit
        )

        if (taggedFeedIds.isEmpty()) {
            return CursorPageResponse(
                content = emptyList(),
                hasNext = false,
                nextCursor = null,
                totalCount = 0L
            )
        }

        // 2. hasNext 판단 및 실제 데이터 추출
        val hasNext = taggedFeedIds.size > pageRequest.limit
        val actualFeedIds = if (hasNext) {
            taggedFeedIds.dropLast(1)
        } else {
            taggedFeedIds
        }

        // 3. 피드 상세 정보 조회
        val feeds = when (pageRequest.sortBy) {
            "recent" -> {
                feedQueryRepository.findByIdInAndDeletedAtIsNullOrderByCreatedAtDescIdDesc(actualFeedIds)
            }

            "score" -> {
                feedQueryRepository.findByIdInAndDeletedAtIsNullAndScoreIsNotNullOrderByScoreDescIdDesc(actualFeedIds)
            }

            else -> {
                feedQueryRepository.findByIdInAndDeletedAtIsNullOrderByCreatedAtDescIdDesc(actualFeedIds)
            }
        }

        // 4. FeedSummaryResponse로 변환
        val feedSummaries = feeds.map { feed -> convertToFeedSummary(feed) }

        // 5. 다음 커서 생성
        val nextCursor = if (hasNext && feedSummaries.isNotEmpty()) {
            when (pageRequest.sortBy) {
                "recent" -> feedSummaries.last().createdAt.toString() + ":" + feedSummaries.last().id
                "score" -> (feedSummaries.last().score ?: 0.0).toString() + ":" + feedSummaries.last().id
                else -> feedSummaries.last().id
            }
        } else null


        return CursorPageResponse(
            content = feedSummaries,
            hasNext = hasNext,
            nextCursor = nextCursor,
            totalCount = null // 성능을 위해 총 개수는 별도 API로 제공
        )

    }

    private fun convertToFeedSummary(feed: Feed): FeedSummaryResponse {
        val user = userRepository.findByIdAndDeletedAtIsNull(feed.userId)
        val place = placeRepository.findById(feed.placeId).orElse(null)

        return FeedSummaryResponse(
            id = feed.id,
            userId = feed.userId,
            userNickname = user?.nickname ?: "Unknown",
            userProfileImageUrl = user?.profileImageUrl,
            imageUrl = feed.imageUrl,
            score = feed.score,
            text = feed.text,
            placeName = place?.name ?: "Unknown Place",
            createdAt = feed.createdAt ?: LocalDateTime.now()
        )
    }

}


