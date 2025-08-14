package com.zzan.zzan.common.cache

import mu.KLogging
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class CacheManager(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val cacheProperties: CacheProperties
) : KLogging() {

    /**
     * Look Aside Pattern
     * 캐시에 없으면 null 반환, 호출자가 DB에서 조회 후 캐시에 Set
     */
    fun <T> get(key: String, clazz: Class<T>): T? {
        return try {
            val value = redisTemplate.opsForValue().get(key)
            if (value != null && clazz.isInstance(value)) {
                clazz.cast(value)
            } else {
                null
            }
        } catch (e: Exception) {
            logger.error("캐시 조회 중 에러: key=$key", e)
            null
        }
    }

    /**
     * Look Aside Pattern
     * 데이터를 DB에서 읽어 왔을 때는 캐시에도 Set
     */
    fun set(key: String, value: Any, ttl: Duration? = null) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl ?: cacheProperties.defaultTtl)
        } catch (e: Exception) {
            logger.error("캐시 저장 중 에러: key=$key", e)
        }
    }

    /**
     * Write Around Pattern
     * DB에 데이터 저장 후에는 캐시에서 Evict
     */
    fun evict(key: String) {
        try {
            redisTemplate.delete(key)
        } catch (e: Exception) {
            logger.error("캐시 삭제 중 에러: key=$key", e)
        }
    }
}
