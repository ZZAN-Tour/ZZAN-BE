package com.zzan.zzan.user.query.handler

import com.zzan.zzan.user.query.repository.UserQueryRepository
import org.springframework.stereotype.Service

@Service
class UserQueryServiceImpl(
    private val userRepository: UserQueryRepository
) : UserQueryService {
    override fun findUserIdByKakaoId(kakaoId: String): String? {
        return userRepository.findUserIdByKakaoId(kakaoId)
    }
}
