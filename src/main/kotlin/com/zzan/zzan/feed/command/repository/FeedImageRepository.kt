package com.zzan.zzan.feed.command.repository

import com.zzan.zzan.feed.command.domain.FeedImage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FeedImageRepository : JpaRepository<FeedImage, String> {
    fun findByFeedIdOrderByOrderNum(feedId: String): List<FeedImage>
    fun deleteByFeedId(feedId: String)
}
