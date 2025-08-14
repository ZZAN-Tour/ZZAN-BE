package com.zzan.zzan.common.cache

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "cache")
data class CacheProperties (
    val defaultTtl: Long =  86400 // 1 day in seconds
)