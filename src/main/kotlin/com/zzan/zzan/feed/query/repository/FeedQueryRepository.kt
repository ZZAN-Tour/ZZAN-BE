package com.zzan.zzan.feed.query.repository

import com.zzan.zzan.api.feed.dto.FeedSearchCriteria
import com.zzan.zzan.api.feed.dto.FeedSummaryResponse
import com.zzan.zzan.feed.command.domain.Feed
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface FeedQueryRepository : JpaRepository<Feed, String> {

    /**
     * 피드 상세 조회 - Native Query나 직접 매핑으로 처리
     */
    @Query(
        """
        SELECT f FROM Feed f
        WHERE f.id = :feedId AND f.deletedAt IS NULL
    """
    )
    fun getFeedDetailByIdWithoutImages(@Param("feedId") feedId: String): Feed?

    /**
     * 특정 피드의 이미지 목록 조회
     */
    @Query(
        """
        SELECT fi FROM FeedImage fi 
        WHERE fi.feedId = :feedId 
        ORDER BY fi.orderNum
    """
    )
    fun getFeedImages(@Param("feedId") feedId: String): List<com.zzan.zzan.feed.command.domain.FeedImage>


    @Query(
        """
        SELECT new com.zzan.zzan.api.feed.dto.FeedSummaryResponse(
            f.id, u.id, u.nickname, u.profileImageUrl, f.imageUrl, f.score, null, p.name, f.createdAt
        )
        FROM Feed f
        JOIN User u ON f.userId = u.id
        JOIN Place p ON f.placeId = p.id
        WHERE f.placeId = :placeId
        AND f.deletedAt IS NULL
        AND (:cursor IS NULL OR f.id <= :cursor)
        ORDER BY f.id DESC
    """
    )
    fun findFeedByPlaceId(
        @Param("placeId") placeId: String,
        @Param("cursor") cursor: String?,
        pageable: Pageable
    ): List<FeedSummaryResponse>


    /**
     * 피드 목록 조회 (요약 정보) - 엔티티 조회 후 서비스에서 변환
     */
    @Query(
        """
        SELECT f FROM Feed f
        WHERE f.deletedAt IS NULL
        AND (:#{#criteria.userId} IS NULL OR f.userId = :#{#criteria.userId})
        AND (:#{#criteria.placeId} IS NULL OR f.placeId = :#{#criteria.placeId})
        AND (:#{#criteria.minScore} IS NULL OR f.score >= :#{#criteria.minScore})
        AND (:#{#criteria.maxScore} IS NULL OR f.score <= :#{#criteria.maxScore})
        AND (:#{#criteria.fromDate} IS NULL OR f.createdAt >= :#{#criteria.fromDate})
        AND (:#{#criteria.toDate} IS NULL OR f.createdAt <= :#{#criteria.toDate})
    """
    )
    fun getFeeds(@Param("criteria") criteria: FeedSearchCriteria, pageable: Pageable): Page<Feed>

    /**
     * 피드 존재 여부 확인
     */
    fun existsByIdAndDeletedAtIsNull(feedId: String): Boolean

    /**
     * 사용자별 피드 개수 조회
     */
    fun countByUserIdAndDeletedAtIsNull(userId: String): Long

    /**
     * 장소별 피드 개수 조회
     */
    fun countByPlaceIdAndDeletedAtIsNull(placeId: String): Long

    /**
     * 특정 피드 ID 목록으로 피드들 조회 - 최신순 (커서 기반)
     */
    @Query(
        """
        SELECT f FROM Feed f 
        WHERE f.id IN :feedIds 
        AND f.deletedAt IS NULL
        AND (:cursor IS NULL OR f.createdAt < :cursorDate OR (f.createdAt = :cursorDate AND f.id < :cursor))
        ORDER BY f.createdAt DESC, f.id DESC
    """
    )
    fun findByIdInWithCursorRecent(
        @Param("feedIds") feedIds: List<String>,
        @Param("cursor") cursor: String?,
        @Param("cursorDate") cursorDate: java.time.LocalDateTime?,
        pageable: org.springframework.data.domain.Pageable
    ): List<Feed>

    /**
     * 특정 피드 ID 목록으로 피드들 조회 - 평점순 (커서 기반)
     */
    @Query(
        """
        SELECT f FROM Feed f 
        WHERE f.id IN :feedIds 
        AND f.deletedAt IS NULL
        AND f.score IS NOT NULL
        AND (:cursorScore IS NULL OR f.score < :cursorScore OR (f.score = :cursorScore AND f.id < :cursor))
        ORDER BY f.score DESC, f.id DESC
    """
    )
    fun findByIdInWithCursorScore(
        @Param("feedIds") feedIds: List<String>,
        @Param("cursor") cursor: String?,
        @Param("cursorScore") cursorScore: Double?,
        pageable: org.springframework.data.domain.Pageable
    ): List<Feed>

    /**
     * 텍스트 검색 - 피드 내용에서 검색
     */
    @Query(
        """
        SELECT f FROM Feed f
        WHERE f.deletedAt IS NULL
        AND (f.text LIKE %:query% OR f.text IS NULL)
    """
    )
    fun searchFeedsByText(@Param("query") query: String, pageable: Pageable): Page<Feed>

    /**
     * 사용자 ID로 피드 조회 (추가 최적화용)
     */
    @Query(
        """
        SELECT f FROM Feed f
        WHERE f.userId = :userId AND f.deletedAt IS NULL
    """
    )
    fun findByUserIdAndDeletedAtIsNull(@Param("userId") userId: String, pageable: Pageable): Page<Feed>

    /**
     * 장소 ID로 피드 조회 (추가 최적화용)
     */
    @Query(
        """
        SELECT f FROM Feed f
        WHERE f.placeId = :placeId AND f.deletedAt IS NULL
    """
    )
    fun findByPlaceIdAndDeletedAtIsNull(@Param("placeId") placeId: String, pageable: Pageable): Page<Feed>

    /**
     * 평점 범위로 피드 조회 (추가 최적화용)
     */
    @Query(
        """
        SELECT f FROM Feed f
        WHERE f.deletedAt IS NULL
        AND (:minScore IS NULL OR f.score >= :minScore)
        AND (:maxScore IS NULL OR f.score <= :maxScore)
    """
    )
    fun findByScoreRangeAndDeletedAtIsNull(
        @Param("minScore") minScore: Double?,
        @Param("maxScore") maxScore: Double?,
        pageable: Pageable
    ): Page<Feed>

    fun findByIdInAndDeletedAtIsNullOrderByCreatedAtDescIdDesc(feedIds: List<String>): List<Feed>
    fun findByIdInAndDeletedAtIsNullAndScoreIsNotNullOrderByScoreDescIdDesc(feedIds: List<String>): List<Feed>
}
