package com.zzan.zzan.liquorrating.query.service

import com.zzan.zzan.api.liquorrating.dto.LiquorRatingResponse
import com.zzan.zzan.api.liquorrating.dto.LiquorRatingStatsResponse
import com.zzan.zzan.api.liquorrating.dto.UserRatingStatsResponse
import com.zzan.zzan.common.exception.CustomException
import com.zzan.zzan.liquor.command.repository.LiquorRepository
import com.zzan.zzan.liquorrating.command.domain.LiquorRating
import com.zzan.zzan.liquorrating.command.repository.LiquorRatingRepository
import com.zzan.zzan.place.command.repository.PlaceRepository
import com.zzan.zzan.user.command.repository.UserRepository
import mu.KLogging
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 전통주 평점 Query Service 구현체
 *
 * 주요 책임:
 * 1. 다양한 조건으로 평점 조회
 * 2. 평점 통계 계산 및 제공
 * 3. 캐싱을 통한 성능 최적화
 * 4. 연관 데이터 조합 (사용자, 전통주, 장소 정보)
 * 5. 검색 및 필터링 기능
 */
@Service
@Transactional(readOnly = true)
class LiquorRatingQueryServiceImpl(
    private val liquorRatingRepository: LiquorRatingRepository,
    private val liquorRepository: LiquorRepository,
    private val userRepository: UserRepository,
    private val placeRepository: PlaceRepository
) : LiquorRatingQueryService {

    companion object : KLogging() {
        private val MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM")
    }

    // =============================================
    // 기본 조회 메서드들
    // =============================================

    /**
     * 특정 전통주의 모든 평점 조회 (캐시 적용)
     */
    @Cacheable("liquorRatings", key = "'liquor:' + #liquorId")
    override fun getRatingsByLiquor(liquorId: String): List<LiquorRatingResponse> {
        logger.debug("Querying ratings for liquor: $liquorId")

        val ratings = liquorRatingRepository.findByLiquorIdOrderByCreatedAtDesc(liquorId)
        val responses = ratings.map { convertToResponse(it) }

        logger.debug("Found ${responses.size} ratings for liquor: $liquorId")
        return responses
    }

    /**
     * 특정 사용자의 모든 평점 조회 (캐시 적용)
     */
    @Cacheable("liquorRatings", key = "'user:' + #userId")
    override fun getRatingsByUser(userId: String): List<LiquorRatingResponse> {
        logger.debug("Querying ratings for user: $userId")

        val ratings = liquorRatingRepository.findByUserIdOrderByCreatedAtDesc(userId)
        val responses = ratings.map { convertToResponse(it) }

        logger.debug("Found ${responses.size} ratings for user: $userId")
        return responses
    }

    /**
     * 특정 전통주의 평점 통계 조회 (캐시 적용)
     */
    @Cacheable("liquorRatingStats", key = "#liquorId")
    override fun getRatingStats(liquorId: String): LiquorRatingStatsResponse {
        logger.debug("Querying rating stats for liquor: $liquorId")

        // 전통주 정보 조회
        val liquor = liquorRepository.findById(liquorId).orElseThrow {
            CustomException(HttpStatus.NOT_FOUND, "전통주를 찾을 수 없습니다.")
        }

        // 모든 평점 조회
        val ratings = liquorRatingRepository.findByLiquorIdOrderByCreatedAtDesc(liquorId)

        // 점수별 분포 계산
        val scoreDistribution = ratings
            .groupBy { kotlin.math.floor(it.score).toInt().toString() }
            .mapValues { it.value.size }

        // 최신 평점 5개
        val latestRatings = ratings.take(5).map { convertToResponse(it) }

        val statsResponse = LiquorRatingStatsResponse(
            liquorId = liquorId,
            liquorName = liquor.name,
            averageScore = liquor.score,
            totalRatings = liquor.ratingCount,
            scoreDistribution = scoreDistribution,
            latestRatings = latestRatings
        )

        logger.debug("Generated rating stats for liquor: $liquorId, avgScore: ${liquor.score}, totalRatings: ${liquor.ratingCount}")
        return statsResponse
    }

    /**
     * 특정 평점 상세 조회
     */
    override fun getRatingById(ratingId: String): LiquorRatingResponse? {
        logger.debug("Querying rating by id: $ratingId")

        val rating = liquorRatingRepository.findById(ratingId).orElse(null) ?: return null
        return convertToResponse(rating)
    }

    // =============================================
    // 비즈니스 로직 기반 조회
    // =============================================

    /**
     * 수정 가능한 사용자 평점 조회
     */
    override fun getEditableRatingsByUser(userId: String): List<LiquorRatingResponse> {
        logger.debug("Querying editable ratings for user: $userId")

        val ratings = liquorRatingRepository.findByUserIdOrderByCreatedAtDesc(userId)
        val editableRatings = ratings
            .filter { it.isWithinEditablePeriod() } // 도메인 로직 활용
            .map { convertToResponse(it) }

        logger.debug("Found ${editableRatings.size} editable ratings for user: $userId")
        return editableRatings
    }

    /**
     * 특정 피드와 관련된 평점들 조회
     */
    override fun getRatingsBySourceFeed(feedId: String): List<LiquorRatingResponse> {
        logger.debug("Querying ratings for source feed: $feedId")

        val ratings = liquorRatingRepository.findBySourceFeedId(feedId)
        val responses = ratings.map { convertToResponse(it) }

        logger.debug("Found ${responses.size} ratings for source feed: $feedId")
        return responses
    }

    /**
     * 특정 장소에서 작성된 평점들 조회
     */
    override fun getRatingsByPlace(placeId: String): List<LiquorRatingResponse> {
        logger.debug("Querying ratings for place: $placeId")

        val ratings = liquorRatingRepository.findByPlaceIdOrderByCreatedAtDesc(placeId)
        val responses = ratings.map { convertToResponse(it) }

        logger.debug("Found ${responses.size} ratings for place: $placeId")
        return responses
    }

    /**
     * 높은 평점 순으로 전통주 조회 (캐시 적용)
     */
    @Cacheable("topRatedLiquors", key = "#minRatingCount + ':' + #limit")
    override fun getTopRatedLiquors(minRatingCount: Int, limit: Int): List<LiquorRatingStatsResponse> {
        logger.debug("Querying top rated liquors - minRatingCount: $minRatingCount, limit: $limit")

        // Repository에서 높은 평점 순으로 조회
        val topLiquors = liquorRatingRepository.findTopRatedLiquors(minRatingCount, limit)

        val responses = topLiquors.map { liquor ->
            // 각 전통주의 통계 생성
            val ratings = liquorRatingRepository.findByLiquorIdOrderByCreatedAtDesc(liquor.id)
            val scoreDistribution = ratings
                .groupBy { kotlin.math.floor(it.score).toInt().toString() }
                .mapValues { it.value.size }

            LiquorRatingStatsResponse(
                liquorId = liquor.id,
                liquorName = liquor.name,
                averageScore = liquor.score,
                totalRatings = liquor.ratingCount,
                scoreDistribution = scoreDistribution,
                latestRatings = ratings.take(3).map { convertToResponse(it) } // 상위 3개만
            )
        }

        logger.debug("Found ${responses.size} top rated liquors")
        return responses
    }

    /**
     * 사용자의 평점 통계 조회 (캐시 적용)
     */
    @Cacheable("userRatingStats", key = "#userId")
    override fun getUserRatingStats(userId: String): UserRatingStatsResponse {
        logger.debug("Querying user rating stats for: $userId")

        // 사용자 정보 조회
        val user = userRepository.findByIdAndDeletedAtIsNull(userId)
            ?: throw CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.")

        // 사용자의 모든 평점 조회
        val ratings = liquorRatingRepository.findByUserIdOrderByCreatedAtDesc(userId)

        if (ratings.isEmpty()) {
            return UserRatingStatsResponse(
                userId = userId,
                userNickname = user.nickname ?: "Unknown",
                totalRatings = 0,
                averageScore = 0.0,
                scoreDistribution = emptyMap(),
                favoriteTypes = emptyList(),
                ratingsByMonth = emptyMap()
            )
        }

        // 평균 점수 계산
        val averageScore = ratings.map { it.score }.average()

        // 점수별 분포 계산
        val scoreDistribution = ratings
            .groupBy { kotlin.math.floor(it.score).toInt().toString() }
            .mapValues { it.value.size }

        // 자주 평가한 전통주 타입 계산
        val liquorIds = ratings.map { it.liquorId }.distinct()
        val liquors = liquorRepository.findAllById(liquorIds)
        val favoriteTypes = liquors
            .groupBy { it.type }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(3)
            .map { it.first }

        // 월별 평점 수 계산
        val ratingsByMonth = ratings
            .filter { it.createdAt != null }
            .groupBy { it.createdAt!!.format(MONTH_FORMATTER) }
            .mapValues { it.value.size }
            .toSortedMap(reverseOrder()) // 최신 월부터 정렬

        val statsResponse = UserRatingStatsResponse(
            userId = userId,
            userNickname = user.nickname ?: "Unknown",
            totalRatings = ratings.size,
            averageScore = averageScore,
            scoreDistribution = scoreDistribution,
            favoriteTypes = favoriteTypes,
            ratingsByMonth = ratingsByMonth
        )

        logger.debug("Generated user rating stats for: $userId, totalRatings: ${ratings.size}, avgScore: $averageScore")
        return statsResponse
    }

    // =============================================
    // 검색 및 필터링 메서드들
    // =============================================

    /**
     * 평점 검색 (텍스트 기반)
     */
    override fun searchRatings(query: String, minScore: Double?, maxResults: Int): List<LiquorRatingResponse> {
        logger.debug("Searching ratings - query: '$query', minScore: $minScore")

        if (query.isBlank()) {
            return emptyList()
        }

        val allRatings = liquorRatingRepository.findRatingsWithComments()
        val filteredRatings = allRatings
            .filter { rating ->
                // 코멘트에서 키워드 검색 (대소문자 무시)
                rating.comment?.lowercase()?.contains(query.lowercase()) == true
            }
            .let { ratings ->
                // 최소 점수 필터링
                if (minScore != null) {
                    ratings.filter { it.score >= minScore }
                } else {
                    ratings
                }
            }
            .sortedByDescending { it.createdAt } // 최신순 정렬
            .take(maxResults)

        val responses = filteredRatings.map { convertToResponse(it) }

        logger.debug("Found ${responses.size} ratings for query: '$query'")
        return responses
    }

    /**
     * 특정 전통주 타입별 평점 조회
     */
    override fun getRatingsByLiquorType(liquorType: String, limit: Int): List<LiquorRatingResponse> {
        logger.debug("Querying ratings by liquor type: $liquorType")

        val ratings = liquorRatingRepository.findRatingsByLiquorType(liquorType)
            .take(limit)

        val responses = ratings.map { convertToResponse(it) }

        logger.debug("Found ${responses.size} ratings for liquor type: $liquorType")
        return responses
    }

    /**
     * 최근 N일 내 작성된 평점 조회
     */
    override fun getRecentRatings(days: Int, limit: Int): List<LiquorRatingResponse> {
        logger.debug("Querying recent ratings - days: $days, limit: $limit")

        val fromDate = LocalDateTime.now().minusDays(days.toLong())
        val ratings = liquorRatingRepository.findRecentRatings(fromDate)
            .take(limit)

        val responses = ratings.map { convertToResponse(it) }

        logger.debug("Found ${responses.size} recent ratings in last $days days")
        return responses
    }

    /**
     * 코멘트가 있는 평점들만 조회
     */
    override fun getRatingsWithComments(minCommentLength: Int, limit: Int): List<LiquorRatingResponse> {
        logger.debug("Querying ratings with comments - minLength: $minCommentLength, limit: $limit")

        val ratings = liquorRatingRepository.findRatingsWithComments()
            .filter { rating ->
                (rating.comment?.length ?: 0) >= minCommentLength
            }
            .take(limit)

        val responses = ratings.map { convertToResponse(it) }

        logger.debug("Found ${responses.size} ratings with comments")
        return responses
    }

    /**
     * 특정 점수 범위의 평점 조회
     */
    override fun getRatingsByScoreRange(minScore: Double, maxScore: Double, limit: Int): List<LiquorRatingResponse> {
        logger.debug("Querying ratings by score range: $minScore - $maxScore")

        require(minScore <= maxScore) { "최소 점수는 최대 점수보다 작거나 같아야 합니다." }
        require(minScore >= 1.0 && maxScore <= 5.0) { "점수는 1.0~5.0 범위여야 합니다." }

        val allRatings = liquorRatingRepository.findAllOrderByCreatedAtDesc()
        val filteredRatings = allRatings
            .filter { it.score >= minScore && it.score <= maxScore }
            .take(limit)

        val responses = filteredRatings.map { convertToResponse(it) }

        logger.debug("Found ${responses.size} ratings in score range: $minScore - $maxScore")
        return responses
    }

    // =============================================
    // 카운트 및 존재 여부 확인 메서드들
    // =============================================

    /**
     * 사용자별 평점 개수 조회
     */
    override fun getRatingCountByUser(userId: String): Long {
        return liquorRatingRepository.countByUserId(userId)
    }

    /**
     * 전통주별 평점 개수 조회
     */
    override fun getRatingCountByLiquor(liquorId: String): Long {
        return liquorRatingRepository.countByLiquorId(liquorId)
    }

    /**
     * 장소별 평점 개수 조회
     */
    override fun getRatingCountByPlace(placeId: String): Long {
        return liquorRatingRepository.countByPlaceId(placeId)
    }

    /**
     * 특정 사용자가 특정 전통주에 평점을 준 적이 있는지 확인
     */
    override fun hasUserRatedLiquor(userId: String, liquorId: String): Boolean {
        return liquorRatingRepository.existsByUserIdAndLiquorId(userId, liquorId)
    }

    // =============================================
    // 통계 및 분석 메서드들
    // =============================================

    /**
     * 월별 평점 통계 조회 (전체) - 캐시 적용
     */
    @Cacheable("monthlyRatingStats", key = "#months")
    override fun getMonthlyRatingStats(months: Int): Map<String, Long> {
        logger.debug("Querying monthly rating stats for $months months")

        val fromDate = LocalDateTime.now().minusMonths(months.toLong())
        val ratings = liquorRatingRepository.findRecentRatings(fromDate)

        val monthlyStats = ratings
            .filter { it.createdAt != null }
            .groupBy { it.createdAt!!.format(MONTH_FORMATTER) }
            .mapValues { it.value.size.toLong() }
            .toSortedMap(reverseOrder()) // 최신 월부터 정렬

        logger.debug("Generated monthly stats for ${monthlyStats.size} months")
        return monthlyStats
    }

    /**
     * 평점 분포 통계 조회 (전체) - 캐시 적용
     */
    @Cacheable("overallScoreDistribution")
    override fun getOverallScoreDistribution(): Map<String, Long> {
        logger.debug("Querying overall score distribution")

        val allRatings = liquorRatingRepository.findAll()
        val distribution = allRatings
            .groupBy { kotlin.math.floor(it.score).toInt().toString() }
            .mapValues { it.value.size.toLong() }
            .toSortedMap(reverseOrder()) // 5점부터 정렬

        logger.debug("Generated overall score distribution: $distribution")
        return distribution
    }

    /**
     * 활발한 사용자 TOP N 조회 - 캐시 적용
     */
    @Cacheable("topActiveUsers", key = "#limit")
    override fun getTopActiveUsers(limit: Int): List<UserRatingStatsResponse> {
        logger.debug("Querying top $limit active users")

        // 모든 사용자의 평점 개수 계산
        val userRatingCounts = liquorRatingRepository.findAll()
            .groupBy { it.userId }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(limit)

        // 상위 사용자들의 상세 통계 생성
        val topUsers = userRatingCounts.mapNotNull { (userId, _) ->
            try {
                getUserRatingStats(userId)
            } catch (e: Exception) {
                logger.warn("Failed to get stats for user: $userId", e)
                null
            }
        }

        logger.debug("Generated stats for ${topUsers.size} top active users")
        return topUsers
    }

    // =============================================
    // 유틸리티 메서드들
    // =============================================

    /**
     * LiquorRating 엔티티를 Response DTO로 변환
     */
    private fun convertToResponse(rating: LiquorRating): LiquorRatingResponse {
        // 연관 데이터 조회 (성능 최적화를 위해 필요시 배치 조회로 변경 가능)
        val user = userRepository.findByIdAndDeletedAtIsNull(rating.userId)
        val liquor = liquorRepository.findById(rating.liquorId).orElse(null)
        val place = placeRepository.findById(rating.placeId).orElse(null)

        return LiquorRatingResponse(
            id = rating.id,
            userId = rating.userId,
            userNickname = user?.nickname ?: "Unknown",
            liquorId = rating.liquorId,
            liquorName = liquor?.name ?: "Unknown",
            score = rating.score, // getter 사용
            comment = rating.comment, // getter 사용
            placeName = place?.name ?: "Unknown",
            createdAt = rating.createdAt ?: throw IllegalStateException("CreatedAt should not be null")
        )
    }

    /**
     * 배치 조회를 위한 성능 최적화 메서드 (N+1 문제 해결)
     */
    private fun convertToResponsesBatch(ratings: List<LiquorRating>): List<LiquorRatingResponse> {
        if (ratings.isEmpty()) return emptyList()

        // 모든 연관 ID 수집
        val userIds = ratings.map { it.userId }.distinct()
        val liquorIds = ratings.map { it.liquorId }.distinct()
        val placeIds = ratings.map { it.placeId }.distinct()

        // 배치로 조회 (N+1 문제 방지)
        val users = userRepository.findAllById(userIds).associateBy { it.id }
        val liquors = liquorRepository.findAllById(liquorIds).associateBy { it.id }
        val places = placeRepository.findAllById(placeIds).associateBy { it.id }

        // 변환
        return ratings.map { rating ->
            LiquorRatingResponse(
                id = rating.id,
                userId = rating.userId,
                userNickname = users[rating.userId]?.nickname ?: "Unknown",
                liquorId = rating.liquorId,
                liquorName = liquors[rating.liquorId]?.name ?: "Unknown",
                score = rating.score,
                comment = rating.comment,
                placeName = places[rating.placeId]?.name ?: "Unknown",
                createdAt = rating.createdAt ?: throw IllegalStateException("CreatedAt should not be null")
            )
        }
    }
}
