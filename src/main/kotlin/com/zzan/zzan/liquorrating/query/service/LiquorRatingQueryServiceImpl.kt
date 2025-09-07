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
import java.time.temporal.ChronoUnit

/**
 * 전통주 평점 Query Service 구현체 - 단순화된 버전
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

    @Cacheable("liquorRatings", key = "'liquor:' + #liquorId")
    override fun getRatingsByLiquor(liquorId: String): List<LiquorRatingResponse> {
        logger.debug("Querying ratings for liquor: $liquorId")

        val ratings = liquorRatingRepository.findByLiquorIdOrderByCreatedAtDesc(liquorId)
        val responses = ratings.map { convertToResponse(it) }

        logger.debug("Found ${responses.size} ratings for liquor: $liquorId")
        return responses
    }

    @Cacheable("liquorRatings", key = "'user:' + #userId")
    override fun getRatingsByUser(userId: String): List<LiquorRatingResponse> {
        logger.debug("Querying ratings for user: $userId")

        val ratings = liquorRatingRepository.findByUserIdOrderByCreatedAtDesc(userId)
        val responses = ratings.map { convertToResponse(it) }

        logger.debug("Found ${responses.size} ratings for user: $userId")
        return responses
    }

    @Cacheable("liquorRatingStats", key = "#liquorId")
    override fun getRatingStats(liquorId: String): LiquorRatingStatsResponse {
        logger.debug("Querying rating stats for liquor: $liquorId")

        val liquor = liquorRepository.findById(liquorId).orElseThrow {
            CustomException(HttpStatus.NOT_FOUND, "전통주를 찾을 수 없습니다.")
        }

        val ratings = liquorRatingRepository.findByLiquorIdOrderByCreatedAtDesc(liquorId)

        val scoreDistribution = ratings
            .groupBy { kotlin.math.floor(it.score).toInt().toString() }
            .mapValues { it.value.size }

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

    override fun getRatingById(ratingId: String): LiquorRatingResponse? {
        logger.debug("Querying rating by id: $ratingId")

        val rating = liquorRatingRepository.findById(ratingId).orElse(null) ?: return null
        return convertToResponse(rating)
    }

    // =============================================
    // 비즈니스 로직 기반 조회
    // =============================================

    override fun getEditableRatingsByUser(userId: String): List<LiquorRatingResponse> {
        logger.debug("Querying editable ratings for user: $userId")

        val ratings = liquorRatingRepository.findByUserIdOrderByCreatedAtDesc(userId)
        val editableRatings = ratings
            .filter { isWithinEditablePeriod(it) }
            .map { convertToResponse(it) }

        logger.debug("Found ${editableRatings.size} editable ratings for user: $userId")
        return editableRatings
    }

    override fun getRatingsBySourceFeed(feedId: String): List<LiquorRatingResponse> {
        logger.debug("Querying ratings for source feed: $feedId")

        val ratings = liquorRatingRepository.findBySourceFeedId(feedId)
        val responses = ratings.map { convertToResponse(it) }

        logger.debug("Found ${responses.size} ratings for source feed: $feedId")
        return responses
    }

    override fun getRatingsByPlace(placeId: String): List<LiquorRatingResponse> {
        logger.debug("Querying ratings for place: $placeId")

        val ratings = liquorRatingRepository.findByPlaceIdOrderByCreatedAtDesc(placeId)
        val responses = ratings.map { convertToResponse(it) }

        logger.debug("Found ${responses.size} ratings for place: $placeId")
        return responses
    }

    @Cacheable("topRatedLiquors", key = "#minRatingCount + ':' + #limit")
    override fun getTopRatedLiquors(minRatingCount: Int, limit: Int): List<LiquorRatingStatsResponse> {
        logger.debug("Querying top rated liquors - minRatingCount: $minRatingCount, limit: $limit")

        val topLiquors = liquorRatingRepository.findTopRatedLiquors(minRatingCount, limit)

        val responses = topLiquors.map { liquor ->
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
                latestRatings = ratings.take(3).map { convertToResponse(it) }
            )
        }

        logger.debug("Found ${responses.size} top rated liquors")
        return responses
    }

    @Cacheable("userRatingStats", key = "#userId")
    override fun getUserRatingStats(userId: String): UserRatingStatsResponse {
        logger.debug("Querying user rating stats for: $userId")

        val user = userRepository.findByIdAndDeletedAtIsNull(userId)
            ?: throw CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.")

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

        val averageScore = ratings.map { it.score }.average()

        val scoreDistribution = ratings
            .groupBy { kotlin.math.floor(it.score).toInt().toString() }
            .mapValues { it.value.size }

        val liquorIds = ratings.map { it.liquorId }.distinct()
        val liquors = liquorRepository.findAllById(liquorIds)
        val favoriteTypes = liquors
            .groupBy { it.type }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(3)
            .map { it.first }

        val ratingsByMonth = ratings
            .filter { it.createdAt != null }
            .groupBy { it.createdAt!!.format(MONTH_FORMATTER) }
            .mapValues { it.value.size }
            .toSortedMap(reverseOrder())

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

    override fun searchRatings(query: String, minScore: Double?, maxResults: Int): List<LiquorRatingResponse> {
        logger.debug("Searching ratings - query: '$query', minScore: $minScore")

        if (query.isBlank()) {
            return emptyList()
        }

        val allRatings = liquorRatingRepository.findRatingsWithComments()
        val filteredRatings = allRatings
            .filter { rating ->
                rating.comment?.lowercase()?.contains(query.lowercase()) == true
            }
            .let { ratings ->
                if (minScore != null) {
                    ratings.filter { it.score >= minScore }
                } else {
                    ratings
                }
            }
            .sortedByDescending { it.createdAt }
            .take(maxResults)

        val responses = filteredRatings.map { convertToResponse(it) }

        logger.debug("Found ${responses.size} ratings for query: '$query'")
        return responses
    }

    override fun getRatingsByLiquorType(liquorType: String, limit: Int): List<LiquorRatingResponse> {
        logger.debug("Querying ratings by liquor type: $liquorType")

        val ratings = liquorRatingRepository.findRatingsByLiquorType(liquorType)
            .take(limit)

        val responses = ratings.map { convertToResponse(it) }

        logger.debug("Found ${responses.size} ratings for liquor type: $liquorType")
        return responses
    }

    override fun getRecentRatings(days: Int, limit: Int): List<LiquorRatingResponse> {
        logger.debug("Querying recent ratings - days: $days, limit: $limit")

        val fromDate = LocalDateTime.now().minusDays(days.toLong())
        val ratings = liquorRatingRepository.findRecentRatings(fromDate)
            .take(limit)

        val responses = ratings.map { convertToResponse(it) }

        logger.debug("Found ${responses.size} recent ratings in last $days days")
        return responses
    }

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

    override fun getRatingCountByUser(userId: String): Long {
        return liquorRatingRepository.countByUserId(userId)
    }

    override fun getRatingCountByLiquor(liquorId: String): Long {
        return liquorRatingRepository.countByLiquorId(liquorId)
    }

    override fun getRatingCountByPlace(placeId: String): Long {
        return liquorRatingRepository.countByPlaceId(placeId)
    }

    override fun hasUserRatedLiquor(userId: String, liquorId: String): Boolean {
        return liquorRatingRepository.existsByUserIdAndLiquorId(userId, liquorId)
    }

    // =============================================
    // 통계 및 분석 메서드들
    // =============================================

    @Cacheable("monthlyRatingStats", key = "#months")
    override fun getMonthlyRatingStats(months: Int): Map<String, Long> {
        logger.debug("Querying monthly rating stats for $months months")

        val fromDate = LocalDateTime.now().minusMonths(months.toLong())
        val ratings = liquorRatingRepository.findRecentRatings(fromDate)

        val monthlyStats = ratings
            .filter { it.createdAt != null }
            .groupBy { it.createdAt!!.format(MONTH_FORMATTER) }
            .mapValues { it.value.size.toLong() }
            .toSortedMap(reverseOrder())

        logger.debug("Generated monthly stats for ${monthlyStats.size} months")
        return monthlyStats
    }

    @Cacheable("overallScoreDistribution")
    override fun getOverallScoreDistribution(): Map<String, Long> {
        logger.debug("Querying overall score distribution")

        val allRatings = liquorRatingRepository.findAll()
        val distribution = allRatings
            .groupBy { kotlin.math.floor(it.score).toInt().toString() }
            .mapValues { it.value.size.toLong() }
            .toSortedMap(reverseOrder())

        logger.debug("Generated overall score distribution: $distribution")
        return distribution
    }

    @Cacheable("topActiveUsers", key = "#limit")
    override fun getTopActiveUsers(limit: Int): List<UserRatingStatsResponse> {
        logger.debug("Querying top $limit active users")

        val userRatingCounts = liquorRatingRepository.findAll()
            .groupBy { it.userId }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(limit)

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

    private fun convertToResponse(rating: LiquorRating): LiquorRatingResponse {
        val user = userRepository.findByIdAndDeletedAtIsNull(rating.userId)
        val liquor = liquorRepository.findById(rating.liquorId).orElse(null)
        val place = placeRepository.findById(rating.placeId).orElse(null)

        return LiquorRatingResponse(
            id = rating.id,
            userId = rating.userId,
            userNickname = user?.nickname ?: "Unknown",
            liquorId = rating.liquorId,
            liquorName = liquor?.name ?: "Unknown",
            score = rating.score,
            comment = rating.comment,
            placeName = place?.name ?: "Unknown",
            createdAt = rating.createdAt ?: throw IllegalStateException("CreatedAt should not be null")
        )
    }

    private fun isWithinEditablePeriod(rating: LiquorRating): Boolean {
        val daysSinceCreated = ChronoUnit.DAYS.between(rating.createdAt, LocalDateTime.now())
        return daysSinceCreated <= 7
    }
}
