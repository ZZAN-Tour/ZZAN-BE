package com.zzan.zzan.feed.query.event

import com.zzan.zzan.feed.command.event.FeedCreatedEvent
import com.zzan.zzan.feed.command.event.FeedUpdatedEvent
import com.zzan.zzan.feed.command.event.FeedDeletedEvent
import mu.KLogging
import org.springframework.cache.CacheManager
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class FeedEventHandler(
    private val cacheManager: CacheManager
) {
    companion object : KLogging()

    @Async
    @EventListener
    fun handleFeedCreated(event: FeedCreatedEvent) {
        logger.info("Feed created: ${event.feedId}")

        // 필요한 경우 Read Model 업데이트나 다른 후처리 작업
        // 예: 통계 업데이트, 알림 발송 등

        // 관련 캐시 무효화
        evictRelatedCaches(event.userId, event.placeId)
    }

    @Async
    @EventListener
    fun handleFeedUpdated(event: FeedUpdatedEvent) {
        logger.info("Feed updated: ${event.feedId}")

        // 캐시 무효화
        cacheManager.getCache("feed")?.evict(event.feedId)
    }

    @Async
    @EventListener
    fun handleFeedDeleted(event: FeedDeletedEvent) {
        logger.info("Feed deleted: ${event.feedId}")

        // 캐시 무효화
        cacheManager.getCache("feed")?.evict(event.feedId)
    }

    private fun evictRelatedCaches(userId: String, placeId: String) {
        // 사용자별, 장소별 피드 목록 캐시 무효화
        // 실제 캐시 키에 맞게 조정 필요
    }
}
