package com.zzan.zzan.user.command.application

import com.zzan.zzan.user.command.domain.User
import com.zzan.zzan.user.command.infrastructure.UserCommandRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserCommandServiceImpl(
    private val userRepository: UserCommandRepository
) : UserCommandService {

    @CacheEvict(value = ["userIdByKakaoId"], key = "#user.kakaoId")
    override fun createUser(user: User): String {
        val savedUser = userRepository.save(user)
        return savedUser.id
    }
}
