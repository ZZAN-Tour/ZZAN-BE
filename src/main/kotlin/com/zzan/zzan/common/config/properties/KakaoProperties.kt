package com.zzan.zzan.common.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "kakao")
class KakaoProperties {
    lateinit var clientId: String
    lateinit var redirectUri: String
}
