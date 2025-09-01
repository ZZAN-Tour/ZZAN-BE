package com.zzan.zzan.api.security.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KLogging
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter : OncePerRequestFilter() {
    companion object : KLogging()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // 정적 리소스는 JWT 검증 생략
        if (request.requestURI == "/favicon.ico") {
            filterChain.doFilter(request, response)
            return
        }

        // 실제 JWT 인증 로직이 여기에 들어갈 예정
        filterChain.doFilter(request, response)
    }
}
