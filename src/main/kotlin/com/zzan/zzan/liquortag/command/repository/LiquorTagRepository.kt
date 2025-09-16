// src/main/kotlin/com/zzan/zzan/liquortag/command/repository/LiquorTagRepository.kt
package com.zzan.zzan.liquortag.command.repository

import com.zzan.zzan.feed.command.domain.LiquorTag
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface LiquorTagRepository : JpaRepository<LiquorTag, String> {
    fun findByFeedIdOrderByImageId(feedId: String): List<LiquorTag>
    fun findByImageIdOrderByTagX(imageId: String): List<LiquorTag>
    fun findByLiquorId(liquorId: String): List<LiquorTag>
    fun deleteByFeedId(feedId: String)
    fun deleteByImageId(imageId: String)
    fun countByImageId(imageId: String): Int
    fun countByFeedId(feedId: String): Int

    @Query("""
        SELECT DISTINCT lt.feedId 
        FROM LiquorTag lt 
        WHERE lt.liquorId = :liquorId
        AND (:cursor IS NULL OR lt.feedId < :cursor)
        ORDER BY lt.feedId DESC
        LIMIT :limit
    """)
    fun findDistinctFeedIdsByLiquorIdWithCursorRecent(
        @Param("liquorId") liquorId: String,
        @Param("cursor") cursor: String?,
        @Param("limit") limit: Int
    ): List<String>

}
