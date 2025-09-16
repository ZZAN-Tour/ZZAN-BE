package com.zzan.zzan.liquorrating.batch

import com.zzan.zzan.liquor.command.repository.LiquorRepository
import com.zzan.zzan.liquorrating.command.event.LiquorRatingEventPublisher
import com.zzan.zzan.liquorrating.command.repository.LiquorRatingRepository
import mu.KLogging
import org.springframework.cache.CacheManager
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * 전통주 평점 1일 배치 처리 서비스 - 단순화된 버전
 */
@Service
class LiquorRatingDailyBatchService(
    private val liquorRatingRepository: LiquorRatingRepository,
    private val liquorRepository: LiquorRepository,
    private val eventPublisher: LiquorRatingEventPublisher,
    private val cacheManager: CacheManager
) {
    companion object : KLogging() {
        private const val BATCH_SIZE = 100
    }

    /**
     * 매일 새벽 2시에 전통주 평점 통계 업데이트
     */
    @Scheduled(cron = "0 0 2 * * ?")
    fun updateLiquorRatingStats() {
        val startTime = System.currentTimeMillis()

        try {
            logger.info("=== Starting daily liquor rating stats update ===")

            val changedLiquorIds = eventPublisher.getChangedLiquorIds()

            if (changedLiquorIds.isEmpty()) {
                logger.info("No liquor ratings to update. Batch completed.")
                return
            }

            logger.info("Found ${changedLiquorIds.size} liquors with rating changes")

            var successCount = 0
            var failureCount = 0
            val failedLiquorIds = mutableListOf<String>()

            changedLiquorIds.chunked(BATCH_SIZE).forEachIndexed { index, batch ->
                logger.info("Processing batch ${index + 1}/${(changedLiquorIds.size + BATCH_SIZE - 1) / BATCH_SIZE} (${batch.size} items)")

                batch.forEach { liquorId ->
                    try {
                        updateSingleLiquorStats(liquorId)
                        successCount++

                        if (successCount % 10 == 0) {
                            logger.debug("Progress: $successCount/${changedLiquorIds.size} completed")
                        }
                    } catch (e: Exception) {
                        logger.error("Failed to update liquor stats: $liquorId", e)
                        failureCount++
                        failedLiquorIds.add(liquorId)
                    }
                }
            }

            if (failureCount == 0) {
                eventPublisher.clearChangedLiquors()
                logger.info("All liquor stats updated successfully. Cleared Redis change list.")
            } else {
                failedLiquorIds.forEach { liquorId ->
                    eventPublisher.markLiquorForUpdate(liquorId)
                }
                logger.warn("Some liquor stats update failed. ${failedLiquorIds.size} items marked for retry.")
            }

            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime

            logger.info("""
                === Daily liquor rating stats update completed ===
                Total liquors: ${changedLiquorIds.size}
                Success: $successCount
                Failure: $failureCount
                Duration: ${duration}ms (${duration / 1000.0}s)
                Failed liquor IDs: $failedLiquorIds
            """.trimIndent())

        } catch (e: Exception) {
            logger.error("Critical error in daily liquor rating batch", e)
        }
    }

    /**
     * 특정 전통주의 평점 통계 업데이트 - 단순화된 버전
     */
    @Transactional
    fun updateSingleLiquorStats(liquorId: String) {
        logger.debug("Updating stats for liquor: $liquorId")

        try {
            // 해당 전통주의 모든 평점 통계 조회
            val statsResult = liquorRatingRepository.findRatingStatsByLiquorId(liquorId)

            if (statsResult != null && statsResult.size >= 2) {
                val count = (statsResult[0] as Number).toInt()
                val avgScore = (statsResult[1] as Number).toDouble()

                // 전통주 엔티티 조회 및 업데이트
                val liquor = liquorRepository.findById(liquorId).orElse(null)
                if (liquor != null) {
                    // 직접 필드 업데이트 (단순화)
                    liquor.score = if (count > 0) avgScore else null
                    liquor.ratingCount = count
                    liquor.ratingUpdatedAt = LocalDateTime.now()

                    // JPA Dirty Checking에 의해 자동으로 UPDATE 쿼리 실행

                    // 캐시 무효화
                    evictRelatedCaches(liquorId)

                    logger.debug("Updated liquor $liquorId stats: avgScore=$avgScore, count=$count")
                } else {
                    logger.warn("Liquor not found during stats update: $liquorId")
                }
            } else {
                logger.debug("No rating stats found for liquor: $liquorId")
            }
        } catch (e: Exception) {
            logger.error("Error updating stats for liquor: $liquorId", e)
            throw e
        }
    }

    /**
     * 수동 실행용 - 전체 전통주 통계 재계산
     */
    fun recalculateAllLiquorStats() {
        val startTime = System.currentTimeMillis()

        logger.info("=== Starting full recalculation of all liquor stats ===")

        try {
            val allStats = liquorRatingRepository.findRatingStatsByLiquorId()

            logger.info("Found stats for ${allStats.size} liquors")

            var successCount = 0
            var failureCount = 0

            allStats.chunked(BATCH_SIZE).forEachIndexed { index, batch ->
                logger.info("Processing recalculation batch ${index + 1}/${(allStats.size + BATCH_SIZE - 1) / BATCH_SIZE}")

                batch.forEach { statsArray ->
                    try {
                        val liquorId = statsArray[0].toString()
                        val count = (statsArray[1] as Number).toInt()
                        val avgScore = (statsArray[2] as Number).toDouble()

                        val liquor = liquorRepository.findById(liquorId).orElse(null)
                        if (liquor != null) {
                            // 직접 필드 업데이트
                            liquor.score = if (count > 0) avgScore else null
                            liquor.ratingCount = count
                            liquor.ratingUpdatedAt = LocalDateTime.now()

                            evictRelatedCaches(liquorId)
                            successCount++
                        }
                    } catch (e: Exception) {
                        logger.error("Failed to recalculate stats for liquor", e)
                        failureCount++
                    }
                }
            }

            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime

            logger.info("""
                === Full recalculation completed ===
                Total: ${allStats.size}
                Success: $successCount
                Failure: $failureCount
                Duration: ${duration}ms (${duration / 1000.0}s)
            """.trimIndent())

        } catch (e: Exception) {
            logger.error("Critical error in full recalculation", e)
            throw e
        }
    }

    /**
     * 특정 전통주의 캐시 무효화
     */
    private fun evictRelatedCaches(liquorId: String) {
        try {
            cacheManager.getCache("liquorRatingStats")?.evict(liquorId)
            cacheManager.getCache("liquorRatings")?.evict("liquor:$liquorId")
            cacheManager.getCache("topRatedLiquors")?.clear()

            logger.debug("Evicted caches for liquor: $liquorId")
        } catch (e: Exception) {
            logger.warn("Failed to evict caches for liquor: $liquorId", e)
        }
    }

    /**
     * 배치 상태 조회 (헬스체크용)
     */
    fun getPendingUpdateCount(): Long {
        return try {
            eventPublisher.getChangedLiquorCount()
        } catch (e: Exception) {
            logger.error("Failed to get pending update count", e)
            -1L
        }
    }

    /**
     * 수동 배치 실행 (관리자용)
     */
    fun runManualBatch(liquorIds: List<String>? = null): Map<String, Any> {
        val startTime = System.currentTimeMillis()

        return try {
            val targetIds = liquorIds ?: eventPublisher.getChangedLiquorIds().toList()

            if (targetIds.isEmpty()) {
                mapOf(
                    "status" to "NO_DATA",
                    "message" to "처리할 전통주가 없습니다.",
                    "duration" to (System.currentTimeMillis() - startTime)
                )
            } else {
                var successCount = 0
                var failureCount = 0

                targetIds.forEach { liquorId ->
                    try {
                        updateSingleLiquorStats(liquorId)
                        successCount++
                    } catch (e: Exception) {
                        logger.error("Failed to process liquorId '$liquorId' in manual batch", e)
                        failureCount++
                    }
                }

                val duration = System.currentTimeMillis() - startTime

                mapOf(
                    "status" to "COMPLETED",
                    "total" to targetIds.size,
                    "success" to successCount,
                    "failure" to failureCount,
                    "duration" to duration
                )
            }
        } catch (e: Exception) {
            logger.error("Critical error during manual batch execution", e)

            val duration = System.currentTimeMillis() - startTime

            mapOf(
                "status" to "ERROR",
                "message" to "수동 배치 실행 중 심각한 오류가 발생했습니다.",
                "error" to (e.message ?: "알 수 없는 오류가 발생했습니다."),
                "duration" to duration
            )
        }
    }
}
