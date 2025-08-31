package com.zzan.zzan.user.query.handler

interface UserQueryService {
    fun findUserIdByKakaoId(kakaoId: String): String?
}
