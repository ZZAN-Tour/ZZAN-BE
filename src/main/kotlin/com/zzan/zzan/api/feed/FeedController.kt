package com.zzan.zzan.api.feed

import com.zzan.zzan.api.feed.dto.*
import com.zzan.zzan.common.response.ApiResponse
import com.zzan.zzan.feed.command.service.FeedCommandService
import com.zzan.zzan.feed.query.FeedQueryService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/feeds")
class FeedController(
    private val feedCommandService: FeedCommandService,
    private val feedQueryService: FeedQueryService
) {

    /**
     * 피드 생성
     * Command: 피드 ID만 반환하여 CQS 원칙 준수
     */
    @PostMapping
    fun createFeed(
        @Valid @RequestBody request: CreateFeedRequest
    ): ApiResponse<Map<String, String>> {
        val feedId = feedCommandService.createFeed(request)
        return ApiResponse.ok(
            mapOf(
                "feedId" to feedId,
                "message" to "피드가 성공적으로 생성되었습니다."
            )
        )
    }

    /**
     * 피드 상세 조회
     * Query: 캐싱된 상세 정보 반환
     */
    @GetMapping("/{feedId}")
    fun getFeedById(@PathVariable feedId: String): ApiResponse<FeedDetailResponse> {
        val feed = feedQueryService.getFeedById(feedId)
        return ApiResponse.ok(feed)
    }

    /**
     * 피드 목록 조회 (페이징, 필터링)
     * Query: 검색 조건에 따른 페이징된 피드 목록 반환
     */
    @GetMapping
    fun getFeeds(
        @RequestParam(required = false) userId: String?,
        @RequestParam(required = false) placeId: String?,
        @RequestParam(required = false) minScore: Double?,
        @RequestParam(required = false) maxScore: Double?,
        @RequestParam(required = false) fromDate: String?,
        @RequestParam(required = false) toDate: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "DESC") sortDirection: String
    ): ApiResponse<PageResponse<FeedSummaryResponse>> {

        val criteria = FeedSearchCriteria(
            userId = userId,
            placeId = placeId,
            minScore = minScore,
            maxScore = maxScore,
            fromDate = fromDate?.let { LocalDateTime.parse(it) },
            toDate = toDate?.let { LocalDateTime.parse(it) }
        )

        val pageRequest = PageRequest(
            page = page,
            size = size,
            sortBy = sortBy,
            sortDirection = sortDirection
        )

        val feeds = feedQueryService.getFeeds(criteria, pageRequest)
        return ApiResponse.ok(feeds)
    }

    /**
     * 내 피드 목록 조회
     * Query: 특정 사용자의 피드 목록 반환
     */
    @GetMapping("/my/{userId}")
    fun getMyFeeds(
        @PathVariable userId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "DESC") sortDirection: String
    ): ApiResponse<PageResponse<FeedSummaryResponse>> {

        val criteria = FeedSearchCriteria(userId = userId)
        val pageRequest = PageRequest(
            page = page,
            size = size,
            sortBy = sortBy,
            sortDirection = sortDirection
        )

        val feeds = feedQueryService.getFeeds(criteria, pageRequest)
        return ApiResponse.ok(feeds)
    }

    /**
     * 특정 장소의 피드 목록 조회
     * Query: 장소별 피드 목록 반환
     */
    @GetMapping("/place/{placeId}")
    fun getFeedsByPlace(
        @PathVariable placeId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "DESC") sortDirection: String
    ): ApiResponse<PageResponse<FeedSummaryResponse>> {

        val criteria = FeedSearchCriteria(placeId = placeId)
        val pageRequest = PageRequest(
            page = page,
            size = size,
            sortBy = sortBy,
            sortDirection = sortDirection
        )

        val feeds = feedQueryService.getFeeds(criteria, pageRequest)
        return ApiResponse.ok(feeds)
    }

    /**
     * 평점별 피드 검색
     * Query: 평점 범위에 따른 피드 목록 반환
     */
    @GetMapping("/score")
    fun getFeedsByScore(
        @RequestParam(required = false) minScore: Double?,
        @RequestParam(required = false) maxScore: Double?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "score") sortBy: String,
        @RequestParam(defaultValue = "DESC") sortDirection: String
    ): ApiResponse<PageResponse<FeedSummaryResponse>> {

        val criteria = FeedSearchCriteria(
            minScore = minScore,
            maxScore = maxScore
        )
        val pageRequest = PageRequest(
            page = page,
            size = size,
            sortBy = sortBy,
            sortDirection = sortDirection
        )

        val feeds = feedQueryService.getFeeds(criteria, pageRequest)
        return ApiResponse.ok(feeds)
    }

    /**
     * 기간별 피드 검색
     * Query: 날짜 범위에 따른 피드 목록 반환
     */
    @GetMapping("/date-range")
    fun getFeedsByDateRange(
        @RequestParam fromDate: String,
        @RequestParam toDate: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "DESC") sortDirection: String
    ): ApiResponse<PageResponse<FeedSummaryResponse>> {

        val criteria = FeedSearchCriteria(
            fromDate = LocalDateTime.parse(fromDate),
            toDate = LocalDateTime.parse(toDate)
        )
        val pageRequest = PageRequest(
            page = page,
            size = size,
            sortBy = sortBy,
            sortDirection = sortDirection
        )

        val feeds = feedQueryService.getFeeds(criteria, pageRequest)
        return ApiResponse.ok(feeds)
    }

    /**
     * 피드 수정
     * Command: 수정 작업만 수행하고 성공 메시지 반환
     */
    @PutMapping("/{feedId}")
    fun updateFeed(
        @PathVariable feedId: String,
        @Valid @RequestBody request: UpdateFeedRequest
    ): ApiResponse<Map<String, String>> {
        feedCommandService.updateFeed(feedId, request)
        return ApiResponse.ok(
            mapOf(
                "feedId" to feedId,
                "message" to "피드가 성공적으로 수정되었습니다."
            )
        )
    }

    /**
     * 피드 삭제 (소프트 삭제)
     * Command: 삭제 작업만 수행하고 성공 메시지 반환
     */
    @DeleteMapping("/{feedId}")
    fun deleteFeed(@PathVariable feedId: String): ApiResponse<Map<String, String>> {
        feedCommandService.deleteFeed(feedId)
        return ApiResponse.ok(
            mapOf(
                "feedId" to feedId,
                "message" to "피드가 성공적으로 삭제되었습니다."
            )
        )
    }

    /**
     * 피드 존재 여부 확인
     * Query: 빠른 존재 여부 체크
     */
    @GetMapping("/{feedId}/exists")
    fun checkFeedExists(@PathVariable feedId: String): ApiResponse<Map<String, Boolean>> {
        val exists = feedQueryService.existsFeed(feedId)
        return ApiResponse.ok(mapOf("exists" to exists))
    }

    /**
     * 사용자의 피드 개수 조회
     * Query: 통계성 정보 반환
     */
    @GetMapping("/count/user/{userId}")
    fun getUserFeedCount(@PathVariable userId: String): ApiResponse<Map<String, Long>> {
        val count = feedQueryService.getFeedCountByUser(userId)
        return ApiResponse.ok(mapOf("count" to count))
    }

    /**
     * 장소의 피드 개수 조회
     * Query: 통계성 정보 반환
     */
    @GetMapping("/count/place/{placeId}")
    fun getPlaceFeedCount(@PathVariable placeId: String): ApiResponse<Map<String, Long>> {
        val count = feedQueryService.getFeedCountByPlace(placeId)
        return ApiResponse.ok(mapOf("count" to count))
    }

    /**
     * 최신 피드 조회 (홈 화면용)
     * Query: 최신순으로 정렬된 피드 목록
     */
    @GetMapping("/recent")
    fun getRecentFeeds(
        @RequestParam(defaultValue = "10") limit: Int
    ): ApiResponse<List<FeedSummaryResponse>> {

        val pageRequest = PageRequest(
            page = 0,
            size = limit,
            sortBy = "createdAt",
            sortDirection = "DESC"
        )

        val criteria = FeedSearchCriteria()
        val feeds = feedQueryService.getFeeds(criteria, pageRequest)
        return ApiResponse.ok(feeds.content)
    }

    /**
     * 인기 피드 조회 (평점 기준)
     * Query: 평점순으로 정렬된 피드 목록
     */
    @GetMapping("/popular")
    fun getPopularFeeds(
        @RequestParam(defaultValue = "10") limit: Int,
        @RequestParam(defaultValue = "4.0") minScore: Double
    ): ApiResponse<List<FeedSummaryResponse>> {

        val pageRequest = PageRequest(
            page = 0,
            size = limit,
            sortBy = "score",
            sortDirection = "DESC"
        )

        val criteria = FeedSearchCriteria(minScore = minScore)
        val feeds = feedQueryService.getFeeds(criteria, pageRequest)
        return ApiResponse.ok(feeds.content)
    }

    /**
     * 피드 검색 (텍스트 기반)
     * Query: 피드 텍스트 내용 검색
     */
    @GetMapping("/search")
    fun searchFeeds(
        @RequestParam query: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ApiResponse<PageResponse<FeedSummaryResponse>> {

        val pageRequest = PageRequest(
            page = page,
            size = size,
            sortBy = "createdAt",
            sortDirection = "DESC"
        )

        val feeds = feedQueryService.searchFeeds(query, pageRequest)
        return ApiResponse.ok(feeds)
    }
}
