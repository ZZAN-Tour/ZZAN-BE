package com.zzan.zzan.common.client

import com.zzan.zzan.common.client.dto.KakaoTokenResponse
import com.zzan.zzan.common.client.dto.KakaoUserResponse
import com.zzan.zzan.common.config.properties.KakaoProperties
import com.zzan.zzan.common.exception.CustomException
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class KakaoApiClient(
    private val kakaoProperties: KakaoProperties
) {
    private val restTemplate = RestTemplate()

    fun getAccessToken(code: String): String {
        return restTemplate.postForEntity(
            KAKAO_TOKEN_URL,
            createTokenRequest(code),
            KakaoTokenResponse::class.java
        ).body?.accessToken
            ?: throw CustomException(HttpStatus.UNAUTHORIZED, "카카오 액세스 토큰 획득에 실패했습니다")
    }

    fun getUserInfo(kakaoAccessToken: String): KakaoUserResponse {
        return restTemplate.exchange(
            KAKAO_USER_INFO_URL,
            HttpMethod.GET,
            createUserInfoRequest(kakaoAccessToken),
            KakaoUserResponse::class.java
        ).body
            ?: throw CustomException(HttpStatus.BAD_REQUEST, "카카오 사용자 정보를 가져올 수 없습니다")
    }

    fun getLoginUrl(): String {
        return buildString {
            append(KAKAO_AUTH_URL)
            append("?client_id=${kakaoProperties.clientId}")
            append("&redirect_uri=${kakaoProperties.redirectUri}")
            append("&response_type=code")
        }
    }

    private fun createTokenRequest(code: String): HttpEntity<String> {
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_FORM_URLENCODED
        }

        val body = buildString {
            append("grant_type=authorization_code")
            append("&client_id=${kakaoProperties.clientId}")
            append("&redirect_uri=${kakaoProperties.redirectUri}")
            append("&code=$code")
        }

        return HttpEntity(body, headers)
    }

    private fun createUserInfoRequest(accessToken: String): HttpEntity<String> {
        val headers = HttpHeaders().apply {
            set("Authorization", "Bearer $accessToken")
        }
        return HttpEntity(headers)
    }

    companion object {
        private const val KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token"
        private const val KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me"
        private const val KAKAO_AUTH_URL = "https://kauth.kakao.com/oauth/authorize"
    }
}
