package com.zzan.zzan.liquorrating.command.repository

import com.zzan.zzan.liquor.command.domain.Liquor
import com.zzan.zzan.liquorrating.command.domain.LiquorRating
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * 전통주 평점 Repository 인터페이스
 *
 * 기본 CRUD 외에 비즈니스 요구사항에 따른 다양한 조회 메서드 제공
 */
@Repository
interface LiquorRatingRepository : JpaRepository<LiquorRating, String> {

    // =============================================
    // 기본 조회 메서드들
    // =============================================

    /**
     * 특정 전통주의 모든 평점 조회 (최신순)
     */
    fun findByLiquorIdOrderByCreatedAtDesc(liquorId: String): List<LiquorRating>

    /**
     * 특정 사용자의 모든 평점 조회 (최신순)
     */
    fun findByUserIdOrderByCreatedAtDesc(userId: String): List<LiquorRating>

    /**
     * 특정 사용자의 특정 전통주에 대한 평점들 조회 (같은 전통주 여러 평점 가능)
     */
    fun findByUserIdAndLiquorIdOrderByCreatedAtDesc(userId: String, liquorId: String): List<LiquorRating>

    /**
     * 특정 피드 기반 평점 조회
     */
    fun findBySourceFeedId(feedId: String): List<LiquorRating>

    /**
     * 특정 장소에서 작성된 평점들 조회 (최신순)
     */
    fun findByPlaceIdOrderByCreatedAtDesc(placeId: String): List<LiquorRating>

    // =============================================
    // 통계용 쿼리 메서드들 (배치 처리에서 사용)
    // =============================================

    /**
     * 모든 전통주별 평점 통계 조회 (전체 재계산용)
     */
    @Query("""
        SELECT r.liquorId, COUNT(r.id), AVG(r._score), SUM(r._score)
        FROM LiquorRating r 
        GROUP BY r.liquorId
    """)
    fun findRatingStatsByLiquorId(): List<Array<Any>>

    /**
     * 특정 전통주의 평점 통계 조회
     */
    @Query("""
        SELECT COUNT(r.id), AVG(r._score)
        FROM LiquorRating r 
        WHERE r.liquorId = :liquorId
    """)
    fun findRatingStatsByLiquorId(@Param("liquorId") liquorId: String): Array<Any>?

    /**
     * 평점 점수별 분포 조회
     */
    @Query("""
        SELECT FLOOR(r._score) as scoreGroup, COUNT(r.id) as count
        FROM LiquorRating r 
        WHERE r.liquorId = :liquorId
        GROUP BY FLOOR(r._score)
        ORDER BY FLOOR(r._score) DESC
    """)
    fun findScoreDistributionByLiquorId(@Param("liquorId") liquorId: String): List<Array<Any>>

    // =============================================
    // 고급 조회 메서드들
    // =============================================

    /**
     * 높은 평점을 받은 전통주들 조회 (추천용)
     *
     * @param minRatingCount 최소 평점 개수
     * @param limit 조회할 개수
     */
    @Query("""
        SELECT l FROM Liquor l 
        WHERE l._ratingCount >= :minRatingCount 
        AND l._score IS NOT NULL
        ORDER BY l._score DESC, l._ratingCount DESC
        LIMIT :limit
    """)
    fun findTopRatedLiquors(
        @Param("minRatingCount") minRatingCount: Int,
        @Param("limit") limit: Int
    ): List<Liquor>

    /**
     * 최근 N일 내에 작성된 평점들 조회
     */
    @Query("""
        SELECT r FROM LiquorRating r 
        WHERE r.createdAt >= :fromDate
        ORDER BY r.createdAt DESC
    """)
    fun findRecentRatings(@Param("fromDate") fromDate: java.time.LocalDateTime): List<LiquorRating>

    /**
     * 특정 점수 이상의 평점들만 조회
     */
    @Query("""
        SELECT r FROM LiquorRating r 
        WHERE r._score >= :minScore
        ORDER BY r._score DESC, r.createdAt DESC
    """)
    fun findRatingsAboveScore(@Param("minScore") minScore: Double): List<LiquorRating>

    /**
     * 특정 전통주 타입에 대한 평점들 조회
     */
    @Query("""
        SELECT r FROM LiquorRating r 
        JOIN Liquor l ON r.liquorId = l.id 
        WHERE l.type = :liquorType
        ORDER BY r.createdAt DESC
    """)
    fun findRatingsByLiquorType(@Param("liquorType") liquorType: String): List<LiquorRating>

    /**
     * 코멘트가 있는 평점들만 조회
     */
    @Query("""
        SELECT r FROM LiquorRating r 
        WHERE r._comment IS NOT NULL 
        AND LENGTH(TRIM(r._comment)) > 0
        ORDER BY r.createdAt DESC
    """)
    fun findRatingsWithComments(): List<LiquorRating>

    // =============================================
    // 카운트 및 존재 여부 확인 메서드들
    // =============================================

    /**
     * 특정 전통주의 평점 개수 조회
     */
    fun countByLiquorId(liquorId: String): Long

    /**
     * 특정 사용자의 평점 개수 조회
     */
    fun countByUserId(userId: String): Long

    /**
     * 특정 장소에서 작성된 평점 개수 조회
     */
    fun countByPlaceId(placeId: String): Long

    /**
     * 특정 사용자가 특정 전통주에 평점을 준 적이 있는지 확인
     */
    fun existsByUserIdAndLiquorId(userId: String, liquorId: String): Boolean

    /**
     * 특정 피드를 기반으로 작성된 평점이 있는지 확인
     */
    fun existsBySourceFeedId(feedId: String): Boolean

    // =============================================
    // 삭제 관련 메서드들
    // =============================================

    /**
     * 특정 사용자의 모든 평점 삭제 (사용자 탈퇴 시)
     */
    fun deleteByUserId(userId: String)

    /**
     * 특정 피드와 관련된 모든 평점 삭제 (피드 삭제 시)
     */
    fun deleteBySourceFeedId(feedId: String)

    /**
     * 특정 전통주의 모든 평점 삭제 (전통주 삭제 시 - 실제로는 거의 사용 안됨)
     */
    fun deleteByLiquorId(liquorId: String)

    // =============================================
    // 성능 최적화용 배치 조회 메서드들
    // =============================================

    /**
     * 여러 전통주의 평점들을 한 번에 조회 (N+1 문제 방지)
     */
    @Query("""
        SELECT r FROM LiquorRating r 
        WHERE r.liquorId IN :liquorIds
        ORDER BY r.liquorId, r.createdAt DESC
    """)
    fun findByLiquorIdIn(@Param("liquorIds") liquorIds: List<String>): List<LiquorRating>

    /**
     * 여러 사용자의 평점들을 한 번에 조회
     */
    @Query("""
        SELECT r FROM LiquorRating r 
        WHERE r.userId IN :userIds
        ORDER BY r.userId, r.createdAt DESC
    """)
    fun findByUserIdIn(@Param("userIds") userIds: List<String>): List<LiquorRating>

    /**
     * 페이징을 위한 전체 평점 조회 (최신순)
     */
    @Query("""
        SELECT r FROM LiquorRating r 
        ORDER BY r.createdAt DESC
    """)
    fun findAllOrderByCreatedAtDesc(): List<LiquorRating>
}
