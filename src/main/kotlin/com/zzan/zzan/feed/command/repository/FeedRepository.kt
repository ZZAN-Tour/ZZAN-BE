package com.zzan.zzan.feed.command.repository

import com.zzan.zzan.feed.command.domain.Feed
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FeedRepository : JpaRepository<Feed, String> {
    fun findByIdAndDeletedAtIsNull(id: String): Feed?
    fun existsByIdAndDeletedAtIsNull(id: String): Boolean
}
