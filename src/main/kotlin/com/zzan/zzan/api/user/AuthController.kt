package com.zzan.zzan.api.user

import com.zzan.zzan.api.user.dto.LoginResponse
import com.zzan.zzan.api.user.dto.LoginUrl
import com.zzan.zzan.common.response.ApiResponse
import com.zzan.zzan.user.query.handler.AuthService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService
) {

    @GetMapping("/kakao/login-url")
    fun getLoginUrl(): ApiResponse<LoginUrl> {
        return ApiResponse.ok(authService.getKakaoLoginUrl());
    }

    @GetMapping("/kakao/callback")
    fun kakaoCallback(@RequestParam code: String): ApiResponse<LoginResponse> {
        return ApiResponse.ok(authService.handleKakaoCallback(code))
    }
}
