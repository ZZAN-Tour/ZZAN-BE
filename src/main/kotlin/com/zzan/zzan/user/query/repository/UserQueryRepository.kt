package com.zzan.zzan.user.query.repository

import com.zzan.zzan.user.command.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserQueryRepository : JpaRepository<User, String> {

    @Query("SELECT u.id FROM User u WHERE u.kakaoId = :kakaoId")
    fun findUserIdByKakaoId(kakaoId: String): String?
}
