package com.zzan.zzan.api.liquorrating

import com.zzan.zzan.api.liquorrating.dto.*
import com.zzan.zzan.common.response.ApiResponse
import com.zzan.zzan.liquorrating.command.service.LiquorRatingCommandService
import com.zzan.zzan.liquorrating.query.service.LiquorRatingQueryService
import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

/**
 * 전통주 평점 API Controller
 *
 * 주요 기능:
 * 1. CRUD 작업 (생성, 조회, 수정, 삭제)
 * 2. 다양한 조건의 평점 조회 (사용자별, 전통주별, 장소별 등)
 * 3. 평점 통계 조회 (전통주별, 사용자별)
 * 4. 부분 업데이트 (점수만, 코멘트만)
 * 5. 검색 및 필터링 기능
 * 6. 관리자 및 분석 기능
 */
@RestController
@RequestMapping("/api/liquor-ratings")
class LiquorRatingController(
    private val liquorRatingCommandService: LiquorRatingCommandService,
    private val liquorRatingQueryService: LiquorRatingQueryService
) {

    companion object : KLogging()

    // =============================================
    // Command 작업들 (CUD - Create, Update, Delete)
    // =============================================

    /**
     * 전통주 평점 생성
     *
     * POST /api/liquor-ratings
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createRating(
        @RequestHeader("X-User-Id") userId: String, // JWT에서 추출한 사용자 ID
        @Valid @RequestBody request: CreateLiquorRatingRequest
    ): ApiResponse<Map<String, String>> {
        logger.info("Creating liquor rating - userId: $userId, liquorId: ${request.liquorId}")

        val ratingId = liquorRatingCommandService.createRating(userId, request)

        return ApiResponse.ok(
            mapOf(
                "ratingId" to ratingId,
                "message" to "평점이 성공적으로 등록되었습니다."
            )
        )
    }

    /**
     * 전통주 평점 수정 (전체)
     *
     * PUT /api/liquor-ratings/{ratingId}
     */
    @PutMapping("/{ratingId}")
    fun updateRating(
        @RequestHeader("X-User-Id") userId: String,
        @PathVariable ratingId: String,
        @Valid @RequestBody request: UpdateLiquorRatingRequest
    ): ApiResponse<Map<String, String>> {
        logger.info("Updating liquor rating - userId: $userId, ratingId: $ratingId")

        liquorRatingCommandService.updateRating(userId, ratingId, request)

        return ApiResponse.ok(
            mapOf(
                "ratingId" to ratingId,
                "message" to "평점이 성공적으로 수정되었습니다."
            )
        )
    }

    /**
     * 점수만 빠르게 업데이트
     *
     * PATCH /api/liquor-ratings/{ratingId}/score
     */
    @PatchMapping("/{ratingId}/score")
    fun updateScoreOnly(
        @RequestHeader("X-User-Id") userId: String,
        @PathVariable ratingId: String,
        @Valid @RequestBody request: UpdateScoreOnlyRequest
    ): ApiResponse<Map<String, String>> {
        logger.info("Updating rating score only - userId: $userId, ratingId: $ratingId, score: ${request.score}")

        liquorRatingCommandService.updateScoreOnly(userId, ratingId, request.score)

        return ApiResponse.ok(
            mapOf(
                "ratingId" to ratingId,
                "message" to "점수가 성공적으로 업데이트되었습니다."
            )
        )
    }

    /**
     * 코멘트만 업데이트
     *
     * PATCH /api/liquor-ratings/{ratingId}/comment
     */
    @PatchMapping("/{ratingId}/comment")
    fun updateCommentOnly(
        @RequestHeader("X-User-Id") userId: String,
        @PathVariable ratingId: String,
        @Valid @RequestBody request: UpdateCommentOnlyRequest
    ): ApiResponse<Map<String, String>> {
        logger.info("Updating rating comment only - userId: $userId, ratingId: $ratingId")

        liquorRatingCommandService.updateCommentOnly(userId, ratingId, request.comment)

        return ApiResponse.ok(
            mapOf(
                "ratingId" to ratingId,
                "message" to "코멘트가 성공적으로 업데이트되었습니다."
            )
        )
    }

    /**
     * 전통주 평점 삭제
     *
     * DELETE /api/liquor-ratings/{ratingId}
     */
    @DeleteMapping("/{ratingId}")
    fun deleteRating(
        @RequestHeader("X-User-Id") userId: String,
        @PathVariable ratingId: String
    ): ApiResponse<Map<String, String>> {
        logger.info("Deleting liquor rating - userId: $userId, ratingId: $ratingId")

        liquorRatingCommandService.deleteRating(userId, ratingId)

        return ApiResponse.ok(
            mapOf(
                "ratingId" to ratingId,
                "message" to "평점이 성공적으로 삭제되었습니다."
            )
        )
    }

    // =============================================
    // Query 작업들 (Read - 조회)
    // =============================================

    /**
     * 특정 평점 상세 조회
     *
     * GET /api/liquor-ratings/{ratingId}
     */
    @GetMapping("/{ratingId}")
    fun getRatingById(@PathVariable ratingId: String): ApiResponse<LiquorRatingResponse?> {
        logger.debug("Querying rating by id: $ratingId")

        val rating = liquorRatingQueryService.getRatingById(ratingId)

        return ApiResponse.ok(rating)
    }

    /**
     * 전통주별 평점 목록 조회
     *
     * GET /api/liquor-ratings/liquor/{liquorId}
     */
    @GetMapping("/liquor/{liquorId}")
    fun getRatingsByLiquor(@PathVariable liquorId: String): ApiResponse<List<LiquorRatingResponse>> {
        logger.debug("Querying ratings for liquor: $liquorId")

        val ratings = liquorRatingQueryService.getRatingsByLiquor(liquorId)

        return ApiResponse.ok(ratings)
    }

    /**
     * 전통주 평점 통계 조회
     *
     * GET /api/liquor-ratings/liquor/{liquorId}/stats
     */
    @GetMapping("/liquor/{liquorId}/stats")
    fun getLiquorRatingStats(@PathVariable liquorId: String): ApiResponse<LiquorRatingStatsResponse> {
        logger.debug("Querying rating stats for liquor: $liquorId")

        val stats = liquorRatingQueryService.getRatingStats(liquorId)

        return ApiResponse.ok(stats)
    }

    /**
     * 내 평점 목록 조회
     *
     * GET /api/liquor-ratings/my
     */
    @GetMapping("/my")
    fun getMyRatings(
        @RequestHeader("X-User-Id") userId: String
    ): ApiResponse<List<LiquorRatingResponse>> {
        logger.debug("Querying ratings for user: $userId")

        val ratings = liquorRatingQueryService.getRatingsByUser(userId)

        return ApiResponse.ok(ratings)
    }

    /**
     * 수정 가능한 내 평점 목록 조회 (7일 이내 작성)
     *
     * GET /api/liquor-ratings/my/editable
     */
    @GetMapping("/my/editable")
    fun getMyEditableRatings(
        @RequestHeader("X-User-Id") userId: String
    ): ApiResponse<List<LiquorRatingResponse>> {
        logger.debug("Querying editable ratings for user: $userId")

        val ratings = liquorRatingQueryService.getEditableRatingsByUser(userId)

        return ApiResponse.ok(ratings)
    }

    /**
     * 내 평점 통계 조회
     *
     * GET /api/liquor-ratings/my/stats
     */
    @GetMapping("/my/stats")
    fun getMyRatingStats(
        @RequestHeader("X-User-Id") userId: String
    ): ApiResponse<UserRatingStatsResponse> {
        logger.debug("Querying rating stats for user: $userId")

        val stats = liquorRatingQueryService.getUserRatingStats(userId)

        return ApiResponse.ok(stats)
    }

    /**
     * 특정 피드와 관련된 평점들 조회
     *
     * GET /api/liquor-ratings/feed/{feedId}
     */
    @GetMapping("/feed/{feedId}")
    fun getRatingsByFeed(@PathVariable feedId: String): ApiResponse<List<LiquorRatingResponse>> {
        logger.debug("Querying ratings for feed: $feedId")

        val ratings = liquorRatingQueryService.getRatingsBySourceFeed(feedId)

        return ApiResponse.ok(ratings)
    }

    /**
     * 특정 장소의 평점들 조회
     *
     * GET /api/liquor-ratings/place/{placeId}
     */
    @GetMapping("/place/{placeId}")
    fun getRatingsByPlace(@PathVariable placeId: String): ApiResponse<List<LiquorRatingResponse>> {
        logger.debug("Querying ratings for place: $placeId")

        val ratings = liquorRatingQueryService.getRatingsByPlace(placeId)

        return ApiResponse.ok(ratings)
    }

    /**
     * 높은 평점을 받은 전통주들 조회 (추천용)
     *
     * GET /api/liquor-ratings/top-rated?minRatingCount=3&limit=10
     */
    @GetMapping("/top-rated")
    fun getTopRatedLiquors(
        @RequestParam(defaultValue = "3") @Min(1) minRatingCount: Int,
        @RequestParam(defaultValue = "10") @Min(1) @Max(50) limit: Int
    ): ApiResponse<List<LiquorRatingStatsResponse>> {
        logger.debug("Querying top rated liquors - minRatingCount: $minRatingCount, limit: $limit")

        val topRatedLiquors = liquorRatingQueryService.getTopRatedLiquors(minRatingCount, limit)

        return ApiResponse.ok(topRatedLiquors)
    }

    // =============================================
    // 검색 및 필터링 API들
    // =============================================

    /**
     * 평점 검색 (코멘트 내용 기준)
     *
     * GET /api/liquor-ratings/search?query=맛있&minScore=4.0&maxResults=50
     */
    @GetMapping("/search")
    fun searchRatings(
        @RequestParam query: String,
        @RequestParam(required = false) @DecimalMin("1.0") @DecimalMax("5.0") minScore: Double?,
        @RequestParam(defaultValue = "50") @Min(1) @Max(100) maxResults: Int
    ): ApiResponse<List<LiquorRatingResponse>> {
        logger.debug("Searching ratings - query: $query, minScore: $minScore, maxResults: $maxResults")

        val results = liquorRatingQueryService.searchRatings(query, minScore, maxResults)

        return ApiResponse.ok(results)
    }

    /**
     * 전통주 타입별 평점 조회
     *
     * GET /api/liquor-ratings/type/{liquorType}?limit=20
     */
    @GetMapping("/type/{liquorType}")
    fun getRatingsByLiquorType(
        @PathVariable liquorType: String,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) limit: Int
    ): ApiResponse<List<LiquorRatingResponse>> {
        logger.debug("Querying ratings by liquor type: $liquorType, limit: $limit")

        val ratings = liquorRatingQueryService.getRatingsByLiquorType(liquorType, limit)

        return ApiResponse.ok(ratings)
    }

    /**
     * 점수 범위별 평점 조회
     *
     * GET /api/liquor-ratings/score-range?minScore=4.0&maxScore=5.0&limit=30
     */
    @GetMapping("/score-range")
    fun getRatingsByScoreRange(
        @RequestParam @DecimalMin("1.0") @DecimalMax("5.0") minScore: Double,
        @RequestParam @DecimalMin("1.0") @DecimalMax("5.0") maxScore: Double,
        @RequestParam(defaultValue = "50") @Min(1) @Max(100) limit: Int
    ): ApiResponse<List<LiquorRatingResponse>> {
        logger.debug("Querying ratings by score range: $minScore - $maxScore, limit: $limit")

        val ratings = liquorRatingQueryService.getRatingsByScoreRange(minScore, maxScore, limit)

        return ApiResponse.ok(ratings)
    }

    /**
     * 최근 N일 평점 조회
     *
     * GET /api/liquor-ratings/recent?days=7&limit=30
     */
    @GetMapping("/recent")
    fun getRecentRatings(
        @RequestParam(defaultValue = "7") @Min(1) @Max(30) days: Int,
        @RequestParam(defaultValue = "30") @Min(1) @Max(100) limit: Int
    ): ApiResponse<List<LiquorRatingResponse>> {
        logger.debug("Querying recent ratings - days: $days, limit: $limit")

        val ratings = liquorRatingQueryService.getRecentRatings(days, limit)

        return ApiResponse.ok(ratings)
    }

    /**
     * 코멘트가 있는 평점들 조회
     *
     * GET /api/liquor-ratings/with-comments?minLength=10&limit=30
     */
    @GetMapping("/with-comments")
    fun getRatingsWithComments(
        @RequestParam(defaultValue = "10") @Min(1) minLength: Int,
        @RequestParam(defaultValue = "30") @Min(1) @Max(100) limit: Int
    ): ApiResponse<List<LiquorRatingResponse>> {
        logger.debug("Querying ratings with comments - minLength: $minLength, limit: $limit")

        val ratings = liquorRatingQueryService.getRatingsWithComments(minLength, limit)

        return ApiResponse.ok(ratings)
    }

    // =============================================
    // 통계 및 분석 API들
    // =============================================

    /**
     * 월별 평점 통계 조회
     *
     * GET /api/liquor-ratings/stats/monthly?months=12
     */
    @GetMapping("/stats/monthly")
    fun getMonthlyRatingStats(
        @RequestParam(defaultValue = "12") @Min(1) @Max(24) months: Int
    ): ApiResponse<Map<String, Long>> {
        logger.debug("Querying monthly rating stats for $months months")

        val stats = liquorRatingQueryService.getMonthlyRatingStats(months)

        return ApiResponse.ok(stats)
    }

    /**
     * 전체 점수 분포 조회
     *
     * GET /api/liquor-ratings/stats/score-distribution
     */
    @GetMapping("/stats/score-distribution")
    fun getOverallScoreDistribution(): ApiResponse<Map<String, Long>> {
        logger.debug("Querying overall score distribution")

        val distribution = liquorRatingQueryService.getOverallScoreDistribution()

        return ApiResponse.ok(distribution)
    }

    /**
     * 활발한 사용자 TOP N 조회
     *
     * GET /api/liquor-ratings/stats/top-users?limit=10
     */
    @GetMapping("/stats/top-users")
    fun getTopActiveUsers(
        @RequestParam(defaultValue = "10") @Min(1) @Max(50) limit: Int
    ): ApiResponse<List<UserRatingStatsResponse>> {
        logger.debug("Querying top $limit active users")

        val topUsers = liquorRatingQueryService.getTopActiveUsers(limit)

        return ApiResponse.ok(topUsers)
    }

    // =============================================
    // 카운트 및 체크 API들
    // =============================================

    /**
     * 사용자별 평점 개수 조회
     *
     * GET /api/liquor-ratings/count/user/{userId}
     */
    @GetMapping("/count/user/{userId}")
    fun getRatingCountByUser(@PathVariable userId: String): ApiResponse<Map<String, Long>> {
        logger.debug("Querying rating count for user: $userId")

        val count = liquorRatingQueryService.getRatingCountByUser(userId)

        return ApiResponse.ok(mapOf("count" to count))
    }

    /**
     * 전통주별 평점 개수 조회
     *
     * GET /api/liquor-ratings/count/liquor/{liquorId}
     */
    @GetMapping("/count/liquor/{liquorId}")
    fun getRatingCountByLiquor(@PathVariable liquorId: String): ApiResponse<Map<String, Long>> {
        logger.debug("Querying rating count for liquor: $liquorId")

        val count = liquorRatingQueryService.getRatingCountByLiquor(liquorId)

        return ApiResponse.ok(mapOf("count" to count))
    }

    /**
     * 장소별 평점 개수 조회
     *
     * GET /api/liquor-ratings/count/place/{placeId}
     */
    @GetMapping("/count/place/{placeId}")
    fun getRatingCountByPlace(@PathVariable placeId: String): ApiResponse<Map<String, Long>> {
        logger.debug("Querying rating count for place: $placeId")

        val count = liquorRatingQueryService.getRatingCountByPlace(placeId)

        return ApiResponse.ok(mapOf("count" to count))
    }

    /**
     * 특정 사용자가 특정 전통주에 평점을 준 적이 있는지 확인
     *
     * GET /api/liquor-ratings/check?userId={userId}&liquorId={liquorId}
     */
    @GetMapping("/check")
    fun checkUserHasRatedLiquor(
        @RequestParam userId: String,
        @RequestParam liquorId: String
    ): ApiResponse<Map<String, Boolean>> {
        logger.debug("Checking if user has rated liquor - userId: $userId, liquorId: $liquorId")

        val hasRated = liquorRatingQueryService.hasUserRatedLiquor(userId, liquorId)

        return ApiResponse.ok(mapOf("hasRated" to hasRated))
    }

    // =============================================
    // 관리자용 API들
    // =============================================

    /**
     * 전체 평점 개수 조회 (관리자용)
     *
     * GET /api/liquor-ratings/admin/total-count
     */
    @GetMapping("/admin/total-count")
    fun getTotalRatingCount(): ApiResponse<Map<String, Long>> {
        // TODO: 관리자 권한 검증 로직 추가
        logger.debug("Querying total rating count (admin)")

        // 모든 사용자의 평점 개수 합산으로 계산
        val monthlyStats = liquorRatingQueryService.getMonthlyRatingStats(120) // 10년치
        val totalCount = monthlyStats.values.sum()

        return ApiResponse.ok(mapOf("totalCount" to totalCount))
    }

    /**
     * 최근 평점들 조회 (관리자용)
     *
     * GET /api/liquor-ratings/admin/recent?days=7&limit=50
     */
    @GetMapping("/admin/recent")
    fun getAdminRecentRatings(
        @RequestParam(defaultValue = "7") @Min(1) @Max(30) days: Int,
        @RequestParam(defaultValue = "50") @Min(1) @Max(200) limit: Int
    ): ApiResponse<List<LiquorRatingResponse>> {
        // TODO: 관리자 권한 검증 로직 추가
        logger.debug("Querying recent ratings for admin - days: $days, limit: $limit")

        val ratings = liquorRatingQueryService.getRecentRatings(days, limit)

        return ApiResponse.ok(ratings)
    }

    /**
     * 시스템 통계 대시보드 (관리자용)
     *
     * GET /api/liquor-ratings/admin/dashboard
     */
    @GetMapping("/admin/dashboard")
    fun getAdminDashboard(): ApiResponse<Map<String, Any>> {
        // TODO: 관리자 권한 검증 로직 추가
        logger.debug("Querying admin dashboard stats")

        val monthlyStats = liquorRatingQueryService.getMonthlyRatingStats(12)
        val scoreDistribution = liquorRatingQueryService.getOverallScoreDistribution()
        val topUsers = liquorRatingQueryService.getTopActiveUsers(5)
        val recentRatings = liquorRatingQueryService.getRecentRatings(7, 10)

        val dashboard = mapOf(
            "totalRatings" to monthlyStats.values.sum(),
            "monthlyStats" to monthlyStats,
            "scoreDistribution" to scoreDistribution,
            "topUsers" to topUsers,
            "recentRatings" to recentRatings
        )

        return ApiResponse.ok(dashboard)
    }
}

// =============================================
// 추가 DTO 클래스들 (부분 업데이트용)
// =============================================

/**
 * 점수만 업데이트하는 요청 DTO
 */
data class UpdateScoreOnlyRequest(
    @field:DecimalMin(value = "1.0", message = "평점은 1.0 이상이어야 합니다.")
    @field:DecimalMax(value = "5.0", message = "평점은 5.0 이하여야 합니다.")
    val score: Double
)

/**
 * 코멘트만 업데이트하는 요청 DTO
 */
data class UpdateCommentOnlyRequest(
    @field:jakarta.validation.constraints.Size(max = 500, message = "코멘트는 500자 이하여야 합니다.")
    val comment: String?
)
