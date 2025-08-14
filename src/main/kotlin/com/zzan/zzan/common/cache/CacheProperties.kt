package com.zzan.zzan.common.cache

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "cache")
data class CacheProperties(
    val defaultTtl: Duration = Duration.ofDays(1)
)
