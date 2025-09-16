package com.zzan.zzan.common.security.config

import com.zzan.zzan.common.security.filter.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() } // CSRF 보호 비활성화 (JWT 사용 시 불필요)
            .cors { it.configurationSource(corsConfigurationSource()) } // CORS 설정
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) } // 세션 관리 정책을 Stateless로 설정
            .authorizeHttpRequests { it.anyRequest().permitAll() } // 모든 요청 허용
            .formLogin { it.disable() } // 폼 로그인 비활성화

            .httpBasic { it.disable() } // HTTP Basic 인증 비활성화
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java) // JWT 인증 필터 추가
            .build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()

        // 모든 Origin 허용
        configuration.allowedOriginPatterns = listOf("*")

        // 모든 HTTP 메서드 허용
        configuration.allowedMethods = listOf("*")

        // 모든 헤더 허용
        configuration.allowedHeaders = listOf("*")

        // 인증 정보 포함 허용 (JWT 등)
        configuration.allowCredentials = true

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)

        return source
    }
}
