package com.zzan.zzan.common.config

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig {

    @Bean
    fun redisTemplate(
        connectionFactory: RedisConnectionFactory
    ): RedisTemplate<String, Any> {
        val template = RedisTemplate<String, Any>()
        template.connectionFactory = connectionFactory

        // JSON Serializer 설정
        val jackson2JsonRedisSerializer = Jackson2JsonRedisSerializer(redisObjectMapper(), Any::class.java)
        template.keySerializer = StringRedisSerializer()
        template.hashKeySerializer = StringRedisSerializer()
        template.valueSerializer = jackson2JsonRedisSerializer
        template.hashValueSerializer = jackson2JsonRedisSerializer

        template.setDefaultSerializer(jackson2JsonRedisSerializer)
        template.afterPropertiesSet()
        return template
    }

    @Bean
    fun stringRedisTemplate(connectionFactory: RedisConnectionFactory): StringRedisTemplate {
        return StringRedisTemplate(connectionFactory)
    }

    @Bean("redisObjectMapper")
    fun redisObjectMapper(): ObjectMapper {
        return ObjectMapper().apply {
            // Kotlin 지원을 위한 모듈 등록
            registerKotlinModule()

            // Java 8 Time API 지원을 위한 모듈 등록
            registerModule(JavaTimeModule())

            // 모든 필드에 대해 직렬화/역직렬화 허용
            setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY)

            // 타입 정보 포함 (역직렬화 시 타입 안전성)
            activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.PROPERTY
            )
        }
    }
}
