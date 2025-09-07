package com.zzan.zzan.liquorrating.command.event

import mu.KLogging
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration

/**
 * 전통주 평점 변경 이벤트를 Redis에 발행하는 서비스
 * 1일 배치 처리를 위해 변경된 전통주 ID들을 Redis Set에 저장
 */
@Service
class LiquorRatingEventPublisher(
    private val redisTemplate: RedisTemplate<String, Any>
) {

    companion object : KLogging() {
        private const val RATING_CHANGED_LIQUORS_KEY = "liquor:rating:changed_liquors"
        private const val TTL_DAYS = 2L // 배치 실패 대비 2일간 보관
    }

    /**
     * 평점 변경된 전통주 ID를 레디스 Set에 추가
     * → 1일 후 배치 처리 시 이 Set을 참조하여 해당 전통주들만 업데이트
     *
     * @param liquorId 변경된 전통주 ID
     */
    fun markLiquorForUpdate(liquorId: String) {
        try {
            redisTemplate.opsForSet().add(RATING_CHANGED_LIQUORS_KEY, liquorId)

            // TTL 설정: 2일 (배치 실패 대비)
            redisTemplate.expire(RATING_CHANGED_LIQUORS_KEY, Duration.ofDays(TTL_DAYS))

            logger.debug("Marked liquor for rating update: $liquorId")
        } catch (e: Exception) {
            logger.error("Failed to mark liquor for update in Redis: $liquorId", e)
            // Redis 실패가 핵심 비즈니스 로직을 방해하지 않도록 예외를 던지지 않음
        }
    }

    /**
     * 배치 처리 완료 후 Set 초기화
     */
    fun clearChangedLiquors() {
        try {
            val deletedCount = redisTemplate.delete(RATING_CHANGED_LIQUORS_KEY)
            logger.info("Cleared changed liquors set from Redis. Deleted keys: $deletedCount")
        } catch (e: Exception) {
            logger.error("Failed to clear changed liquors set from Redis", e)
        }
    }

    /**
     * 변경된 전통주 ID 목록 조회
     *
     * @return 변경된 전통주 ID Set (배치 처리에서 사용)
     */
    fun getChangedLiquorIds(): Set<String> {
        return try {
            val members = redisTemplate.opsForSet().members(RATING_CHANGED_LIQUORS_KEY)
            val liquorIds = members?.map { it.toString() }?.toSet() ?: emptySet()

            logger.debug("Retrieved ${liquorIds.size} changed liquor IDs from Redis")
            liquorIds
        } catch (e: Exception) {
            logger.error("Failed to retrieve changed liquor IDs from Redis", e)
            emptySet()
        }
    }

    /**
     * 변경된 전통주 개수 조회 (모니터링용)
     *
     * @return 대기 중인 전통주 개수
     */
    fun getChangedLiquorCount(): Long {
        return try {
            redisTemplate.opsForSet().size(RATING_CHANGED_LIQUORS_KEY) ?: 0L
        } catch (e: Exception) {
            logger.error("Failed to get changed liquor count from Redis", e)
            0L
        }
    }

    /**
     * 특정 전통주가 변경 대기 목록에 있는지 확인 (디버깅용)
     *
     * @param liquorId 확인할 전통주 ID
     * @return 대기 목록 포함 여부
     */
    fun isLiquorMarkedForUpdate(liquorId: String): Boolean {
        return try {
            redisTemplate.opsForSet().isMember(RATING_CHANGED_LIQUORS_KEY, liquorId) ?: false
        } catch (e: Exception) {
            logger.error("Failed to check if liquor is marked for update: $liquorId", e)
            false
        }
    }

    /**
     * 수동으로 특정 전통주를 변경 목록에서 제거 (관리용)
     *
     * @param liquorId 제거할 전통주 ID
     */
    fun removeLiquorFromUpdateList(liquorId: String) {
        try {
            val removed = redisTemplate.opsForSet().remove(RATING_CHANGED_LIQUORS_KEY, liquorId)
            logger.info("Removed liquor from update list: $liquorId, removed: $removed")
        } catch (e: Exception) {
            logger.error("Failed to remove liquor from update list: $liquorId", e)
        }
    }
}
