package com.zzan.zzan.common.util

import com.zzan.zzan.common.config.properties.JwtProperties
import com.zzan.zzan.common.exception.CustomException
import com.zzan.zzan.user.command.domain.User
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

    private fun createToken(claims: Map<String, String?>, validitySeconds: Long, token: Token): String {
        val now = Instant.now()
        val expiry = now.plusSeconds(validitySeconds)

        val jwtClaims = JwtClaimsSet.builder()
            .subject(claims["userId"] as String)
            .issuedAt(now)
            .expiresAt(expiry)
            .claim("type", token.type)
            .apply {
                claims.forEach { (key, value) ->
                    if (key != "userId") { // subject로 이미 설정됨
                        claim(key, value)
                    }
                }
            }
            .build()

        val headers = JwsHeader.with(MacAlgorithm.HS256).build()
        return jwtEncoder.encode(JwtEncoderParameters.from(headers, jwtClaims)).tokenValue
    }

    fun createAccessToken(user: User): String {
        val claims = mapOf(
            "userId" to user.id,
            "nickname" to user.nickname,
            "profileImageUrl" to user.profileImageUrl,
            "role" to user.role.name
        )
        return createToken(claims, jwtProperties.accessTokenValiditySeconds, Token.Access)
    }

    fun createRefreshToken(userId: String): String {
        val claims = mapOf("userId" to userId)
        return createToken(claims, jwtProperties.refreshTokenValiditySeconds, Token.Refresh)
    }

    fun validateToken(token: String): Boolean {
        return try {
            jwtDecoder.decode(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getUserIdFromToken(token: String): String? {
        return try {
            jwtDecoder.decode(token).subject
        } catch (e: Exception) {
            null
        }
    }

    fun getNicknameFromToken(token: String): String? {
        return try {
            jwtDecoder.decode(token).getClaimAsString("nickname")
        } catch (e: Exception) {
            null
        }
    }

    fun getProfileImageUrlFromToken(token: String): String? {
        return try {
            jwtDecoder.decode(token).getClaimAsString("profileImageUrl")
        } catch (e: Exception) {
            null
        }
    }

    fun getRoleFromToken(token: String): String? {
        return try {
            jwtDecoder.decode(token).getClaimAsString("role")
        } catch (e: Exception) {
            null
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
