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
        println("===== 카카오 콜백 시작 =====")

        val kakaoAccessToken = kakaoApiClient.getAccessToken(code)
        println("카카오 액세스 토큰: $kakaoAccessToken")

        val kakaoUser = kakaoApiClient.getUserInfo(kakaoAccessToken)
        println("카카오 사용자 정보: ${kakaoUser.id}")

        val existingUser = userQueryService.findUserByKakaoId(kakaoUser.id.toString())
        println("기존 사용자 조회 결과: ${existingUser?.id}")

        val user = existingUser ?: run {
            println("===== 새 사용자 생성 시작 =====")
            try {
                val userToCreate = User.of(kakaoUser)
                println("생성할 User 객체: $userToCreate")

                val savedUser = userCommandService.createUser(userToCreate)
                println("저장된 사용자: ${savedUser.id}")

                // 바로 조회해서 검증
                val verification = userQueryService.findUserByKakaoId(kakaoUser.id.toString())
                println("저장 후 검증 조회: ${verification?.id}")

                savedUser
            } catch (e: Exception) {
                println("사용자 생성 실패: ${e.message}")
                e.printStackTrace()
                throw e
            }
        }

        println("최종 사용자 ID: ${user.id}")

        val accessToken = jwtUtil.createAccessToken(user)
        val refreshToken = jwtUtil.createRefreshToken(user.id)

        println("===== 카카오 콜백 완료 =====")
        return LoginResponse(accessToken = accessToken, refreshToken = refreshToken)
    }
}
