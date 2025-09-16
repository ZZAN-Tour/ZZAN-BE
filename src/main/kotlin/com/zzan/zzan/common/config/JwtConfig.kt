package com.zzan.zzan.common.config

import com.nimbusds.jose.jwk.source.ImmutableSecret
import com.nimbusds.jose.proc.SecurityContext
import com.zzan.zzan.common.config.properties.JwtProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import javax.crypto.spec.SecretKeySpec

@Configuration
class JwtConfig(
    private val jwtProperties: JwtProperties
) {

    @Bean
    fun jwtEncoder(): JwtEncoder {
        val secret = SecretKeySpec(jwtProperties.secretKey.toByteArray(), "HmacSHA256")
        val immutableSecret = ImmutableSecret<SecurityContext>(secret)
        return NimbusJwtEncoder(immutableSecret)
    }

    @Bean
    fun jwtDecoder(): JwtDecoder {
        val secret = SecretKeySpec(jwtProperties.secretKey.toByteArray(), "HmacSHA256")
        return NimbusJwtDecoder.withSecretKey(secret).macAlgorithm(MacAlgorithm.HS256).build()
    }
}
