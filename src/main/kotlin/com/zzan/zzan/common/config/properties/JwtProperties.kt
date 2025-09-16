package com.zzan.zzan.common.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "jwt")
class JwtProperties {
    lateinit var secretKey: String
    val accessTokenValiditySeconds: Long = 3600 * 24 * 30 // 30 days (임시)
    val refreshTokenValiditySeconds: Long = 30 * 24 * 60 * 60 // 30 days
}
