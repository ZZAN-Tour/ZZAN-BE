package com.zzan.zzan.user.query.handler

import com.zzan.zzan.api.user.dto.LoginResponse
import com.zzan.zzan.api.user.dto.LoginUrl

interface AuthService {
    /**
     * 카카오 로그인 URL을 생성합니다.
     * @return 카카오 로그인 URL
     */
    fun getKakaoLoginUrl(): LoginUrl

    /**
     * OAuth2.0 콜백을 처리합니다.
     * @param code 인가 코드
     * @return 로그인 응답 (액세스 토큰 및 리프레시 토큰 포함)
     */
    fun handleKakaoCallback(code: String): LoginResponse
}
