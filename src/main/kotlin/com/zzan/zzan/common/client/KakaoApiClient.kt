package com.zzan.zzan.common.client

import com.zzan.zzan.common.client.dto.KakaoTokenResponse
import com.zzan.zzan.common.client.dto.KakaoUserResponse
import com.zzan.zzan.common.config.properties.KakaoProperties
import com.zzan.zzan.common.exception.CustomException
import mu.KLogging
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate

@Component
class KakaoApiClient(
    private val kakaoProperties: KakaoProperties
) : KLogging() {
    private val restTemplate = RestTemplate()

    fun getAccessToken(code: String): String {
        return call("카카오 액세스 토큰 획득에 실패했습니다") {
            restTemplate.postForEntity(
                KAKAO_TOKEN_URL,
                createTokenRequest(code),
                KakaoTokenResponse::class.java
            ).body?.accessToken
        }
    }

    fun getUserInfo(kakaoAccessToken: String): KakaoUserResponse {
        return call("카카오 사용자 정보를 가져올 수 없습니다") {
            restTemplate.exchange(
                KAKAO_USER_INFO_URL,
                HttpMethod.GET,
                createUserInfoRequest(kakaoAccessToken),
                KakaoUserResponse::class.java
            ).body
        }
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

    // API 호출 및 예외 처리 공통 로직
    private fun <T> call(errorMessage: String, block: () -> T?): T {
        return try {
            block() ?: throw CustomException(HttpStatus.UNAUTHORIZED, errorMessage)
        } catch (ex: HttpClientErrorException) {
            logger.error { "Kakao API Error: ${ex.statusCode} - ${ex.responseBodyAsString}" }
            throw CustomException(HttpStatus.UNAUTHORIZED, errorMessage)
        } catch (ex: RestClientException) {
            logger.error { "Kakao API RestClientException: ${ex.message}" }
            throw CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "외부 서버 통신 오류: ${ex.message}")
        }
    }

    companion object {
        private const val KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token"
        private const val KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me"
        private const val KAKAO_AUTH_URL = "https://kauth.kakao.com/oauth/authorize"
    }
}
