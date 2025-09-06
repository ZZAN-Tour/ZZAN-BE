package com.zzan.zzan.user.query.repository

import com.zzan.zzan.api.user.dto.FeedScrapResponse
import com.zzan.zzan.api.user.dto.LiquorScrapResponse
import com.zzan.zzan.user.command.domain.User
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UserQueryRepository : JpaRepository<User, String> {

    @Query("SELECT u FROM User u WHERE u.kakaoId = :kakaoId")
    fun findUserByKakaoId(kakaoId: String): User?

    @Query(
        """
            SELECT new com.zzan.zzan.api.user.dto.FeedScrapResponse(
                fs.id, f.id, f.imageUrl, p.id, p.name, p.address
            )
            FROM FeedScrap fs 
            JOIN Feed f ON fs.feedId = f.id
            JOIN Place p ON f.placeId = p.id
            WHERE fs.userId = :userId
            AND (:cursor IS NULL OR fs.id <= :cursor)
            ORDER BY fs.id DESC
        """
    )
    fun findFeedScrapsByUserIdWithCursor(
        @Param("userId") userId: String,
        @Param("cursor") cursor: String?,
        pageable: Pageable
    ): List<FeedScrapResponse>

    @Query(
        """
            SELECT new com.zzan.zzan.api.user.dto.LiquorScrapResponse(
                ls.id, l.id, l.name, l.score, l.imageUrl, l.type
            )
            FROM LiquorScrap ls 
            JOIN Liquor l ON ls.liquorId = l.id
            WHERE ls.userId = :userId
            AND (:cursor IS NULL OR ls.id <= :cursor)
            ORDER BY ls.id DESC
        """
    )
    fun findLiquorScrapsByUserIdWithCursor(
        @Param("userId") userId: String,
        @Param("cursor") cursor: String?,
        pageable: Pageable
    ): List<LiquorScrapResponse>
}
