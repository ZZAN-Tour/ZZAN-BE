package com.zzan.zzan.user.command.infrastructure

import com.zzan.zzan.user.command.domain.FeedScrap
import org.springframework.data.jpa.repository.JpaRepository

interface FeedScrapRepository : JpaRepository<FeedScrap, String> {
    fun existsByUserIdAndFeedId(userId: String, feedId: String): Boolean
    fun findByUserIdAndFeedId(userId: String, feedId: String): FeedScrap?
}
