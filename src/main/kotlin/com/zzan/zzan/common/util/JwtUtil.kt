package com.zzan.zzan.common.util

import com.zzan.zzan.common.config.properties.JwtProperties
import com.zzan.zzan.common.exception.CustomException
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.*
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class JwtUtil(
    private val jwtProperties: JwtProperties,
    private val jwtEncoder: JwtEncoder,
    private val jwtDecoder: JwtDecoder
) {

    enum class Token(val type: String) {
        Access("access"),
        Refresh("refresh")
    }

    private fun createToken(userId: String, validitySeconds: Long, token: Token): String {
        val now = Instant.now()
        val expiry = now.plusSeconds(validitySeconds)

        val claims = JwtClaimsSet.builder()
            .subject(userId)
            .issuedAt(now)
            .expiresAt(expiry)
            .claim("type", token.type)
            .build()

        val headers = JwsHeader.with(MacAlgorithm.HS256).build()
        return jwtEncoder.encode(JwtEncoderParameters.from(headers, claims)).tokenValue
    }

    fun createAccessToken(userId: String): String {
        return createToken(userId, jwtProperties.accessTokenValiditySeconds, Token.Access)
    }

    fun createRefreshToken(userId: String): String {
        return createToken(userId, jwtProperties.refreshTokenValiditySeconds, Token.Refresh)
    }

    fun validateToken(token: String): Boolean {
        return try {
            jwtDecoder.decode(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getUserIdFromToken(token: String): String {
        return try {
            jwtDecoder.decode(token).subject
        } catch (e: Exception) {
            throw CustomException(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다")
        }
    }

    fun getTokenType(token: String): String {
        return try {
            jwtDecoder.decode(token).getClaimAsString("type")
        } catch (e: Exception) {
            throw CustomException(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다")
        }
    }

    fun isAccessToken(token: String): Boolean {
        return getTokenType(token) == Token.Access.type
    }

    fun isRefreshToken(token: String): Boolean {
        return getTokenType(token) == Token.Refresh.type
    }

    fun isTokenExpired(token: String): Boolean {
        return try {
            val jwt = jwtDecoder.decode(token)
            jwt.expiresAt?.isBefore(Instant.now()) ?: true
        } catch (e: Exception) {
            true
        }
    }
}
