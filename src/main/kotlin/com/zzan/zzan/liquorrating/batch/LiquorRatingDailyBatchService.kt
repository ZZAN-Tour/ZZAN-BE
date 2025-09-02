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
 * 전통주 평점 1일 배치 처리 서비스
 *
 * 주요 책임:
 * 1. 매일 새벽 2시에 변경된 전통주들의 평점 통계 업데이트
 * 2. Redis에 저장된 변경 목록을 기반으로 효율적 처리
 * 3. 캐시 무효화 및 정리 작업
 * 4. 에러 처리 및 로깅
 */
@Service
class LiquorRatingDailyBatchService(
    private val liquorRatingRepository: LiquorRatingRepository,
    private val liquorRepository: LiquorRepository,
    private val eventPublisher: LiquorRatingEventPublisher,
    private val cacheManager: CacheManager
) {
    companion object : KLogging() {
        private const val BATCH_SIZE = 100 // 한 번에 처리할 전통주 개수
    }

    /**
     * 매일 새벽 2시에 전통주 평점 통계 업데이트
     *
     * 실행 시간: 매일 새벽 2시 (서버 부하가 적은 시간)
     * cron 표현식: "초 분 시 일 월 요일"
     */
    @Scheduled(cron = "0 0 2 * * ?")
    fun updateLiquorRatingStats() {
        val startTime = System.currentTimeMillis()

        try {
            logger.info("=== Starting daily liquor rating stats update ===")

            // 1. Redis에서 변경된 전통주 목록 조회
            val changedLiquorIds = eventPublisher.getChangedLiquorIds()

            if (changedLiquorIds.isEmpty()) {
                logger.info("No liquor ratings to update. Batch completed.")
                return
            }

            logger.info("Found ${changedLiquorIds.size} liquors with rating changes")

            // 2. 배치 단위로 처리
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
                            logger.debug("Progress: $successCount/$${changedLiquorIds.size} completed")
                        }
                    } catch (e: Exception) {
                        logger.error("Failed to update liquor stats: $liquorId", e)
                        failureCount++
                        failedLiquorIds.add(liquorId)
                    }
                }
            }

            // 3. 성공적으로 처리된 경우 Redis 정리
            if (failureCount == 0) {
                eventPublisher.clearChangedLiquors()
                logger.info("All liquor stats updated successfully. Cleared Redis change list.")
            } else {
                // 실패한 항목들은 Redis에 남겨두어 다음 배치에서 재시도
                failedLiquorIds.forEach { liquorId ->
                    eventPublisher.markLiquorForUpdate(liquorId)
                }
                logger.warn("Some liquor stats update failed. ${failedLiquorIds.size} items marked for retry.")
            }

            // 4. 배치 완료 로그
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

            // 5. 메트릭 기록 (모니터링용)
            recordBatchMetrics(changedLiquorIds.size, successCount, failureCount, duration)

        } catch (e: Exception) {
            logger.error("Critical error in daily liquor rating batch", e)
            // 중요: 배치 전체 실패 시에도 예외를 던지지 않음 (스케줄러 중단 방지)
        }
    }

    /**
     * 특정 전통주의 평점 통계 업데이트
     *
     * @param liquorId 업데이트할 전통주 ID
     */
    @Transactional
    fun updateSingleLiquorStats(liquorId: String) {
        logger.debug("Updating stats for liquor: $liquorId")

        try {
            // 1. 해당 전통주의 모든 평점 통계 조회
            val statsResult = liquorRatingRepository.findRatingStatsByLiquorId(liquorId)

            if (statsResult != null && statsResult.size >= 2) {
                val count = (statsResult[0] as Number).toInt()
                val avgScore = (statsResult[1] as Number).toDouble()

                // 2. 전통주 엔티티 조회 및 업데이트
                val liquor = liquorRepository.findById(liquorId).orElse(null)
                if (liquor != null) {
                    // Mutable Entity 패턴 사용 - 도메인 메서드로 업데이트
                    liquor.updateRatingStats(avgScore, count)

                    // JPA Dirty Checking에 의해 자동으로 UPDATE 쿼리 실행
                    // save() 호출 불필요!

                    // 3. 캐시 무효화
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
            throw e // 재시도를 위해 예외 전파
        }
    }

    /**
     * 수동 실행용 - 전체 전통주 통계 재계산
     *
     * 사용 사례:
     * - 시스템 초기 설정
     * - 데이터 마이그레이션 후
     * - 통계 오류 발견 시 전체 재계산
     */
    fun recalculateAllLiquorStats() {
        val startTime = System.currentTimeMillis()

        logger.info("=== Starting full recalculation of all liquor stats ===")

        try {
            // 1. 모든 전통주별 평점 통계 조회
            val allStats = liquorRatingRepository.findRatingStatsByLiquorId()

            logger.info("Found stats for ${allStats.size} liquors")

            var successCount = 0
            var failureCount = 0

            // 2. 배치 처리
            allStats.chunked(BATCH_SIZE).forEachIndexed { index, batch ->
                logger.info("Processing recalculation batch ${index + 1}/${(allStats.size + BATCH_SIZE - 1) / BATCH_SIZE}")

                batch.forEach { statsArray ->
                    try {
                        val liquorId = statsArray[0].toString()
                        val count = (statsArray[1] as Number).toInt()
                        val avgScore = (statsArray[2] as Number).toDouble()

                        val liquor = liquorRepository.findById(liquorId).orElse(null)
                        if (liquor != null) {
                            liquor.updateRatingStats(avgScore, count)
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
     *
     * @param liquorId 캐시를 무효화할 전통주 ID
     */
    private fun evictRelatedCaches(liquorId: String) {
        try {
            // 1. 전통주 평점 통계 캐시 무효화
            cacheManager.getCache("liquorRatingStats")?.evict(liquorId)

            // 2. 전통주별 평점 목록 캐시 무효화
            cacheManager.getCache("liquorRatings")?.evict("liquor:$liquorId")

            // 3. 상위 평점 전통주 캐시 무효화 (전체 목록이므로 모든 키 삭제)
            cacheManager.getCache("topRatedLiquors")?.clear()

            logger.debug("Evicted caches for liquor: $liquorId")
        } catch (e: Exception) {
            logger.warn("Failed to evict caches for liquor: $liquorId", e)
            // 캐시 무효화 실패는 비즈니스 로직에 영향을 주지 않으므로 예외를 전파하지 않음
        }
    }

    /**
     * 배치 메트릭 기록 (모니터링용)
     *
     * @param totalCount 총 처리 대상 개수
     * @param successCount 성공 개수
     * @param failureCount 실패 개수
     * @param durationMs 소요 시간 (밀리초)
     */
    private fun recordBatchMetrics(totalCount: Int, successCount: Int, failureCount: Int, durationMs: Long) {
        try {
            // TODO: 실제 메트릭 수집 시스템 (Micrometer, Prometheus 등)과 연동
            // 현재는 로깅으로 대체
            logger.info("""
                Batch Metrics:
                - liquor.rating.batch.total: $totalCount
                - liquor.rating.batch.success: $successCount
                - liquor.rating.batch.failure: $failureCount
                - liquor.rating.batch.duration_ms: $durationMs
                - liquor.rating.batch.success_rate: ${if (totalCount > 0) successCount * 100.0 / totalCount else 0.0}%
            """.trimIndent())
        } catch (e: Exception) {
            logger.warn("Failed to record batch metrics", e)
        }
    }

    /**
     * 배치 상태 조회 (헬스체크용)
     *
     * @return 현재 대기 중인 전통주 개수
     */
    fun getPendingUpdateCount(): Long {
        return try {
            eventPublisher.getChangedLiquorCount()
        } catch (e: Exception) {
            logger.error("Failed to get pending update count", e)
            -1L // 오류 표시
        }
    }


    /**
     * 수동 배치 실행 (관리자용)
     *
     * @param liquorIds 특정 전통주들만 처리 (null이면 전체)
     */
    fun runManualBatch(liquorIds: List<String>? = null): Map<String, Any> {
        val startTime = System.currentTimeMillis() // try-catch 블록 밖으로 이동

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
                        // 개별 항목 실패 시 로그 기록
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
            // === 완성된 catch 블록 시작 ===

            // 1. 심각한 오류 로그 기록 (예: Redis 연결 실패 등)
            logger.error("Critical error during manual batch execution", e)

            val duration = System.currentTimeMillis() - startTime

            // 2. API 호출자에게 반환할 에러 응답 맵 생성
            return mapOf(
                "status" to "ERROR",
                "message" to "수동 배치 실행 중 심각한 오류가 발생했습니다.",
                "error" to (e.message ?: "알 수 없는 오류가 발생했습니다."),
                "duration" to duration
            )
            // === 완성된 catch 블록 끝 ===
        }
    }
}
