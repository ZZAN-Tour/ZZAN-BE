package com.zzan.zzan.common.security.filter

import com.zzan.zzan.common.util.JwtUtil
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtUtil: JwtUtil,
) : OncePerRequestFilter() {
    companion object {
        private val log = KotlinLogging.logger {}
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        SecurityContextHolder.clearContext() // 기존 인증정보 제거
        val token = extractTokenFromRequest(request)

        if (token != null && jwtUtil.validateToken(token) && jwtUtil.isAccessToken(token)) {
            try {
                setAuthenticationContext(token, request)
            } catch (e: Exception) {
                log.warn { "JWT 인증 처리 중 오류 발생: ${e.message}" }
                SecurityContextHolder.clearContext()
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun extractTokenFromRequest(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        return if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else {
            null
        }
    }

    private fun setAuthenticationContext(token: String, request: HttpServletRequest) {
        val userId = jwtUtil.getUserIdFromToken(token)
        val nickname = jwtUtil.getNicknameFromToken(token)
        val role = jwtUtil.getRoleFromToken(token)

        if (userId != null && role != null) {
            val authorities = listOf(SimpleGrantedAuthority("ROLE_$role"))

            val authentication = UsernamePasswordAuthenticationToken(
                userId, // name
                null,
                authorities
            ).apply {
                details = JwtUserPrincipal(userId, nickname, role)
            }

            SecurityContextHolder.getContext().authentication = authentication
        }
    }

    /**
     * JWT에서 추출한 사용자 정보를 담는 Principal 클래스
     */
    data class JwtUserPrincipal(
        val userId: String,
        val nickname: String?,
        val role: String
    )
}
