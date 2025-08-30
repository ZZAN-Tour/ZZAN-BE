package com.zzan.zzan.user.query.handler

import com.zzan.zzan.api.user.dto.LoginResponse
import com.zzan.zzan.api.user.dto.LoginUrl
import com.zzan.zzan.common.client.KakaoApiClient
import com.zzan.zzan.common.util.JwtUtil
import com.zzan.zzan.user.command.application.UserCommandService
import com.zzan.zzan.user.command.domain.User
import org.springframework.stereotype.Service

@Service
class AuthServiceImpl(
    private val kakaoApiClient: KakaoApiClient,
    private val userCommandService: UserCommandService,
    private val userQueryService: UserQueryService,
    private val jwtUtil: JwtUtil
) : AuthService {
    override fun getKakaoLoginUrl(): LoginUrl {
        return LoginUrl(kakaoApiClient.getLoginUrl())
    }

    override fun handleKakaoCallback(code: String): LoginResponse {
        // 1. 카카오 토큰 받기
        val kakaoAccessToken = kakaoApiClient.getAccessToken(code)

        // 2. 카카오 사용자 정보 받기
        val kakaoUser = kakaoApiClient.getUserInfo(kakaoAccessToken)

        // 3. 사용자 저장/조회
        val userId = userQueryService.findUserIdByKakaoId(kakaoUser.id.toString())
            ?: userCommandService.createUser(User.of(kakaoUser))

        // 4. JWT 토큰 생성 (액세스 토큰, 리프레시 토큰)
        val accessToken = jwtUtil.createAccessToken(userId)
        val refreshToken = jwtUtil.createRefreshToken(userId)

        return LoginResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }
}
